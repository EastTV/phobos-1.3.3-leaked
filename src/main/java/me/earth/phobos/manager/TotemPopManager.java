package me.earth.phobos.manager;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.Feature;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.modules.client.Notifications;
import me.earth.phobos.util.TextUtil;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TotemPopManager extends Feature {

    private Notifications notifications;

    private Map<EntityPlayer, Integer> poplist = new ConcurrentHashMap<>();
    private Set<EntityPlayer> toAnnounce = new HashSet<>();

    public void onUpdate() {
        if(notifications.totemAnnounce.passedMs(notifications.delay.getValue()) && notifications.isOn() && notifications.totemPops.getValue()) {
            for(EntityPlayer player : toAnnounce) {
                if(player == null) {
                    continue;
                }
                Command.sendMessage(TextUtil.RED + player.getName() + " popped " + TextUtil.GREEN + getTotemPops(player) + TextUtil.RED + " Totem" + (getTotemPops(player) == 1 ? "" : "s") + ".",notifications.totemNoti.getValue());
                toAnnounce.remove(player);
                notifications.totemAnnounce.reset();
                break;
            }
        }
    }

    public void onLogout() {
        onOwnLogout(notifications.clearOnLogout.getValue());
    }

    public void init() {
        this.notifications = Phobos.moduleManager.getModuleByClass(Notifications.class);
    }

    public void onTotemPop(EntityPlayer player) {
        popTotem(player);
        if(!player.equals(mc.player)) {
            toAnnounce.add(player);
            notifications.totemAnnounce.reset();
        }
    }

    public void onDeath(EntityPlayer player) {
        if(getTotemPops(player) != 0 && !player.equals(mc.player) && notifications.isOn() && notifications.totemPops.getValue()) {
            Command.sendMessage(TextUtil.RED + player.getName() + " died after popping " + TextUtil.GREEN + getTotemPops(player) + TextUtil.RED + " Totem" + (getTotemPops(player) == 1 ? "" : "s") + ".",notifications.totemNoti.getValue());
            toAnnounce.remove(player);
        }
        resetPops(player);
    }

    public void onLogout(EntityPlayer player, boolean clearOnLogout) {
        if(clearOnLogout) {
            resetPops(player);
        }
    }

    public void onOwnLogout(boolean clearOnLogout) {
        if(clearOnLogout) {
            clearList();
        }
    }

    public void clearList() {
        poplist = new ConcurrentHashMap<>();
    }

    public void resetPops(EntityPlayer player) {
        setTotemPops(player, 0);
    }

    public void popTotem(EntityPlayer player) {
        poplist.merge(player, 1, Integer::sum);
    }

    public void setTotemPops(EntityPlayer player, int amount) {
        poplist.put(player, amount);
    }

    public int getTotemPops(EntityPlayer player) {
        Integer pops = poplist.get(player);
        if(pops == null) {
            return 0;
        }
        return pops;
    }

    public String getTotemPopString(EntityPlayer player) {
        return TextUtil.WHITE + (getTotemPops(player) <= 0 ? "" : "-" + getTotemPops(player) + " ");
    }
}
