package me.earth.phobos.features.modules.render;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.BossInfoClient;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static net.minecraft.client.gui.GuiBossOverlay.GUI_BARS_TEXTURES;

public class NoRender extends Module {

    public Setting<Boolean> fire = register(new Setting("Fire", false, "Removes the portal overlay."));
    public Setting<Boolean> portal = register(new Setting("Portal", false, "Removes the portal overlay."));
    public Setting<Boolean> pumpkin = register(new Setting("Pumpkin", false, "Removes the pumpkin overlay."));
    public Setting<Boolean> totemPops = register(new Setting("TotemPop", false, "Removes the Totem overlay."));
    public Setting<Boolean> items = register(new Setting("Items", false, "Removes items on the ground."));
    public Setting<Boolean> nausea = register(new Setting("Nausea", false, "Removes Portal Nausea."));
    public Setting<Boolean> hurtcam = register(new Setting("HurtCam", false, "Removes shaking after taking damage."));
    public Setting<Fog> fog = register(new Setting("Fog", Fog.NONE, "Removes Fog."));
    public Setting<Boolean> noWeather = register(new Setting("Weather", false, "AntiWeather"));
    public Setting<Boss> boss = register(new Setting("BossBars", Boss.NONE, "Modifies the bossbars."));
    public Setting<Float> scale = register(new Setting("Scale", 0.0f, 0.5f, 1.0f, v -> boss.getValue() == Boss.MINIMIZE || boss.getValue() != Boss.STACK, "Scale of the bars."));
    public Setting<Boolean> bats = register(new Setting("Bats", false, "Removes bats."));
    public Setting<NoArmor> noArmor = register(new Setting("NoArmor", NoArmor.NONE, "Doesnt Render Armor on players."));
    public Setting<Skylight> skylight = register(new Setting("Skylight", Skylight.NONE));
    public Setting<Boolean> barriers = register(new Setting("Barriers", false, "Barriers"));

    public enum Skylight {
        NONE,
        WORLD,
        ENTITY,
        ALL
    }

    private static NoRender INSTANCE = new NoRender();

    public NoRender() {
        super("NoRender", "Allows you to stop rendering stuff", Category.RENDER, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if(items.getValue()) {
            mc.world.loadedEntityList.stream().filter(EntityItem.class::isInstance).map(EntityItem.class::cast).forEach(Entity::setDead);
        }

        if (noWeather.getValue() && mc.world.isRaining()) {
            mc.world.setRainStrength(0);
        }
    }

    public void doVoidFogParticles(int posX, int posY, int posZ)
    {
        int i = 32;
        Random random = new Random();
        ItemStack itemstack = mc.player.getHeldItemMainhand();
        boolean flag = !barriers.getValue() || (mc.playerController.getCurrentGameType() == GameType.CREATIVE && !itemstack.isEmpty() && itemstack.getItem() == Item.getItemFromBlock(Blocks.BARRIER));
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int j = 0; j < 667; ++j)
        {
            this.showBarrierParticles(posX, posY, posZ, 16, random, flag, blockpos$mutableblockpos);
            this.showBarrierParticles(posX, posY, posZ, 32, random, flag, blockpos$mutableblockpos);
        }
    }

    public void showBarrierParticles(int x, int y, int z, int offset, Random random, boolean holdingBarrier, BlockPos.MutableBlockPos pos)
    {
        int i = x + mc.world.rand.nextInt(offset) - mc.world.rand.nextInt(offset);
        int j = y + mc.world.rand.nextInt(offset) - mc.world.rand.nextInt(offset);
        int k = z + mc.world.rand.nextInt(offset) - mc.world.rand.nextInt(offset);
        pos.setPos(i, j, k);
        IBlockState iblockstate = mc.world.getBlockState(pos);
        iblockstate.getBlock().randomDisplayTick(iblockstate, mc.world, pos, random);

        if (!holdingBarrier && iblockstate.getBlock() == Blocks.BARRIER)
        {
            mc.world.spawnParticle(EnumParticleTypes.BARRIER, (double)((float)i + 0.5F), (double)((float)j + 0.5F), (double)((float)k + 0.5F), 0.0D, 0.0D, 0.0D, new int[0]);
        }
    }

