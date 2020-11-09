package me.earth.phobos.manager;

import com.google.common.base.Strings;
import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.ConnectionEvent;
import me.earth.phobos.event.events.DeathEvent;
import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.event.events.Render2DEvent;
import me.earth.phobos.event.events.Render3DEvent;
import me.earth.phobos.event.events.TotemPopEvent;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.Feature;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.client.Managers;
import me.earth.phobos.util.TextUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.Objects;
import java.util.UUID;

public class EventManager extends Feature {

    private final Timer timer = new Timer();
    private final Timer logoutTimer = new Timer();

    public void init() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onUnload() { MinecraftForge.EVENT_BUS.unregister(this); }

    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!fullNullCheck() && event.getEntity().getEntityWorld().isRemote && event.getEntityLiving().equals(mc.player)) {
            Phobos.potionManager.update();
            Phobos.totemPopManager.onUpdate();
            Phobos.inventoryManager.update();
            Phobos.holeManager.update();
            Phobos.moduleManager.onUpdate();
            Phobos.timerManager.update();
            if(timer.passedMs(Managers.getInstance().moduleListUpdates.getValue())) {
                Phobos.moduleManager.sortModules(true);
                timer.reset();
            }
        }
    }

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        logoutTimer.reset();
        Phobos.moduleManager.onLogin();
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Phobos.moduleManager.onLogout();
        Phobos.totemPopManager.onLogout();
        Phobos.potionManager.onLogout();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(fullNullCheck()) {
            return;
        }

        Phobos.moduleManager.onTick();

        for (EntityPlayer player : mc.world.playerEntities) {

            if (player == null || player.getHealth() > 0) {
                continue;
            }

            MinecraftForge.EVENT_BUS.post(new DeathEvent(player));
            Phobos.totemPopManager.onDeath(player);
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if(fullNullCheck()) return;

        if(event.getStage() == 0) {
            Phobos.speedManager.updateValues();
            Phobos.rotationManager.updateRotations();
            Phobos.positionManager.updatePosition();
        }

        if(event.getStage() == 1) {
            Phobos.rotationManager.restoreRotations();
            Phobos.positionManager.restorePosition();
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if(event.getStage() != 0) {
            return;
        }

        Phobos.serverManager.onPacketReceived();

        if(event.getPacket() instanceof SPacketEntityStatus) {
            SPacketEntityStatus packet = event.getPacket();
            if(packet.getOpCode() == 35) {
                if (packet.getEntity(mc.world) instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer)packet.getEntity(mc.world);
                    MinecraftForge.EVENT_BUS.post(new TotemPopEvent(player));
                    Phobos.totemPopManager.onTotemPop(player);
                    Phobos.potionManager.onTotemPop(player);
                }
            }
        }

        if(event.getPacket() instanceof SPacketPlayerListItem && !fullNullCheck() && logoutTimer.passedS(1)) {
            final SPacketPlayerListItem packet = event.getPacket();
            if (!SPacketPlayerListItem.Action.ADD_PLAYER.equals(packet.getAction()) && !SPacketPlayerListItem.Action.REMOVE_PLAYER.equals(packet.getAction())) {
                return;
            }

            packet.getEntries().stream().filter(Objects::nonNull).filter(data -> !Strings.isNullOrEmpty(data.getProfile().getName()) || data.getProfile().getId() != null)
                    .forEach(data -> {
                                final UUID id = data.getProfile().getId();
                                switch(packet.getAction()) {
                                    case ADD_PLAYER:
                                        String name = data.getProfile().getName();
                                        MinecraftForge.EVENT_BUS.post(new ConnectionEvent(0, id, name));
                                        break;
                                    case REMOVE_PLAYER:
                                        EntityPlayer entity = mc.world.getPlayerEntityByUUID(id);
                                        if(entity != null) {
                                            String logoutName = entity.getName();
                                            MinecraftForge.EVENT_BUS.post(new ConnectionEvent(1, entity, id, logoutName));
                                        } else {
                                            MinecraftForge.EVENT_BUS.post(new ConnectionEvent(2, id, null));
                                        }
                                        break;
                                }
                            });
        }

        if(event.getPacket() instanceof SPacketTimeUpdate) {
            Phobos.serverManager.update();
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (event.isCanceled()) {
            return;
        }

        mc.profiler.startSection("phobos");
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableDepth();
        GlStateManager.glLineWidth(1f);
        Render3DEvent render3dEvent = new Render3DEvent(event.getPartialTicks());
        Phobos.moduleManager.onRender3D(render3dEvent);
        //MinecraftForge.EVENT_BUS.post(render3dEvent);
        GlStateManager.glLineWidth(1f);
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
        mc.profiler.endSection();
    }

    @SubscribeEvent
    public void renderHUD(RenderGameOverlayEvent.Post event) {
        if(event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
            Phobos.textManager.updateResolution();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Text event) {
        if (event.getType().equals(RenderGameOverlayEvent.ElementType.TEXT)) {
            final ScaledResolution resolution = new ScaledResolution(mc);
            Render2DEvent render2DEvent = new Render2DEvent(event.getPartialTicks(), resolution);
            Phobos.moduleManager.onRender2D(render2DEvent);
            GlStateManager.color(1.f, 1.f, 1.f, 1.f);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState()) {
            Phobos.moduleManager.onKeyPressed(Keyboard.getEventKey());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatSent(ClientChatEvent event) {
        if(event.getMessage().startsWith(Command.getCommandPrefix())) {
            event.setCanceled(true);
            try {
                mc.ingameGUI.getChatGUI().addToSentMessages(event.getMessage());
                if (event.getMessage().length() > 1) {
                    Phobos.commandManager.executeCommand(event.getMessage().substring(Command.getCommandPrefix().length() - 1));
                } else {
                    Command.sendMessage("Please enter a command.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Command.sendMessage(TextUtil.RED +  "An error occurred while running this command. Check the log!");
            }
            event.setMessage("");
        }
    }
}
