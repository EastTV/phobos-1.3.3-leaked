package me.earth.phobos.features.modules.movement;

import me.earth.phobos.event.events.KeyEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class NoSlowDown extends Module {

    public Setting<Boolean> guiMove = register(new Setting("GuiMove", true));
    public Setting<Boolean> noSlow = register(new Setting("NoSlow", true));
    public Setting<Boolean> soulSand = register(new Setting("SoulSand", true));

    private static NoSlowDown INSTANCE = new NoSlowDown();

    private static KeyBinding[] keys = {
            mc.gameSettings.keyBindForward,
            mc.gameSettings.keyBindBack,
            mc.gameSettings.keyBindLeft,
            mc.gameSettings.keyBindRight,
            mc.gameSettings.keyBindJump,
            mc.gameSettings.keyBindSprint
    };

    public NoSlowDown() {
        super("NoSlowDown", "Prevents you from getting slowed down.", Category.MOVEMENT, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static NoSlowDown getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new NoSlowDown();
        }
        return INSTANCE;
    }

    @Override
    public void onUpdate() {
        if(guiMove.getValue()) {
            if (mc.currentScreen instanceof GuiOptions
                    || mc.currentScreen instanceof GuiVideoSettings
                    || mc.currentScreen instanceof GuiScreenOptionsSounds
                    || mc.currentScreen instanceof GuiContainer
                    || mc.currentScreen instanceof GuiIngameMenu) {
                for (KeyBinding bind : keys) {
                    KeyBinding.setKeyBindState(bind.getKeyCode(), Keyboard.isKeyDown(bind.getKeyCode()));
                }
            } else if (mc.currentScreen == null) {
                for (KeyBinding bind : keys) {
                    if (!Keyboard.isKeyDown(bind.getKeyCode())) {
                        KeyBinding.setKeyBindState(bind.getKeyCode(), false);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onInput(InputUpdateEvent event) {
        if (noSlow.getValue() && mc.player.isHandActive() && !mc.player.isRiding()) {
            event.getMovementInput().moveStrafe *= 5;
            event.getMovementInput().moveForward *= 5;
        }
    }

    @SubscribeEvent
    public void onKeyEvent(KeyEvent event) {
        if(guiMove.getValue() && event.getStage() == 0 && !(mc.currentScreen instanceof GuiChat)) {
            event.info = event.pressed;
        }
    }
}
