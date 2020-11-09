package me.earth.phobos.features.modules.player;

import com.mojang.authlib.GameProfile;
import me.earth.phobos.Phobos;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FakePlayer extends Module {

    private Setting<Boolean> copyInv = register(new Setting("CopyInv", true));
    public Setting<Boolean> multi = register(new Setting("Multi", false));
    private Setting<Integer> players = register(new Setting("Players", 1, 1, 9, v -> multi.getValue(), "Amount of other players."));

    private static final String[] fitInfo = {"fdee323e-7f0c-4c15-8d1c-0f277442342a", "Fit"};
    public static final String[][] phobosInfo = { {"8af022c8-b926-41a0-8b79-2b544ff00fcf", "3arthqu4ke", "3", "0"}, {"0aa3b04f-786a-49c8-bea9-025ee0dd1e85", "zb0b", "-3", "0"}, {"19bf3f1f-fe06-4c86-bea5-3dad5df89714", "3vt", "0", "-3"}, {"e47d6571-99c2-415b-955e-c4bc7b55941b", "Phobos_eu", "0", "3"}, {"b01f9bc1-cb7c-429a-b178-93d771f00926", "bakpotatisen", "6", "0"}, {"b232930c-c28a-4e10-8c90-f152235a65c5", "948", "-6", "0"}, {"ace08461-3db3-4579-98d3-390a67d5645b", "Browswer", "0", "-6"}, {"5bead5b0-3bab-460d-af1d-7929950f40c2", "fsck", "0", "6"}, {"78ee2bd6-64c4-45f0-96e5-0b6747ba7382", "Fit", "0", "9"}, {"78ee2bd6-64c4-45f0-96e5-0b6747ba7382", "deathcurz0", "0", "-9"}};
    public List<Integer> fakePlayerIdList = new ArrayList<>();
    private final List<EntityOtherPlayerMP> fakeEntities = new ArrayList<>();

    private static FakePlayer INSTANCE = new FakePlayer();

    public FakePlayer() {
        super("FakePlayer", "Spawns in a fake player", Module.Category.PLAYER, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static FakePlayer getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new FakePlayer();
        }
        return INSTANCE;
    }

    @Override
    public void onLoad() {
        this.disable();
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            this.disable();
            return;
        }

        fakePlayerIdList = new ArrayList<>();
        if(multi.getValue()) {
            int amount = 0;
            int entityId = -101;
            for (String[] data : phobosInfo) {
                addFakePlayer(data[0], data[1], entityId, Integer.parseInt(data[2]), Integer.parseInt(data[3]));
                amount++;
                if(amount >= players.getValue()) {
                    return;
                }
                entityId -= amount;
            }
        } else {
            addFakePlayer(fitInfo[0], fitInfo[1], -100, 0, 0);
        }
    }

    @Override
    public void onDisable() {
        if (fullNullCheck()) return;
        for(int id : fakePlayerIdList) {
            mc.world.removeEntityFromWorld(id);
        }
    }

    @Override
    public void onLogout() {
        if(this.isOn()) {
            this.disable();
        }
    }

    private void addFakePlayer(String uuid, String name, int entityId, int offsetX, int offsetZ) {
        GameProfile profile = new GameProfile(UUID.fromString(uuid), name);
        final EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP(mc.world, profile);
        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        fakePlayer.posX = fakePlayer.posX + offsetX;
        fakePlayer.posZ = fakePlayer.posZ + offsetZ;
        if(copyInv.getValue()) {
            for (PotionEffect potionEffect : Phobos.potionManager.getOwnPotions()) {
                fakePlayer.addPotionEffect(potionEffect);
            }
            fakePlayer.inventory.copyInventory(mc.player.inventory);
        }
        fakePlayer.setHealth(mc.player.getHealth() + mc.player.getAbsorptionAmount());
        fakeEntities.add(fakePlayer);
        mc.world.addEntityToWorld(entityId, fakePlayer);
        fakePlayerIdList.add(entityId);
    }
}
