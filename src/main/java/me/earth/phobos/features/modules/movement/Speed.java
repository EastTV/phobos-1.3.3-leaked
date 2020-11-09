package me.earth.phobos.features.modules.movement;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.ClientEvent;
import me.earth.phobos.event.events.MoveEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.BlockUtil;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovementInput;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

public class Speed extends Module {

    public Setting<Mode> mode = register(new Setting("Mode", Mode.INSTANT));
    public Setting<Boolean> strafeJump = register(new Setting("Jump", false, v -> mode.getValue() == Mode.INSTANT));
    public Setting<Boolean> noShake = register(new Setting("NoShake", true, v -> mode.getValue() != Mode.INSTANT));
    public Setting<Boolean> useTimer = register(new Setting("UseTimer", false, v -> mode.getValue() != Mode.INSTANT));

    private static Speed INSTANCE = new Speed();

    private double highChainVal = 0.0;
    private double lowChainVal = 0.0;
    private boolean oneTime = false;
    public double startY = 0.0;
    public boolean antiShake = false;
    private double bounceHeight = 0.4;
    private float move = 0.26f;

    public Speed() {
        super("Speed", "Makes you faster", Category.MOVEMENT, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static Speed getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Speed();
        }
        return INSTANCE;
    }

    public enum Mode {
        INSTANT,
        ONGROUND,
        ACCEL,
        BOOST
    }

    private boolean shouldReturn() {
        return Phobos.moduleManager.isModuleEnabled("Freecam") || Phobos.moduleManager.isModuleEnabled("Phase") || Phobos.moduleManager.isModuleEnabled("ElytraFlight") || Phobos.moduleManager.isModuleEnabled("Strafe") || Phobos.moduleManager.isModuleEnabled("Flight");
    }

    @Override
    public void onUpdate() {
        if(shouldReturn() || mc.player.isSneaking() || mc.player.isInWater() || mc.player.isInLava()) {
            return;
        }

        switch(mode.getValue()) {
            case BOOST:
                doBoost();
                break;
            case ACCEL:
                doAccel();
                break;
            case ONGROUND:
                doOnground();
                break;
            default:
        }
    }

    private void doBoost() {
        bounceHeight = 0.4;
        move = 0.26f;
        if (mc.player.onGround) {
            startY = mc.player.posY;
        }

        if (EntityUtil.getEntitySpeed((Entity)mc.player) <= 1.0) {
            this.lowChainVal = 1.0;
            this.highChainVal = 1.0;
        }

        if (EntityUtil.isEntityMoving((Entity)mc.player) && !mc.player.collidedHorizontally && !BlockUtil.isBlockAboveEntitySolid((Entity)mc.player) && BlockUtil.isBlockBelowEntitySolid((Entity)mc.player)) {
            oneTime = true;
            antiShake = noShake.getValue() && mc.player.getRidingEntity() == null;
            final Random random = new Random();
            final boolean rnd = random.nextBoolean();
            if (mc.player.posY >= startY + bounceHeight) {
                mc.player.motionY = -bounceHeight;
                ++this.lowChainVal;
                if (this.lowChainVal == 1.0) {
                    move = 0.075f;
                }
                if (this.lowChainVal == 2.0) {
                    move = 0.15f;
                }
                if (this.lowChainVal == 3.0) {
                    move = 0.175f;
                }
                if (this.lowChainVal == 4.0) {
                    move = 0.2f;
                }
                if (this.lowChainVal == 5.0) {
                    move = 0.225f;
                }
                if (this.lowChainVal == 6.0) {
                    move = 0.25f;
                }
                if (this.lowChainVal >= 7.0) {
                    move = 0.27895f;
                }
                if (useTimer.getValue()) {
                    Phobos.timerManager.setTimer(1.0f);
                }
            }
            if (mc.player.posY == startY) {
                mc.player.motionY = bounceHeight;
                ++this.highChainVal;
                if (this.highChainVal == 1.0) {
                    move = 0.075f;
                }
                if (this.highChainVal == 2.0) {
                    move = 0.175f;
                }
                if (this.highChainVal == 3.0) {
                    move = 0.325f;
                }
                if (this.highChainVal == 4.0) {
                    move = 0.375f;
                }
                if (this.highChainVal == 5.0) {
                    move = 0.4f;
                }
                if (this.highChainVal >= 6.0) {
                    move = 0.43395f;
                }
                if (useTimer.getValue()) {
                    if (rnd) {
                        Phobos.timerManager.setTimer(1.3f);
                    } else {
                        Phobos.timerManager.setTimer(1.0f);
                    }
                }
            }
            EntityUtil.moveEntityStrafe(move, (Entity)mc.player);
        } else {
            if (oneTime) {
                mc.player.motionY = -0.1;
                oneTime = false;
            }
            highChainVal = 0.0;
            lowChainVal = 0.0;
            antiShake = false;
            this.speedOff();
        }
    }

