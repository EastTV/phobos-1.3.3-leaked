package me.earth.phobos.features.modules.client;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.Render2DEvent;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.combat.Auto32k;
import me.earth.phobos.features.modules.misc.ToolTips;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.ColorUtil;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.MathUtil;
import me.earth.phobos.util.RenderUtil;
import me.earth.phobos.util.TextUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HUD extends Module {

    private final Setting<Boolean> renderingUp = register(new Setting("RenderingUp", false, "Orientation of the HUD-Elements."));
    public Setting<Boolean> potionIcons = register(new Setting("PotionIcons", true, "Draws Potion Icons."));
    public Setting<Boolean> shadow = register(new Setting("Shadow", false, "Draws the text with a shadow."));
    private final Setting<WaterMark> watermark = register(new Setting("Logo", WaterMark.NONE, "WaterMark"));
    private final Setting<Boolean> modeVer = register(new Setting("Version", false, v -> watermark.getValue() != WaterMark.NONE));
    private final Setting<Boolean> arrayList = register(new Setting("ActiveModules", false, "Lists the active modules."));
    private final Setting<Boolean> serverBrand = register(new Setting("ServerBrand", false, "Brand of the server you are on."));
    private final Setting<Boolean> ping = register(new Setting("Ping", false, "Your response time to the server."));
    private final Setting<Boolean> tps = register(new Setting("TPS", false, "Ticks per second of the server."));
    private final Setting<Boolean> fps = register(new Setting("FPS", false, "Your frames per second."));
    private final Setting<Boolean> coords = register(new Setting("Coords", false, "Your current coordinates"));
    private final Setting<Boolean> direction = register(new Setting("Direction", false, "The Direction you are facing."));
    private final Setting<Boolean> speed = register(new Setting("Speed", false, "Your Speed"));
    private final Setting<Boolean> potions = register(new Setting("Potions", false, "Your Speed"));
    public Setting<Boolean> textRadar = register(new Setting("TextRadar", false, "A TextRadar"));
    private final Setting<Boolean> armor = register(new Setting("Armor", false, "ArmorHUD"));
    private final Setting<Boolean> percent = register(new Setting("Percent", true, v -> armor.getValue()));
    private final Setting<Boolean> totems = register(new Setting("Totems", false, "TotemHUD"));
    private final Setting<Greeter> greeter = register(new Setting("Greeter", Greeter.NONE, "Greets you."));
    private final Setting<String> spoofGreeter = register(new Setting("GreeterName", "3arthqu4ke", v -> greeter.getValue() == Greeter.CUSTOM));
    public Setting<Boolean> time = register(new Setting("Time", false, "The time"));
    private final Setting<LagNotify> lag = register(new Setting("Lag", LagNotify.GRAY, "Lag Notifier"));
    private final Setting<Boolean> hitMarkers = register(new Setting("HitMarkers", true));
    public Setting<Integer> hudRed = register(new Setting("Red", 255, 0, 255));
    public Setting<Integer> hudGreen = register(new Setting("Green", 0, 0, 255));
    public Setting<Integer> hudBlue = register(new Setting("Blue", 0, 0, 255));
    private final Setting<Boolean> grayNess = register(new Setting("FutureColour", true));
    private static HUD INSTANCE = new HUD();

    private Map<String, Integer> players = new HashMap<>();

    private static final ResourceLocation box = new ResourceLocation("textures/gui/container/shulker_box.png");
    private static final ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);
    private int color;
    private boolean shouldIncrement;
    private int hitMarkerTimer;
    private final Timer timer = new Timer();

    public HUD() {
        super("HUD", "HUD Elements rendered on your screen", Category.CLIENT, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static HUD getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HUD();
        }
        return INSTANCE;
    }

    @Override
    public void onUpdate() {
        if (timer.passedMs(Managers.getInstance().textRadarUpdates.getValue())) {
            this.players = getTextRadarPlayers();
            timer.reset();
        }
        if (shouldIncrement) {
            hitMarkerTimer++;
        }
        if (hitMarkerTimer == 10) {
            hitMarkerTimer = 0;
            shouldIncrement = false;
        }
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        if (fullNullCheck()) {
            return;
        }

        int width = renderer.scaledWidth;
        int height = renderer.scaledHeight;
        color = ColorUtil.toRGBA(hudRed.getValue(), hudGreen.getValue(), hudBlue.getValue());
        String grayString = (grayNess.getValue() ? TextUtil.GRAY : "");

        switch (watermark.getValue()) {
            case PHOBOS:
                renderer.drawString("Phobos" + (modeVer.getValue() ? " v" + Phobos.MODVER : ""), 2, 2, color, true);
                break;
            case EARTH:
                renderer.drawString("3arthh4ck" + (modeVer.getValue() ? " v" + Phobos.MODVER : ""), 2, 2, color, true);
            default:
        }

        if (textRadar.getValue()) {
            drawTextRadar((ToolTips.getInstance().isOff() || !ToolTips.getInstance().shulkerSpy.getValue() || !ToolTips.getInstance().render.getValue()) ? 0 : ToolTips.getInstance().getTextRadarY());
        }
        int j = renderingUp.getValue() ?0:(mc.currentScreen instanceof GuiChat ? 14 : 0);
        if (arrayList.getValue()) {
            if (renderingUp.getValue()) {
                for (int i = 0; i < Phobos.moduleManager.sortedModules.size(); i++) {
                    Module module = Phobos.moduleManager.sortedModules.get(i);
                    String text = module.getDisplayName() + TextUtil.GRAY + (module.getDisplayInfo() != null ? " [" + TextUtil.WHITE + module.getDisplayInfo() + TextUtil.GRAY + "]" : "");
                    renderer.drawString(text, width - 2 - renderer.getStringWidth(text), 2 + j * 10, color, true);
                    j++;
                }
            } else {
                for (int i = 0; i < Phobos.moduleManager.sortedModules.size(); i++) {
                    Module module = Phobos.moduleManager.sortedModules.get(i);
                    String text = module.getDisplayName() + TextUtil.GRAY + (module.getDisplayInfo() != null ? " [" + TextUtil.WHITE + module.getDisplayInfo() + TextUtil.GRAY + "]" : "");
                    renderer.drawString(text, width - 2 - renderer.getStringWidth(text), height - (j += 10), color, true);
                }
            }
        }

        int i = mc.currentScreen instanceof GuiChat ? 14 : 0;
        if (renderingUp.getValue()) {
            if (serverBrand.getValue()) {
                String text = grayString + "Server brand " + TextUtil.WHITE + Phobos.serverManager.getServerBrand();
                renderer.drawString(text, width - (renderer.getStringWidth(text) + 2), height - 2 - (i += 10), color, true);
            }
            if (potions.getValue()) {
                for (PotionEffect effect : Phobos.potionManager.getOwnPotions()) {
                    String text = Phobos.potionManager.getColoredPotionString(effect);
                    renderer.drawString(text, width - (renderer.getStringWidth(text) + 2), height - 2 - (i += 10), color, true);
                }
            }
            if (speed.getValue()) {
                String text = grayString + "Speed " + TextUtil.WHITE + Phobos.speedManager.getSpeedKpH() + " km/h";
                renderer.drawString(text, width - (renderer.getStringWidth(text) + 2), height - 2 - (i += 10), color, true);
            }
            if (time.getValue()) {
                String text = grayString + "Time " + TextUtil.WHITE + (new SimpleDateFormat("h:mm a").format(new Date()));
                renderer.drawString(text, width - (renderer.getStringWidth(text) + 2), height - 2 - (i += 10), color, true);
            }
            if (tps.getValue()) {
                String text = grayString + "TPS " + TextUtil.WHITE + Phobos.serverManager.getTPS();
                renderer.drawString(text, width - (renderer.getStringWidth(text) + 2), height - 2 - (i += 10), color, true);
            }
            String fpsText = grayString + "FPS " + TextUtil.WHITE + Minecraft.debugFPS;
            String text = grayString + "Ping " + TextUtil.WHITE + Phobos.serverManager.getPing();
            if (renderer.getStringWidth(text) > renderer.getStringWidth(fpsText)) {
                if (ping.getValue()) {
                    renderer.drawString(text, width - (renderer.getStringWidth(text) + 2), height - 2 - (i += 10), color, true);
                }
                if (fps.getValue()) {
                    renderer.drawString(fpsText, width - (renderer.getStringWidth(fpsText) + 2), height - 2 - (i += 10), color, true);
                }
            } else {
                if (fps.getValue()) {
                    renderer.drawString(fpsText, width - (renderer.getStringWidth(fpsText) + 2), height - 2 - (i += 10), color, true);
                }
                if (ping.getValue()) {
                    renderer.drawString(text, width - (renderer.getStringWidth(text) + 2), height - 2 - (i += 10), color, true);
                }
            }
        } else {
            if (serverBrand.getValue()) {
                String text = grayString + "Server brand " + TextUtil.RESET + Phobos.serverManager.getServerBrand();
                renderer.drawString(text, width - (renderer.getStringWidth(text) + 2), 2 + i++ * 10, color, true);
            }
            if (potions.getValue()) {
                for (PotionEffect effect : Phobos.potionManager.getOwnPotions()) {
                    String text = Phobos.potionManager.getColoredPotionString(effect);
                    renderer.drawString(text, width - (renderer.getStringWidth(text) + 2), 2 + i++ * 10, color, true);
                }
            }
            if (speed.getValue()) {
                String text = grayString + "Speed " + TextUtil.RESET + Phobos.speedManager.getSpeedKpH() + " km/h";
                renderer.drawString(text, width - (renderer.getStringWidth(text) + 2), 2 + i++ * 10, color, true);
            }
            if (time.getValue()) {
                String text = grayString + "Time " + TextUtil.RESET + (new SimpleDateFormat("h:mm a").format(new Date()));
                renderer.drawString(text, width - (renderer.getStringWidth(text) + 2), 2 + i++ * 10, color, true);
            }
            if (tps.getValue()) {
                String text = grayString + "TPS " + TextUtil.RESET + Phobos.serverManager.getTPS();
                renderer.drawString(text, width - (renderer.getStringWidth(text) + 2), 2 + i++ * 10, color, true);
            }
            String fpsText = grayString + "FPS " + TextUtil.RESET + Minecraft.debugFPS;
            String text = grayString + "Ping " + TextUtil.RESET + Phobos.serverManager.getPing();
            if (renderer.getStringWidth(text) > renderer.getStringWidth(fpsText)) {
                if (ping.getValue()) {
                    renderer.drawString(text, width - (renderer.getStringWidth(text) + 2), 2 + i++ * 10, color, true);
                }
                if (fps.getValue()) {
                    renderer.drawString(fpsText, width - (renderer.getStringWidth(fpsText) + 2), 2 + i++ * 10, color, true);
                }
            } else {
                if (fps.getValue()) {
                    renderer.drawString(fpsText, width - (renderer.getStringWidth(fpsText) + 2), 2 + i++ * 10, color, true);
                }
                if (ping.getValue()) {
                    renderer.drawString(text, width - (renderer.getStringWidth(text) + 2), 2 + i++ * 10, color, true);
                }
            }
        }

        boolean inHell = (mc.world.getBiome(mc.player.getPosition()).getBiomeName().equals("Hell"));

        int posX = (int) mc.player.posX;
        int posY = (int) mc.player.posY;
        int posZ = (int) mc.player.posZ;

        float nether = !inHell ? 0.125f : 8;
        int hposX = (int) (mc.player.posX * nether);
        int hposZ = (int) (mc.player.posZ * nether);

        if (renderingUp.getValue()) {
            Phobos.notificationManager.handleNotifications(height - (i + 16));
        } else {
            Phobos.notificationManager.handleNotifications(height - (j + 16));
        }

        i = mc.currentScreen instanceof GuiChat ? 14 : 0;
        String coordinates = TextUtil.RESET + "XYZ " + TextUtil.WHITE + posX + ", " + posY + ", " + posZ + " " + TextUtil.RESET + "[" + TextUtil.WHITE + hposX + ", " + hposZ + TextUtil.RESET + "]";
        String text = (direction.getValue() ? Phobos.rotationManager.getDirection4D(false) + " " : "") + (coords.getValue() ? coordinates : "") + "";

        renderer.drawString(text, 2, height - (i += 10), color, true);

        if (armor.getValue()) {
            renderArmorHUD(percent.getValue());
        }

        if (totems.getValue()) {
            renderTotemHUD();
        }

        if (greeter.getValue() != Greeter.NONE) {
            renderGreeter();
        }

        if (lag.getValue() != LagNotify.NONE) {
            renderLag();
        }
    }

    public Map<String, Integer> getTextRadarPlayers() {
        return EntityUtil.getTextRadarPlayers();
    }

    public void renderGreeter() {
        int width = renderer.scaledWidth;
        String text = "";
        switch (greeter.getValue()) {
            case TIME:
                text += MathUtil.getTimeOfDay() + mc.player.getDisplayNameString();
                break;
            case LONG:
                text += "Welcome to Phobos.eu " + mc.player.getDisplayNameString() + " :^)";
                break;
            case CUSTOM:
                text += spoofGreeter.getValue();
                break;
            default:
                text += "Welcome " + mc.player.getDisplayNameString();
        }
        renderer.drawString(text, (width / 2.0f) - (renderer.getStringWidth(text) / 2.0f) + 2, 2, color, true);
    }

    public void renderLag() {
        int width = renderer.scaledWidth;
        if (Phobos.serverManager.isServerNotResponding()) {
            String text = (lag.getValue() == LagNotify.GRAY ? TextUtil.GRAY : TextUtil.RED) + "Server not responding: " + MathUtil.round((Phobos.serverManager.serverRespondingTime() / 1000.0f), 1) + "s.";
            renderer.drawString(text, (width / 2.0f) - (renderer.getStringWidth(text) / 2.0f) + 2, 20, color, true);
        }
    }

    public void renderTotemHUD() {
        int width = renderer.scaledWidth;
        int height = renderer.scaledHeight;
        int totems = mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING).mapToInt(ItemStack::getCount).sum();
        if (mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING)
            totems += mc.player.getHeldItemOffhand().getCount();
        if (totems > 0) {
            GlStateManager.enableTexture2D();
            int i = width / 2;
            int iteration = 0;
            int y = height - 55 - (mc.player.isInWater() && mc.playerController.gameIsSurvivalOrAdventure() ? 10 : 0);
            int x = i - 189 + 9 * 20 + 2;
            GlStateManager.enableDepth();
            RenderUtil.itemRender.zLevel = 200F;
            RenderUtil.itemRender.renderItemAndEffectIntoGUI(totem, x, y);
            RenderUtil.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, totem, x, y, "");
            RenderUtil.itemRender.zLevel = 0F;
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            renderer.drawStringWithShadow(totems + "", x + 19 - 2 - renderer.getStringWidth(totems + ""), y + 9, 0xffffff);
            //mc.fontRenderer.drawStringWithShadow(totems + "", x + 19 - 2 - mc.fontRenderer.getStringWidth(totems + ""), y + 9, 0xffffff);
            GlStateManager.enableDepth();
            GlStateManager.disableLighting();
        }
    }

    public void renderArmorHUD(boolean percent) {
        int width = renderer.scaledWidth;
        int height = renderer.scaledHeight;
        GlStateManager.enableTexture2D();
        int i = width / 2;
        int iteration = 0;
        int y = height - 55 - (mc.player.isInWater() && mc.playerController.gameIsSurvivalOrAdventure() ? 10 : 0);
        for (ItemStack is : mc.player.inventory.armorInventory) {
            iteration++;
            if (is.isEmpty()) continue;
            int x = i - 90 + (9 - iteration) * 20 + 2;
            GlStateManager.enableDepth();
            RenderUtil.itemRender.zLevel = 200F;
            RenderUtil.itemRender.renderItemAndEffectIntoGUI(is, x, y);
            RenderUtil.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, is, x, y, "");
            RenderUtil.itemRender.zLevel = 0F;
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            String s = is.getCount() > 1 ? is.getCount() + "" : "";
            renderer.drawStringWithShadow(s, x + 19 - 2 - renderer.getStringWidth(s), y + 9, 0xffffff);
            //mc.fontRenderer.drawStringWithShadow(s, x + 19 - 2 - mc.fontRenderer.getStringWidth(s), y + 9, 0xffffff);

            if (percent) {
                int dmg = 0;
                int itemDurability = is.getMaxDamage() - is.getItemDamage();
                float green = ((float) is.getMaxDamage() - (float) is.getItemDamage()) / (float) is.getMaxDamage();
                float red = 1 - green;
                if (percent) { //(for percent was parted in old phobos)
                    dmg = 100 - (int) (red * 100);
                } else {
                    dmg = itemDurability;
                }
                renderer.drawStringWithShadow(dmg + "", x + 8 - renderer.getStringWidth(dmg + "") / 2, y - 11, ColorUtil.toRGBA((int) (red * 255), (int) (green * 255), 0));
            }
        }
        GlStateManager.enableDepth();
        GlStateManager.disableLighting();
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(AttackEntityEvent event) {
        shouldIncrement = true;
    }

    public void drawHitMarkers() {

    }

    public void drawTextRadar(int yOffset) {
        if (!this.players.isEmpty()) {
            int y = renderer.getFontHeight() + 7 + yOffset;
            for (Map.Entry<String, Integer> player : this.players.entrySet()) {
                String text = player.getKey() + " ";
                int textheight = renderer.getFontHeight() + 1;
                renderer.drawString(text, 2, y, color, true);
                y += textheight;
            }
        }
    }

    public enum Greeter {
        NONE,
        NAME,
        TIME,
        LONG,
        CUSTOM
    }

    public enum LagNotify {
        NONE,
        RED,
        GRAY
    }

    public enum WaterMark {
        NONE,
        PHOBOS,
        EARTH
    }
}