    @SubscribeEvent
    public void onRenderPre(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.BOSSINFO && boss.getValue() != Boss.NONE) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderPost(RenderGameOverlayEvent.Post event) {
        if(event.getType() == RenderGameOverlayEvent.ElementType.BOSSINFO && boss.getValue() != Boss.NONE) {
            if (boss.getValue() == Boss.MINIMIZE) {
                Map<UUID, BossInfoClient> map = mc.ingameGUI.getBossOverlay().mapBossInfos;
                if (map == null) return;
                ScaledResolution scaledresolution = new ScaledResolution(mc);
                int i = scaledresolution.getScaledWidth();
                int j = 12;
                for (Map.Entry<UUID, BossInfoClient> entry : map.entrySet()) {
                    BossInfoClient info = entry.getValue();
                    String text = info.getName().getFormattedText();
                    int k = (int) ((i / scale.getValue()) / 2 - 91);
                    GL11.glScaled(scale.getValue(), scale.getValue(), 1);
                    if (!event.isCanceled()) {
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        mc.getTextureManager().bindTexture(GUI_BARS_TEXTURES);
                        mc.ingameGUI.getBossOverlay().render(k, j, info);
                        mc.fontRenderer.drawStringWithShadow(text, (float) ((i / scale.getValue()) / 2 - mc.fontRenderer.getStringWidth(text) / 2), (float) (j - 9), 16777215);
                    }
                    GL11.glScaled(1d / scale.getValue(), 1d / scale.getValue(), 1);
                    j += 10 + mc.fontRenderer.FONT_HEIGHT;
                }
            } else if (boss.getValue() == Boss.STACK) {
                Map<UUID, BossInfoClient> map = mc.ingameGUI.getBossOverlay().mapBossInfos;
                HashMap<String, Pair<BossInfoClient, Integer>> to = new HashMap<>();
                for (Map.Entry<UUID, BossInfoClient> entry : map.entrySet()) {
                    String s = entry.getValue().getName().getFormattedText();
                    if (to.containsKey(s)) {
                        Pair<BossInfoClient, Integer> p = to.get(s);
                        p = new Pair<>(p.getKey(), p.getValue() + 1);
                        to.put(s, p);
                    } else {
                        Pair<BossInfoClient, Integer> p = new Pair<>(entry.getValue(), 1);
                        to.put(s, p);
                    }
                }
                ScaledResolution scaledresolution = new ScaledResolution(mc);
                int i = scaledresolution.getScaledWidth();
                int j = 12;
                for (Map.Entry<String, Pair<BossInfoClient, Integer>> entry : to.entrySet()) {
                    String text = entry.getKey();
                    BossInfoClient info = entry.getValue().getKey();
                    int a = entry.getValue().getValue();
                    text += " x" + a;
                    int k = (int) ((i / scale.getValue()) / 2 - 91);
                    GL11.glScaled(scale.getValue(), scale.getValue(), 1);
                    if (!event.isCanceled()) {
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        mc.getTextureManager().bindTexture(GUI_BARS_TEXTURES);
                        mc.ingameGUI.getBossOverlay().render(k, j, info);
                        mc.fontRenderer.drawStringWithShadow(text, (float) ((i / scale.getValue()) / 2 - mc.fontRenderer.getStringWidth(text) / 2), (float) (j - 9), 16777215);
                    }
                    GL11.glScaled(1d / scale.getValue(), 1d / scale.getValue(), 1);
                    j += 10 + mc.fontRenderer.FONT_HEIGHT;
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Pre<?> event) {
        if(bats.getValue() && event.getEntity() instanceof EntityBat) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundAtEntityEvent event) {
        if(bats.getValue() && event.getSound().equals(SoundEvents.ENTITY_BAT_AMBIENT)
                || event.getSound().equals(SoundEvents.ENTITY_BAT_DEATH)
                || event.getSound().equals(SoundEvents.ENTITY_BAT_HURT)
                || event.getSound().equals(SoundEvents.ENTITY_BAT_LOOP)
                || event.getSound().equals(SoundEvents.ENTITY_BAT_TAKEOFF)) {
            event.setVolume(0.f);
            event.setPitch(0.f);
            event.setCanceled(true);
        }
    }

    public static NoRender getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new NoRender();
        }
        return INSTANCE;
    }

    public enum Fog {
        NONE,
        AIR,
        NOFOG
    }

    public enum Boss {
        NONE,
        REMOVE,
        STACK,
        MINIMIZE
    }

    public enum NoArmor {
        NONE,
        ALL,
        HELMET
    }

    public static class Pair<T, S> {

        private T key;
        private S value;

        public Pair(T key, S value) {
            this.key = key;
            this.value = value;
        }

        public T getKey() {
            return key;
        }

        public S getValue() {
            return value;
        }

        public void setKey(T key) {
            this.key = key;
        }

        public void setValue(S value) {
            this.value = value;
        }
    }
}