    private void doAccel() {
        bounceHeight = 0.4;
        move = 0.26f;
        if (mc.player.onGround) {
            startY = mc.player.posY;
        }
        if (EntityUtil.getEntitySpeed((Entity)mc.player) <= 1.0) {
            this.lowChainVal = 1.0;
            this.highChainVal = 1.0;
        }
        if (EntityUtil.isEntityMoving((Entity)mc.player) && !mc.player.collidedHorizontally && !BlockUtil.isBlockAboveEntitySolid((Entity)mc.player) && BlockUtil.isBlockBelowEntitySolid((Entity)mc.player)) {
            oneTime = true;
            antiShake = noShake.getValue() && mc.player.getRidingEntity() == null;
            final Random random = new Random();
            final boolean rnd = random.nextBoolean();
            if (mc.player.posY >= startY + bounceHeight) {
                mc.player.motionY = -bounceHeight;
                ++this.lowChainVal;
                if (this.lowChainVal == 1.0) {
                    move = 0.075f;
                }
                if (this.lowChainVal == 2.0) {
                    move = 0.175f;
                }
                if (this.lowChainVal == 3.0) {
                    move = 0.275f;
                }
                if (this.lowChainVal == 4.0) {
                    move = 0.35f;
                }
                if (this.lowChainVal == 5.0) {
                    move = 0.375f;
                }
                if (this.lowChainVal == 6.0) {
                    move = 0.4f;
                }
                if (this.lowChainVal == 7.0) {
                    move = 0.425f;
                }
                if (this.lowChainVal == 8.0) {
                    move = 0.45f;
                }
                if (this.lowChainVal == 9.0) {
                    move = 0.475f;
                }
                if (this.lowChainVal == 10.0) {
                    move = 0.5f;
                }
                if (this.lowChainVal == 11.0) {
                    move = 0.5f;
                }
                if (this.lowChainVal == 12.0) {
                    move = 0.525f;
                }
                if (this.lowChainVal == 13.0) {
                    move = 0.525f;
                }
                if (this.lowChainVal == 14.0) {
                    move = 0.535f;
                }
                if (this.lowChainVal == 15.0) {
                    move = 0.535f;
                }
                if (this.lowChainVal == 16.0) {
                    move = 0.545f;
                }
                if (this.lowChainVal >= 17.0) {
                    move = 0.545f;
                }
                if (useTimer.getValue()) {
                    Phobos.timerManager.setTimer(1.0f);
                }
            }
            if (mc.player.posY == startY) {
                mc.player.motionY = bounceHeight;
                ++this.highChainVal;
                if (this.highChainVal == 1.0) {
                    move = 0.075f;
                }
                if (this.highChainVal == 2.0) {
                    move = 0.175f;
                }
                if (this.highChainVal == 3.0) {
                    move = 0.375f;
                }
                if (this.highChainVal == 4.0) {
                    move = 0.6f;
                }
                if (this.highChainVal == 5.0) {
                    move = 0.775f;
                }
                if (this.highChainVal == 6.0) {
                    move = 0.825f;
                }
                if (this.highChainVal == 7.0) {
                    move = 0.875f;
                }
                if (this.highChainVal == 8.0) {
                    move = 0.925f;
                }
                if (this.highChainVal == 9.0) {
                    move = 0.975f;
                }
                if (this.highChainVal == 10.0) {
                    move = 1.05f;
                }
                if (this.highChainVal == 11.0) {
                    move = 1.1f;
                }
                if (this.highChainVal == 12.0) {
                    move = 1.1f;
                }
                if (this.highChainVal == 13.0) {
                    move = 1.15f;
                }
                if (this.highChainVal == 14.0) {
                    move = 1.15f;
                }
                if (this.highChainVal == 15.0) {
                    move = 1.175f;
                }
                if (this.highChainVal == 16.0) {
                    move = 1.175f;
                }
                if (this.highChainVal >= 17.0) {
                    move = 1.175f;
                }
                if (useTimer.getValue()) {
                    if (rnd) {
                        Phobos.timerManager.setTimer(1.3f);
                    } else {
                        Phobos.timerManager.setTimer(1.0f);
                    }
                }
            }
            EntityUtil.moveEntityStrafe(move, (Entity)mc.player);
        }
        else {
            if (oneTime) {
                mc.player.motionY = -0.1;
                oneTime = false;
            }
            antiShake = false;
            this.highChainVal = 0.0;
            this.lowChainVal = 0.0;
            this.speedOff();
        }
    }

