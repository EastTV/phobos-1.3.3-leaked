package me.earth.phobos.features.modules.misc;

import me.earth.phobos.features.modules.Module;
import me.earth.phobos.util.PlayerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MobOwner extends Module {

    private final Map<Entity, String> owners = new HashMap<>();
    private final Map<Entity, UUID> toLookUp = new ConcurrentHashMap<>();
    private final List<Entity> lookedUp = new ArrayList<>();

    public MobOwner() {
        super("MobOwner", "Shows you who owns mobs.", Category.MISC, false, false, false);
    }

    @Override
    public void onUpdate() {
        if(fullNullCheck()) {
            return;
        }

        if (PlayerUtil.timer.passedS(5)) {
            for(Map.Entry<Entity, UUID> entry : toLookUp.entrySet()) {
                Entity entity = entry.getKey();
                UUID uuid = entry.getValue();
                if(uuid != null) {
                    EntityPlayer owner = mc.world.getPlayerEntityByUUID(uuid);
                    if(owner != null) {
                        owners.put(entity, owner.getName());
                        lookedUp.add(entity);
                        continue;
                    } else {
                        try {
                            String name = PlayerUtil.getNameFromUUID(uuid);
                            if(name != null) {
                                owners.put(entity, name);
                                lookedUp.add(entity);
                            }
                        } catch (Exception e) {
                            lookedUp.add(entity);
                            toLookUp.remove(entry);
                        }
                    }
                    PlayerUtil.timer.reset();
                    break;
                } else {
                    lookedUp.add(entity);
                    toLookUp.remove(entry);
                }
            }
        }

        for(Entity entity : mc.world.getLoadedEntityList()) {
            if(!entity.getAlwaysRenderNameTag()) {
                if (entity instanceof EntityTameable) {
                    EntityTameable tameableEntity = (EntityTameable) entity;
                    if (tameableEntity.isTamed() && tameableEntity.getOwnerId() != null) {
                        if (owners.get(tameableEntity) != null) {
                            tameableEntity.setAlwaysRenderNameTag(true);
                            tameableEntity.setCustomNameTag(owners.get(tameableEntity));
                        } else if (!lookedUp.contains(entity)) {
                            toLookUp.put(tameableEntity, tameableEntity.getOwnerId());
                        }
                    }
                } else if (entity instanceof AbstractHorse) {
                    AbstractHorse tameableEntity = (AbstractHorse) entity;
                    if (tameableEntity.isTame() && tameableEntity.getOwnerUniqueId() != null) {
                        if (owners.get(tameableEntity) != null) {
                            tameableEntity.setAlwaysRenderNameTag(true);
                            tameableEntity.setCustomNameTag(owners.get(tameableEntity));
                        } else if (!lookedUp.contains(entity)) {
                            toLookUp.put(tameableEntity, tameableEntity.getOwnerUniqueId());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        for (Entity entity : mc.world.loadedEntityList) {
            if(entity instanceof EntityTameable || entity instanceof AbstractHorse) {
                try {
                    entity.setAlwaysRenderNameTag(false);
                } catch (Exception ignored) {}
            }
        }
    }
}
