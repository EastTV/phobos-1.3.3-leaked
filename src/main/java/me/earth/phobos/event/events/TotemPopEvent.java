package me.earth.phobos.event.events;

import me.earth.phobos.event.EventStage;
import net.minecraft.entity.player.EntityPlayer;

public class TotemPopEvent extends EventStage {

    private EntityPlayer entity;

    public TotemPopEvent(EntityPlayer entity) {
        super();
        this.entity = entity;
    }

    public EntityPlayer getEntity() {
        return entity;
    }

}
