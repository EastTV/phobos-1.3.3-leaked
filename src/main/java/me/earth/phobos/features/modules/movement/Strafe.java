package me.earth.phobos.features.modules.movement;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.MoveEvent;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.modules.player.Freecam;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.EntityUtil;
import net.minecraft.init.MobEffects;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Strafe extends Module {

    private final Setting<Mode> mode = register(new Setting("Mode", Mode.NCP));
    private final Setting<Boolean> limiter = register(new Setting("SetGround", true));
    private final Setting<Boolean> limiter2 = register(new Setting("Bhop", false));
    private final Setting<Integer> specialMoveSpeed = register(new Setting("Speed", 100, 0, 150));
    private final Setting<Integer> potionSpeed = register(new Setting("Speed1", 130, 0, 150));
    private final Setting<Integer> potionSpeed2 = register(new Setting("Speed2", 125, 0, 150));
    private final Setting<Integer> acceleration = register(new Setting("Accel", 2149, 1000, 2500));
    private final Setting<Boolean> potion = register(new Setting("Potion", false));
    private final Setting<Boolean> step = register(new Setting("SetStep", true, v -> mode.getValue() == Mode.BHOP));

    private int stage = 1;
    private double moveSpeed;
    private double lastDist;
    private int cooldownHops = 0;

    public Strafe() {
        super("Strafe", "AirControl etc.", Category.MOVEMENT, true, false, false);
    }

    @Override
    public void onEnable() {
        moveSpeed = getBaseMoveSpeed();
    }

    @Override
    public void onDisable() {
        moveSpeed = 0D;
        stage = 2;
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if(event.getStage() == 0) {
            this.lastDist = Math.sqrt((mc.player.posX - mc.player.prevPosX) * (mc.player.posX - mc.player.prevPosX) + (mc.player.posZ - mc.player.prevPosZ) * (mc.player.posZ - mc.player.prevPosZ));
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if(event.getStage() != 0 || shouldReturn()) {
            return;
        }

        if (mode.getValue() == Mode.NCP) {
            doNCP(event);
        } else if(mode.getValue() == Mode.BHOP) {
            float moveForward = mc.player.movementInput.moveForward;
            float moveStrafe = mc.player.movementInput.moveStrafe;
            float rotationYaw = mc.player.rotationYaw;

            if (limiter2.getValue() && mc.player.onGround) {
                stage = 2;
            }

            if(limiter.getValue() && (round(mc.player.posY - (int) mc.player.posY, 3) == round(0.138D, 3))) {
                mc.player.motionY -= 0.13D;
                event.setY(event.getY() - 0.13D);
                mc.player.posY -= 0.13D;
            }

            if ((stage == 1) && (EntityUtil.isMoving())) {
                stage = 2;
                moveSpeed = (getMultiplier() * getBaseMoveSpeed() - 0.01D);
            } else if (stage == 2) {
                stage = 3;
                if (EntityUtil.isMoving()) {
                    mc.player.motionY = 0.4D;
                    event.setY(0.4D);
                    if (cooldownHops > 0) {
                        cooldownHops -= 1;
                    }
                    moveSpeed *= (acceleration.getValue() / 1000.0);
                }
            } else if (stage == 3) {
                stage = 4;
                double difference = 0.66D * (lastDist - getBaseMoveSpeed());
                moveSpeed = (lastDist - difference);
            } else {
                if ((mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0D, mc.player.motionY, 0D)).size() > 0) || (mc.player.collidedVertically)) {
                    stage = 1;
                }
                moveSpeed = (lastDist - lastDist / 159D);
            }

            moveSpeed = Math.max(moveSpeed, getBaseMoveSpeed());
            if ((moveForward == 0F) && (moveStrafe == 0F)) {
                event.setX(0D);
                event.setZ(0D);
                moveSpeed = 0D;
            } else if (moveForward != 0F) {
                if (moveStrafe >= 1F) {
                    rotationYaw += (moveForward > 0F ? -45F : 45F);
                    moveStrafe = 0F;
                } else if (moveStrafe <= -1F) {
                    rotationYaw += (moveForward > 0F ? 45F : -45F);
                    moveStrafe = 0F;
                }
                if (moveForward > 0F) {
                    moveForward = 1F;
                } else if (moveForward < 0F) {
                    moveForward = -1F;
                }
            }

            double motionX = Math.cos(Math.toRadians(rotationYaw + 90F));
            double motionZ = Math.sin(Math.toRadians(rotationYaw + 90F));

            if (cooldownHops == 0) {
                event.setX(moveForward * moveSpeed * motionX + moveStrafe * moveSpeed * motionZ);
                event.setZ(moveForward * moveSpeed * motionZ - moveStrafe * moveSpeed * motionX);
            }

            if(step.getValue()) {
                mc.player.stepHeight = 0.6F;
            }

            if ((moveForward == 0F) && (moveStrafe == 0F)) {
                event.setX(0D);
                event.setZ(0D);
            }
        }
    }

    private void doNCP(MoveEvent event) {
        if(!limiter.getValue() && mc.player.onGround) {
            stage = 2;
        }

        switch (this.stage) {
            case 0: {
                ++this.stage;
                this.lastDist = 0.0;
                break;
            }
            case 2: {
                double motionY = 0.40123128;
                if ((mc.player.moveForward != 0.0f || mc.player.moveStrafing != 0.0f) && mc.player.onGround) {
                    if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                        motionY += (mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1f;
                    }
                    event.setY(mc.player.motionY = motionY);
                    this.moveSpeed *= 2.149;
                    break;
                }
                break;
            }
            case 3: {
                this.moveSpeed = this.lastDist - 0.76 * (this.lastDist - this.getBaseMoveSpeed());
                break;
            }
            default: {
                if ((((limiter2.getValue() && mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0)).size() > 0)) || mc.player.collidedVertically) && this.stage > 0) {
                    this.stage = ((mc.player.moveForward != 0.0f || mc.player.moveStrafing != 0.0f) ? 1 : 0);
                }
                this.moveSpeed = this.lastDist - this.lastDist / 159.0;
                break;
            }
        }
        this.moveSpeed = Math.max(this.moveSpeed, this.getBaseMoveSpeed());
        double forward = mc.player.movementInput.moveForward;
        double strafe = mc.player.movementInput.moveStrafe;
        final double yaw = mc.player.rotationYaw;
        if (forward == 0.0 && strafe == 0.0) {
            event.setX(0.0);
            event.setZ(0.0);
        } else if (forward != 0.0 && strafe != 0.0) {
            forward *= Math.sin(0.7853981633974483);
            strafe *= Math.cos(0.7853981633974483);
        }
        event.setX((forward * this.moveSpeed * -Math.sin(Math.toRadians(yaw)) + strafe * this.moveSpeed * Math.cos(Math.toRadians(yaw))) * 0.99);
        event.setZ((forward * this.moveSpeed * Math.cos(Math.toRadians(yaw)) - strafe * this.moveSpeed * -Math.sin(Math.toRadians(yaw))) * 0.99);
        ++this.stage;
    }

    public static double getBaseMoveSpeed() {
        double baseSpeed = 0.272;
        if (mc.player.isPotionActive(MobEffects.SPEED)) {
            final int amplifier = Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SPEED)).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * amplifier;
        }
        return baseSpeed;
    }

    private float getMultiplier() { //TODO: with Amplifier
        float baseSpeed = specialMoveSpeed.getValue();
        if (potion.getValue() && mc.player.isPotionActive(MobEffects.SPEED)) {
            final int amplifier = Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SPEED)).getAmplifier() + 1;
            if(amplifier >= 2) {
                baseSpeed = potionSpeed2.getValue();
            } else {
                baseSpeed = potionSpeed.getValue();
            }
        }
        return baseSpeed / 100.0f;
    }

    private boolean shouldReturn() {
        return Phobos.moduleManager.isModuleEnabled(Freecam.class) || Phobos.moduleManager.isModuleEnabled(Phase.class) || Phobos.moduleManager.isModuleEnabled(ElytraFlight.class) || Phobos.moduleManager.isModuleEnabled(Flight.class);
    }

    @Override
    public String getDisplayInfo() {
        if(mode.getValue() != Mode.NONE) {
            if(mode.getValue() == Mode.NCP) {
                return mode.currentEnumName().toUpperCase();
            } else {
                return mode.currentEnumName();
            }
        }
        return null;
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bigDecimal = new BigDecimal(value).setScale(places, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

    public enum Mode {
        NONE,
        NCP,
        BHOP
    }
}