    private void doOnground() {
        bounceHeight = 0.4;
        move = 0.26f;
        if (mc.player.onGround) {
            startY = mc.player.posY;
        }
        if (EntityUtil.getEntitySpeed((Entity)mc.player) <= 1.0) {
            this.lowChainVal = 1.0;
            this.highChainVal = 1.0;
        }
        if (EntityUtil.isEntityMoving((Entity)mc.player) && !mc.player.collidedHorizontally && !BlockUtil.isBlockAboveEntitySolid((Entity)mc.player) && BlockUtil.isBlockBelowEntitySolid((Entity)mc.player)) {
            oneTime = true;
            antiShake = noShake.getValue() && mc.player.getRidingEntity() == null;
            final Random random = new Random();
            final boolean rnd = random.nextBoolean();
            if (mc.player.posY >= startY + bounceHeight) {
                mc.player.motionY = -bounceHeight;
                ++this.lowChainVal;
                if (this.lowChainVal == 1.0) {
                    move = 0.075f;
                }
                if (this.lowChainVal == 2.0) {
                    move = 0.175f;
                }
                if (this.lowChainVal == 3.0) {
                    move = 0.275f;
                }
                if (this.lowChainVal == 4.0) {
                    move = 0.35f;
                }
                if (this.lowChainVal == 5.0) {
                    move = 0.375f;
                }
                if (this.lowChainVal == 6.0) {
                    move = 0.4f;
                }
                if (this.lowChainVal == 7.0) {
                    move = 0.425f;
                }
                if (this.lowChainVal == 8.0) {
                    move = 0.45f;
                }
                if (this.lowChainVal == 9.0) {
                    move = 0.475f;
                }
                if (this.lowChainVal == 10.0) {
                    move = 0.5f;
                }
                if (this.lowChainVal == 11.0) {
                    move = 0.5f;
                }
                if (this.lowChainVal == 12.0) {
                    move = 0.525f;
                }
                if (this.lowChainVal == 13.0) {
                    move = 0.525f;
                }
                if (this.lowChainVal == 14.0) {
                    move = 0.535f;
                }
                if (this.lowChainVal == 15.0) {
                    move = 0.535f;
                }
                if (this.lowChainVal == 16.0) {
                    move = 0.545f;
                }
                if (this.lowChainVal >= 17.0) {
                    move = 0.545f;
                }
                if (useTimer.getValue()) {
                    Phobos.timerManager.setTimer(1.0f);
                }
            }
            if (mc.player.posY == startY) {
                mc.player.motionY = bounceHeight;
                ++this.highChainVal;
                if (this.highChainVal == 1.0) {
                    move = 0.075f;
                }
                if (this.highChainVal == 2.0) {
                    move = 0.175f;
                }
                if (this.highChainVal == 3.0) {
                    move = 0.375f;
                }
                if (this.highChainVal == 4.0) {
                    move = 0.6f;
                }
                if (this.highChainVal == 5.0) {
                    move = 0.775f;
                }
                if (this.highChainVal == 6.0) {
                    move = 0.825f;
                }
                if (this.highChainVal == 7.0) {
                    move = 0.875f;
                }
                if (this.highChainVal == 8.0) {
                    move = 0.925f;
                }
                if (this.highChainVal == 9.0) {
                    move = 0.975f;
                }
                if (this.highChainVal == 10.0) {
                    move = 1.05f;
                }
                if (this.highChainVal == 11.0) {
                    move = 1.1f;
                }
                if (this.highChainVal == 12.0) {
                    move = 1.1f;
                }
                if (this.highChainVal == 13.0) {
                    move = 1.15f;
                }
                if (this.highChainVal == 14.0) {
                    move = 1.15f;
                }
                if (this.highChainVal == 15.0) {
                    move = 1.175f;
                }
                if (this.highChainVal == 16.0) {
                    move = 1.175f;
                }
                if (this.highChainVal >= 17.0) {
                    move = 1.2f;
                }
                if (useTimer.getValue()) {
                    if (rnd) {
                        Phobos.timerManager.setTimer(1.3f);
                    } else {
                        Phobos.timerManager.setTimer(1.0f);
                    }
                }
            }
            EntityUtil.moveEntityStrafe(move, (Entity)mc.player);
        }
        else {
            if (oneTime) {
                mc.player.motionY = -0.1;
                oneTime = false;
            }
            antiShake = false;
            this.highChainVal = 0.0;
            this.lowChainVal = 0.0;
            this.speedOff();
        }
    }

