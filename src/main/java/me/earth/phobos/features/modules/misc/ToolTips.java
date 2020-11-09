package me.earth.phobos.features.modules.misc;

import me.earth.phobos.event.events.Render2DEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Bind;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.ColorUtil;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.RenderUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static me.earth.phobos.util.RenderUtil.itemRender;

public class ToolTips extends Module {

    public Setting<Boolean> maps = register(new Setting("Maps", true));
    public Setting<Boolean> shulkers = register(new Setting("ShulkerViewer", true));
    public Setting<Bind> peek = register(new Setting("Peek", new Bind(-1)));
    public Setting<Boolean> shulkerSpy = register(new Setting("ShulkerSpy", true));
    public Setting<Boolean> render = register(new Setting("Render", true, v -> shulkerSpy.getValue()));
    public Setting<Boolean> own = register(new Setting("OwnShulker", true, v -> shulkerSpy.getValue()));
    public Setting<Integer> cooldown = register(new Setting("ShowForS", 2, 0, 5, v -> shulkerSpy.getValue()));
    public Setting<Boolean> textColor = register(new Setting("TextColor", false, v -> shulkers.getValue()));
    private final Setting<Integer> red = register(new Setting("Red", 255, 0, 255, v -> textColor.getValue()));
    private final Setting<Integer> green = register(new Setting("Green", 0, 0, 255, v -> textColor.getValue()));
    private final Setting<Integer> blue = register(new Setting("Blue", 0, 0, 255, v -> textColor.getValue()));
    private final Setting<Integer> alpha = register(new Setting("Alpha", 255, 0, 255, v -> textColor.getValue()));
    public Setting<Boolean> offsets = register(new Setting("Offsets", false));
    private final Setting<Integer> yPerPlayer = register(new Setting("Y/Player", 18, v -> offsets.getValue()));
    private final Setting<Integer> xOffset = register(new Setting("XOffset", 4, v -> offsets.getValue()));
    private final Setting<Integer> yOffset = register(new Setting("YOffset", 2, v -> offsets.getValue()));
    private final Setting<Integer> trOffset = register(new Setting("TROffset", 2, v -> offsets.getValue()));
    public Setting<Integer> invH = register(new Setting("InvH", 3, v -> offsets.getValue()));

    private static final ResourceLocation MAP = new ResourceLocation("textures/map/map_background.png");
    private static final ResourceLocation SHULKER_GUI_TEXTURE = new ResourceLocation("textures/gui/container/shulker_box.png");
    private static ToolTips INSTANCE = new ToolTips();
    public Map<EntityPlayer, ItemStack> spiedPlayers = new ConcurrentHashMap<>();
    public Map<EntityPlayer, Timer> playerTimers = new ConcurrentHashMap<>();
    private int textRadarY = 0;

    public ToolTips() {
        super("ToolTips", "Several tweaks for tooltips.", Category.MISC, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static ToolTips getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ToolTips();
        }
        return INSTANCE;
    }

    @Override
    public void onUpdate() {
        if(fullNullCheck() || !shulkerSpy.getValue()) {
            return;
        }

        if(peek.getValue().getKey() != -1 && mc.currentScreen instanceof GuiContainer && Keyboard.isKeyDown(peek.getValue().getKey())) {
            Slot slot = ((GuiContainer)mc.currentScreen).getSlotUnderMouse();
            if(slot != null) {
                ItemStack stack = slot.getStack();
                if(stack != null && stack.getItem() instanceof ItemShulkerBox) {
                    displayInv(stack, null);
                }
            }
        }

        for(EntityPlayer player : mc.world.playerEntities) {
            if(player != null) {
                if(player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ItemShulkerBox) {
                    if(!EntityUtil.isFakePlayer(player) && (own.getValue() || !mc.player.equals(player))) {
                        ItemStack stack = player.getHeldItemMainhand();
                        spiedPlayers.put(player, stack);
                    }
                }
            }
        }
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        if(fullNullCheck() || !shulkerSpy.getValue() || !render.getValue()) {
            return;
        }

        int x = -4 + xOffset.getValue();
        int y = 10 + yOffset.getValue();
        this.textRadarY = 0;
        for(EntityPlayer player : mc.world.playerEntities) {
            if(spiedPlayers.get(player) != null) {
                if(player.getHeldItemMainhand() == null || !(player.getHeldItemMainhand().getItem() instanceof ItemShulkerBox)) {
                    Timer playerTimer = playerTimers.get(player);
                    if(playerTimer == null) {
                        Timer timer = new Timer();
                        timer.reset();
                        playerTimers.put(player, timer);
                    } else {
                        if(playerTimer.passedS(cooldown.getValue())) {
                            continue;
                        }
                    }
                } else if(player.getHeldItemMainhand().getItem() instanceof ItemShulkerBox) {
                    Timer playerTimer = playerTimers.get(player);
                    if(playerTimer != null) {
                        playerTimer.reset();
                        playerTimers.put(player, playerTimer);
                    }
                }

                ItemStack stack = spiedPlayers.get(player);
                renderShulkerToolTip(stack, x, y, player.getName());
                y += yPerPlayer.getValue() + 60;
                this.textRadarY = y - 10 - yOffset.getValue() + trOffset.getValue();
            }
        }
    }

