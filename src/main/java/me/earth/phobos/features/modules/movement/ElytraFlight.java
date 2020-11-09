package me.earth.phobos.features.modules.movement;

import me.earth.phobos.event.events.MoveEvent;
import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.MathUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ElytraFlight extends Module {

    public Setting<Mode> mode = register(new Setting("Mode", Mode.FLY));
    public Setting<Integer> devMode = register(new Setting("Type", 2, 1, 3, v -> mode.getValue() == Mode.BYPASS || mode.getValue() == Mode.BETTER, "EventMode"));
    public Setting<Float> speed = register(new Setting("Speed", 1.0f, 0.0f, 10.0f, v -> mode.getValue() != Mode.FLY && mode.getValue() != Mode.BOOST && mode.getValue() != Mode.BETTER && mode.getValue() != Mode.OHARE, "The Speed."));
    public Setting<Float> vSpeed = register(new Setting("VSpeed", 0.3f, 0.0f, 10.0f, v -> mode.getValue() == Mode.BETTER || mode.getValue() == Mode.OHARE, "Vertical Speed"));
    public Setting<Float> hSpeed = register(new Setting("HSpeed", 1.0f, 0.0f, 10.0f, v -> mode.getValue() == Mode.BETTER || mode.getValue() == Mode.OHARE, "Horizontal Speed"));
    public Setting<Float> glide = register(new Setting("Glide", 0.0001f, 0.0f, 0.2f, v -> mode.getValue() == Mode.BETTER, "Glide Speed"));
    public Setting<Float> tooBeeSpeed = register(new Setting("TooBeeSpeed", 1.8000000268220901f, 1.0f, 2.0f, v -> mode.getValue() == Mode.TOOBEE, "Speed for flight on 2b2t"));
    public Setting<Boolean> autoStart = register(new Setting("AutoStart", true));
    public Setting<Boolean> disableInLiquid = register(new Setting("NoLiquid", true));
    public Setting<Boolean> infiniteDura = register(new Setting("InfiniteDura", false));
    public Setting<Boolean> noKick = register(new Setting("NoKick", false, v -> mode.getValue() == Mode.PACKET));
    public Setting<Boolean> allowUp = register(new Setting("AllowUp", true, v -> mode.getValue() == Mode.BETTER));

    private static ElytraFlight INSTANCE = new ElytraFlight();
    private final Timer timer = new Timer();
    private Double posX;
    private Double flyHeight;
    private Double posZ;

    public ElytraFlight() {
        super("ElytraFlight", "Makes Elytra Flight better.", Category.MOVEMENT, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static ElytraFlight getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ElytraFlight();
        }
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        if (mode.getValue() == Mode.BETTER && !autoStart.getValue() && devMode.getValue() == 1) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
        }
        flyHeight = null;
        posX = null;
        posZ = null;
    }

    @Override
    public String getDisplayInfo() {
        return mode.currentEnumName();
    }

    @Override
    public void onUpdate() {
        if (mode.getValue() == Mode.BYPASS && devMode.getValue() == 1) {
            if (mc.player.isElytraFlying()) {
                mc.player.motionX = 0;
                mc.player.motionY = -0.0001;
                mc.player.motionZ = 0;
                double forwardInput = mc.player.movementInput.moveForward;
                double strafeInput = mc.player.movementInput.moveStrafe;
                double[] result = forwardStrafeYaw(forwardInput, strafeInput, mc.player.rotationYaw);
                double forward = result[0];
                double strafe = result[1];
                double yaw = result[2];
                if (!(forwardInput == 0.0 && strafeInput == 0.0)) {
                    mc.player.motionX = (forward * speed.getValue() * Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * speed.getValue() * Math.sin(Math.toRadians(yaw + 90.0f)));
                    mc.player.motionZ = (forward * speed.getValue() * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * speed.getValue() * Math.cos(Math.toRadians(yaw + 90.0f)));
                }

                if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.motionY = -1.0;
                }
            }
        }
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer && mode.getValue() == Mode.TOOBEE) {
            final CPacketPlayer packet = event.getPacket();
            if (mc.player.isElytraFlying()) {
                if (!mc.player.movementInput.jump) {
                    if (packet.pitch < 1) packet.pitch = 1;
                }
            }
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if (mode.getValue() == Mode.OHARE) {
            ItemStack itemstack = mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (itemstack.getItem() == Items.ELYTRA && ItemElytra.isUsable(itemstack)) {
                if (mc.player.isElytraFlying()) {
                    event.setY(mc.gameSettings.keyBindJump.isKeyDown() ? vSpeed.getValue() : mc.gameSettings.keyBindSneak.isKeyDown() ? -vSpeed.getValue() : 0);
                    mc.player.addVelocity(0, mc.gameSettings.keyBindJump.isKeyDown() ? vSpeed.getValue() : mc.gameSettings.keyBindSneak.isKeyDown() ? -vSpeed.getValue() : 0, 0);
                    mc.player.rotateElytraX = 0;
                    mc.player.rotateElytraY = 0;
                    mc.player.rotateElytraZ = 0;
                    mc.player.moveVertical = mc.gameSettings.keyBindJump.isKeyDown() ? vSpeed.getValue() : mc.gameSettings.keyBindSneak.isKeyDown() ? -vSpeed.getValue() : 0;
                    double forward = mc.player.movementInput.moveForward;
                    double strafe = mc.player.movementInput.moveStrafe;
                    float yaw = mc.player.rotationYaw;
                    if ((forward == 0.0D) && (strafe == 0.0D)) {
                        event.setX(0);
                        event.setZ(0);
                    } else {
                        if (forward != 0.0D) {
                            if (strafe > 0.0D) {
                                yaw += (forward > 0.0D ? -45 : 45);
                            } else if (strafe < 0.0D) {
                                yaw += (forward > 0.0D ? 45 : -45);
                            }
                            strafe = 0.0D;
                            if (forward > 0.0D) {
                                forward = 1.0D;
                            } else if (forward < 0.0D) {
                                forward = -1.0D;
                            }
                        }
                        final double cos = Math.cos(Math.toRadians(yaw + 90.0F));
                        final double sin = Math.sin(Math.toRadians(yaw + 90.0F));
                        event.setX((forward * hSpeed.getValue() * cos + strafe * hSpeed.getValue() * sin));
                        event.setZ((forward * hSpeed.getValue() * sin - strafe * hSpeed.getValue() * cos));
                    }
                }
            }
        } else if (event.getStage() == 0 && mode.getValue() == Mode.BYPASS && devMode.getValue() == 3) {
            if (mc.player.isElytraFlying()) {
                event.setX(0.0);
                event.setY(-0.0001);
                event.setZ(0.0);
                double forwardInput = mc.player.movementInput.moveForward;
                double strafeInput = mc.player.movementInput.moveStrafe;
                double[] result = forwardStrafeYaw(forwardInput, strafeInput, mc.player.rotationYaw);
                double forward = result[0];
                double strafe = result[1];
                double yaw = result[2];
                if (!(forwardInput == 0.0 && strafeInput == 0.0)) {
                    event.setX(forward * speed.getValue() * Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * speed.getValue() * Math.sin(Math.toRadians(yaw + 90.0f)));
                    event.setY(forward * speed.getValue() * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * speed.getValue() * Math.cos(Math.toRadians(yaw + 90.0f)));
                }

                if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    event.setY(-1.0);
                }
            }
        } else if (mode.getValue() == Mode.TOOBEE) {
            if (!mc.player.isElytraFlying()) return;
            if (!mc.player.movementInput.jump) {
                if (mc.player.movementInput.sneak) {
                    mc.player.motionY = -(this.tooBeeSpeed.getValue() / 2);
                    event.setY(-(this.speed.getValue() / 2));
                } else if (event.getY() != -0.000101d) {
                    event.setY(-0.000101d);
                    mc.player.motionY = -0.000101d;
                }
            } else {
                return;
            }
        }
        setMoveSpeed(event, tooBeeSpeed.getValue());
    }

    private void setMoveSpeed(MoveEvent event, double speed) {
        double forward = mc.player.movementInput.moveForward;
        double strafe = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;
        if (forward == 0.0 && strafe == 0.0) {
            event.setX(0.0);
            event.setZ(0.0);
            mc.player.motionX = 0;
            mc.player.motionZ = 0;
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += ((forward > 0.0) ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += ((forward > 0.0) ? 45 : -45);
                }
                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }
            final double x = forward * speed * -Math.sin(Math.toRadians(yaw)) + strafe * speed * Math.cos(Math.toRadians(yaw));

            final double z = forward * speed * Math.cos(Math.toRadians(yaw)) - strafe * speed * -Math.sin(Math.toRadians(yaw));

            event.setX(x);
            event.setZ(z);
            mc.player.motionX = x;
            mc.player.motionZ = z;
        }
    }

    @Override
    public void onTick() {
        if (!mc.player.isElytraFlying()) return;
        switch (mode.getValue()) {
            case BOOST:
                if (mc.player.isInWater()) {
                    mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                    return;
                }

                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.motionY += 0.08;
                } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.motionY -= 0.04;
                }


                if (mc.gameSettings.keyBindForward.isKeyDown()) {
                    float yaw = (float) Math
                            .toRadians(mc.player.rotationYaw);
                    mc.player.motionX -= MathHelper.sin(yaw) * 0.05F;
                    mc.player.motionZ += MathHelper.cos(yaw) * 0.05F;
                } else if (mc.gameSettings.keyBindBack.isKeyDown()) {
                    float yaw = (float) Math
                            .toRadians(mc.player.rotationYaw);
                    mc.player.motionX += MathHelper.sin(yaw) * 0.05F;
                    mc.player.motionZ -= MathHelper.cos(yaw) * 0.05F;
                }
                break;
            case FLY:
                mc.player.capabilities.isFlying = true;
                break;
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
            return;
        }

        switch (event.getStage()) {
            case 0:
                if (disableInLiquid.getValue() && (mc.player.isInWater() || mc.player.isInLava())) {
                    if (mc.player.isElytraFlying()) {
                        mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                    }
                    return;
                }

                if (autoStart.getValue()) {
                    if (mc.gameSettings.keyBindJump.isKeyDown() && !mc.player.isElytraFlying()) {
                        if (mc.player.motionY < 0) {
                            if (timer.passedMs(250)) {
                                mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                                timer.reset();
                            }
                        }
                    }
                }

                if (mode.getValue() == Mode.BETTER) {
                    final double[] dir = MathUtil.directionSpeed(devMode.getValue() == 1 ? speed.getValue() : this.hSpeed.getValue());
                    switch (devMode.getValue()) {
                        case 1:
                            mc.player.setVelocity(0, 0, 0);
                            mc.player.jumpMovementFactor = speed.getValue();

                            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                                mc.player.motionY += speed.getValue();
                            }

                            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                                mc.player.motionY -= speed.getValue();
                            }

                            if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                                mc.player.motionX = dir[0];
                                mc.player.motionZ = dir[1];
                            } else {
                                mc.player.motionX = 0;
                                mc.player.motionZ = 0;
                            }
                            break;
                        case 2:
                            if (mc.player.isElytraFlying()) {
                                if (flyHeight == null) {
                                    flyHeight = mc.player.posY;
                                }
                            } else {
                                flyHeight = null;
                                return;
                            }

                            if (this.noKick.getValue()) {
                                flyHeight -= this.glide.getValue();
                            }

                            posX = 0d;
                            posZ = 0d;

                            if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                                posX = dir[0];
                                posZ = dir[1];
                            }

                            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                                flyHeight = mc.player.posY + this.vSpeed.getValue();
                            }

                            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                                flyHeight = mc.player.posY - this.vSpeed.getValue();
                            }

                            mc.player.setPosition(mc.player.posX + posX, flyHeight, mc.player.posZ + posZ);
                            mc.player.setVelocity(0, 0, 0);
                            break;
                        case 3:
                            if (mc.player.isElytraFlying()) {
                                if (flyHeight == null
                                        || (posX == null || posX == 0)
                                        || (posZ == null || posZ == 0)) {
                                    flyHeight = mc.player.posY;
                                    posX = mc.player.posX;
                                    posZ = mc.player.posZ;
                                }
                            } else {
                                flyHeight = null;
                                posX = null;
                                posZ = null;
                                return;
                            }

                            if (this.noKick.getValue()) {
                                flyHeight -= glide.getValue();
                            }

                            if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                                posX += dir[0];
                                posZ += dir[1];
                            }

                            if (allowUp.getValue() && mc.gameSettings.keyBindJump.isKeyDown()) {
                                flyHeight = mc.player.posY + this.vSpeed.getValue() / 10;
                            }

                            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                                flyHeight = mc.player.posY - this.vSpeed.getValue() / 10;
                            }

                            mc.player.setPosition(posX, flyHeight, posZ);
                            mc.player.setVelocity(0, 0, 0);
                            break;
                    }
                }

                final double rotationYaw = Math.toRadians(mc.player.rotationYaw);
                if (mc.player.isElytraFlying()) {
                    switch (mode.getValue()) {
                        case VANILLA:
                            final float speedScaled = speed.getValue() * 0.05f; // 5/100 of original value

                            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                                mc.player.motionY += speedScaled;
                            }

                            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                                mc.player.motionY -= speedScaled;
                            }

                            if (mc.gameSettings.keyBindForward.isKeyDown()) {
                                mc.player.motionX -= Math.sin(rotationYaw) * speedScaled;
                                mc.player.motionZ += Math.cos(rotationYaw) * speedScaled;
                            }

                            if (mc.gameSettings.keyBindBack.isKeyDown()) {
                                mc.player.motionX += Math.sin(rotationYaw) * speedScaled;
                                mc.player.motionZ -= Math.cos(rotationYaw) * speedScaled;
                            }
                            break;
                        case PACKET:
                            freezePlayer(mc.player);
                            runNoKick(mc.player);

                            final double[] directionSpeedPacket = MathUtil.directionSpeed(speed.getValue());

                            if (mc.player.movementInput.jump) {
                                mc.player.motionY = speed.getValue();
                            }

                            if (mc.player.movementInput.sneak) {
                                mc.player.motionY = -speed.getValue();
                            }

                            if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                                mc.player.motionX = directionSpeedPacket[0];
                                mc.player.motionZ = directionSpeedPacket[1];
                            }

                            mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                            mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                            break;
                        case BYPASS:
                            if (devMode.getValue() == 3) {
                                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                                    mc.player.motionY = 0.02f;
                                }

                                if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                                    mc.player.motionY = -0.2f;
                                }

                                if (mc.player.ticksExisted % 8 == 0 && mc.player.posY <= 240) {
                                    mc.player.motionY = 0.02f;
                                }

                                mc.player.capabilities.isFlying = true;
                                mc.player.capabilities.setFlySpeed(0.025f);

                                final double[] directionSpeedBypass = MathUtil.directionSpeed(0.52f);
                                if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                                    mc.player.motionX = directionSpeedBypass[0];
                                    mc.player.motionZ = directionSpeedBypass[1];
                                } else {
                                    mc.player.motionX = 0;
                                    mc.player.motionZ = 0;
                                }
                            }
                            break;
                    }
                }

                if (infiniteDura.getValue()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                }
                break;
            case 1:
                if (infiniteDura.getValue()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                }
                break;
        }
    }

    private double[] forwardStrafeYaw(double forward, double strafe, double yaw) {
        double[] result = new double[3];
        result[0] = forward;
        result[1] = strafe;
        result[2] = yaw;
        if (!(forward == 0.0 && strafe == 0.0)) {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    result[2] += (forward > 0.0 ? -45 : 45);
                } else if (strafe < 0.0) {
                    result[2] += (forward > 0.0 ? 45 : -45);
                }

                result[1] = 0.0;
                if (forward > 0.0) {
                    result[0] = 1.0;
                } else if (forward < 0.0) {
                    result[0] = -1.0;
                }
            }
        }
        return result;
    }

    private void freezePlayer(EntityPlayer player) {
        player.motionX = 0;
        player.motionY = 0;
        player.motionZ = 0;
    }

    private void runNoKick(EntityPlayer player) {
        if (this.noKick.getValue() && !player.isElytraFlying() && player.ticksExisted % 4 == 0) {
            player.motionY = -0.04f;
        }
    }

    @Override
    public void onDisable() {
        if (fullNullCheck() || mc.player.capabilities.isCreativeMode) return;
        mc.player.capabilities.isFlying = false;
    }

    public enum Mode {
        VANILLA,
        PACKET,
        BOOST,
        FLY,
        BYPASS,
        BETTER,
        OHARE,
        TOOBEE
    }
}
