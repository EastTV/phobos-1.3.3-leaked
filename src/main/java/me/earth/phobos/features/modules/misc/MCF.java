package me.earth.phobos.features.modules.misc;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.command.Command;
import me.earth.phobos.features.gui.PhobosGui;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Bind;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.TextUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class MCF extends Module {

    private final Setting<Boolean> middleClick = register(new Setting("MiddleClick", true));
    private final Setting<Boolean> keyboard = register(new Setting("Keyboard", false));
    private final Setting<Bind> key = register(new Setting("KeyBind", new Bind(-1), v -> keyboard.getValue()));

    private boolean clicked = false;

    public MCF() {
        super("MCF", "Middleclick Friends.", Category.MISC, true, false, false);
    }

    @Override
    public void onUpdate() {
        if(Mouse.isButtonDown(2)) {
            if(!clicked && middleClick.getValue() && mc.currentScreen == null) {
                onClick();
            }
            clicked = true;
        } else {
            clicked = false;
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (keyboard.getValue() && Keyboard.getEventKeyState() && !(mc.currentScreen instanceof PhobosGui) && key.getValue().getKey() == Keyboard.getEventKey()) {
            onClick();
        }
    }

    private void onClick() {
        RayTraceResult result = mc.objectMouseOver;
        if(result != null && result.typeOfHit == RayTraceResult.Type.ENTITY) {
            Entity entity = result.entityHit;
            if(entity instanceof EntityPlayer) {
                if(Phobos.friendManager.isFriend(entity.getName())) {
                    Phobos.friendManager.removeFriend(entity.getName());
                    Command.sendMessage(TextUtil.RED + entity.getName() + TextUtil.RESET + " unfriended.");
                } else {
                    Phobos.friendManager.addFriend(entity.getName());
                    Command.sendMessage(TextUtil.AQUA + entity.getName() + TextUtil.RESET + " friended.");
                }
            }
        }
        clicked = true;
    }
}