    public int getTextRadarY() {
        return this.textRadarY;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void makeTooltip(ItemTooltipEvent event) {
        //TODO: wtf
    }

    @SubscribeEvent
    public void renderTooltip(RenderTooltipEvent.PostText event) {
        if (maps.getValue() && !event.getStack().isEmpty() && event.getStack().getItem() instanceof ItemMap) {
            final MapData mapData = Items.FILLED_MAP.getMapData(event.getStack(), mc.world);
            if (mapData != null) {
                GlStateManager.pushMatrix();
                GlStateManager.color(1.0f, 1.0f, 1.0f);
                RenderHelper.disableStandardItemLighting();
                mc.getTextureManager().bindTexture(MAP);
                Tessellator instance = Tessellator.getInstance();
                BufferBuilder buffer = instance.getBuffer();
                int n = 7;
                float n2 = 135.0f;
                float n3 = 0.5f;
                GlStateManager.translate(event.getX(), event.getY() - n2 * n3 - 5.0f, 0.0f);
                GlStateManager.scale(n3, n3, n3);
                buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(-n, n2, 0.0).tex(0.0, 1.0).endVertex();
                buffer.pos(n2, n2, 0.0).tex(1.0, 1.0).endVertex();
                buffer.pos(n2, -n, 0.0).tex(1.0, 0.0).endVertex();
                buffer.pos(-n, -n, 0.0).tex(0.0, 0.0).endVertex();
                instance.draw();
                mc.entityRenderer.getMapItemRenderer().renderMap(mapData, false);
                GlStateManager.enableLighting();
                GlStateManager.popMatrix();
            }
        }
    }

    public void renderShulkerToolTip(ItemStack stack, int x, int y, String name) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound != null && tagCompound.hasKey("BlockEntityTag", 10)) {
            NBTTagCompound blockEntityTag = tagCompound.getCompoundTag("BlockEntityTag");
            if (blockEntityTag.hasKey("Items", 9)) {
                GlStateManager.enableTexture2D();
                GlStateManager.disableLighting();
                GlStateManager.color(1.f, 1.f, 1.f, 1.f);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                mc.getTextureManager().bindTexture(SHULKER_GUI_TEXTURE);
                RenderUtil.drawTexturedRect(x, y, 0, 0, 176, 16, 500);
                RenderUtil.drawTexturedRect(x, y + 16, 0, 16, 176, 54 + invH.getValue(), 500);
                RenderUtil.drawTexturedRect(x, y + 16 + 54, 0, 160, 176, 8, 500);
                GlStateManager.disableDepth();
                Color color = new Color(0, 0, 0, 255);
                if(textColor.getValue()) {
                    color = new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue());
                }
                renderer.drawStringWithShadow(name == null ? stack.getDisplayName() : name, x + 8, y + 6, ColorUtil.toRGBA(color));
                GlStateManager.enableDepth();
                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.enableRescaleNormal();
                GlStateManager.enableColorMaterial();
                GlStateManager.enableLighting();
                NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(blockEntityTag, nonnulllist);

                for (int i = 0; i < nonnulllist.size(); i++) {
                    int iX = x + (i % 9) * (18) + 8;
                    int iY = y + (i / 9) * (18) + 18;
                    ItemStack itemStack = nonnulllist.get(i);
                    mc.getRenderItem().zLevel = 501;
                    itemRender.renderItemAndEffectIntoGUI(itemStack, iX, iY);
                    itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, iX, iY, null);
                    mc.getRenderItem().zLevel = 0.f;
                }

                GlStateManager.disableLighting();
                GlStateManager.disableBlend();
                GlStateManager.color(1.f, 1.f, 1.f, 1.0f);
            }
        }
    }

    public static void displayInv(ItemStack stack, String name) {
        try {
            Item item = stack.getItem();
            TileEntityShulkerBox entityBox = new TileEntityShulkerBox();
            ItemShulkerBox shulker = (ItemShulkerBox)item;
            entityBox.blockType = shulker.getBlock();
            entityBox.setWorld(mc.world);
            ItemStackHelper.loadAllItems(stack.getTagCompound().getCompoundTag("BlockEntityTag"), entityBox.items);
            entityBox.readFromNBT(stack.getTagCompound().getCompoundTag("BlockEntityTag"));
            entityBox.setCustomName(name == null ? stack.getDisplayName() : name);
            new Thread(() -> {
                try {
                    Thread.sleep(200L);
                } catch (InterruptedException ignored) {}
                mc.player.displayGUIChest(entityBox);
            }).start();
        } catch (Exception ignored) {}
    }


}