    @Override
    public void onDisable() {
        if(mode.getValue() == Mode.ONGROUND || mode.getValue() == Mode.BOOST) {
            mc.player.motionY = -0.1;
        }
        Phobos.timerManager.setTimer(1.0f);
        highChainVal = 0.0;
        lowChainVal = 0.0;
        antiShake = false;
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        if(event.getStage() == 2) {
            if(event.getSetting().equals(this.mode)) {
                if(mode.getPlannedValue() == Mode.INSTANT) {
                    mc.player.motionY = -0.1;
                }
            }
        }
    }

    @Override
    public String getDisplayInfo() {
        return mode.currentEnumName();
    }

    @SubscribeEvent
    public void onMode(MoveEvent event) {
        if(!shouldReturn() && event.getStage() == 0 && mode.getValue() == Mode.INSTANT && !nullCheck() && !(mc.player.isSneaking() || mc.player.isInWater() || mc.player.isInLava())) {
            if (mc.player.movementInput.moveForward != 0.0f || mc.player.movementInput.moveStrafe != 0.0f) {
                if (mc.player.onGround && strafeJump.getValue()) {
                    mc.player.motionY = 0.4;
                    event.setY(0.4);
                }
                final MovementInput movementInput = mc.player.movementInput;
                float moveForward = movementInput.moveForward;
                float moveStrafe = movementInput.moveStrafe;
                float rotationYaw = mc.player.rotationYaw;

                if (moveForward == 0.0 && moveStrafe == 0.0) {
                    event.setX(0.0);
                    event.setZ(0.0);
                } else {
                    if (moveForward != 0.0) {
                        if (moveStrafe > 0.0) {
                            rotationYaw = rotationYaw + (moveForward > 0.0 ? -45 : 45);
                        } else if (moveStrafe < 0.0) {
                            rotationYaw = rotationYaw + (moveForward > 0.0 ? 45 : -45);
                        }
                        moveStrafe = 0.0f;
                        moveForward = moveForward == 0.0f ? moveForward : (moveForward > 0.0 ? 1.0f : -1.0f);
                    }
                    moveStrafe = moveStrafe == 0.0f ? moveStrafe : (moveStrafe > 0.0 ? 1.0f : -1.0f);

                    event.setX(moveForward * EntityUtil.getMaxSpeed() * Math.cos(Math.toRadians(rotationYaw + 90.0f)) + moveStrafe * EntityUtil.getMaxSpeed() * Math.sin(Math.toRadians(rotationYaw + 90.0f)));
                    event.setZ(moveForward * EntityUtil.getMaxSpeed() * Math.sin(Math.toRadians(rotationYaw + 90.0f)) - moveStrafe * EntityUtil.getMaxSpeed() * Math.cos(Math.toRadians(rotationYaw + 90.0f)));
                }
            }
        }
    }

    private void speedOff() {
        final float yaw = (float)Math.toRadians(mc.player.rotationYaw);
        if (BlockUtil.isBlockAboveEntitySolid((Entity)mc.player)) {
            if (mc.gameSettings.keyBindForward.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown() && mc.player.onGround) {
                mc.player.motionX -= MathUtil.sin(yaw) * 0.15;
                mc.player.motionZ += MathUtil.cos(yaw) * 0.15;
            }
        } else if (mc.player.collidedHorizontally) {
            if (mc.gameSettings.keyBindForward.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown() && mc.player.onGround) {
                mc.player.motionX -= MathUtil.sin(yaw) * 0.03;
                mc.player.motionZ += MathUtil.cos(yaw) * 0.03;
            }
        } else if (!BlockUtil.isBlockBelowEntitySolid((Entity)mc.player)) {
            if (mc.gameSettings.keyBindForward.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown() && mc.player.onGround) {
                mc.player.motionX -= MathUtil.sin(yaw) * 0.03;
                mc.player.motionZ += MathUtil.cos(yaw) * 0.03;
            }
        } else {
            mc.player.motionX = 0.0;
            mc.player.motionZ = 0.0;
        }
    }
}
