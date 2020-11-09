package me.earth.phobos.features.modules.movement;

import me.earth.phobos.Phobos;
import me.earth.phobos.event.events.ClientEvent;
import me.earth.phobos.event.events.MoveEvent;
import me.earth.phobos.event.events.PacketEvent;
import me.earth.phobos.event.events.PushEvent;
import me.earth.phobos.event.events.UpdateWalkingPlayerEvent;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.util.EntityUtil;
import me.earth.phobos.util.MathUtil;
import me.earth.phobos.util.Timer;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Flight extends Module {

    public Setting<Mode> mode = register(new Setting("Mode", Mode.PACKET));
    public Setting<Boolean> better = register(new Setting("Better", false, v -> mode.getValue() == Mode.PACKET));
    public Setting<Format> format = register(new Setting("Format", Format.DAMAGE, v -> mode.getValue() == Mode.DAMAGE));
    public Setting<PacketMode> type = register(new Setting("Type", PacketMode.Y, v -> mode.getValue() == Mode.PACKET));
    public Setting<Boolean> phase = register(new Setting("Phase", false, v -> mode.getValue() == Mode.PACKET && better.getValue()));
    public Setting<Float> speed = register(new Setting("Speed", 0.1f, 0.0f, 10.0f, v -> mode.getValue() == Mode.PACKET || mode.getValue() == Mode.DESCEND || mode.getValue() == Mode.DAMAGE, "The speed."));
    public Setting<Boolean> noKick = register(new Setting("NoKick", false, v -> mode.getValue() == Mode.PACKET || mode.getValue() == Mode.DAMAGE));
    public Setting<Boolean> noClip = register(new Setting("NoClip", false, v -> mode.getValue() == Mode.DAMAGE));
    public Setting<Boolean> groundSpoof = register(new Setting("GroundSpoof", false, v -> mode.getValue() == Mode.SPOOF));
    public Setting<Boolean> antiGround = register(new Setting("AntiGround", true, v -> mode.getValue() == Mode.SPOOF));
    public Setting<Integer> cooldown = register(new Setting("Cooldown", 1, v -> mode.getValue() == Mode.DESCEND));
    public Setting<Boolean> ascend = register(new Setting("Ascend", false, v -> mode.getValue() == Mode.DESCEND));

    private List<CPacketPlayer> packets = new ArrayList<>();
    private int teleportId = 0;
    private static Flight INSTANCE = new Flight();
    private int counter = 0;
    private final Fly flySwitch = new Fly();
    private double moveSpeed, lastDist;
    private int level;
    private Timer delayTimer = new Timer();

    public Flight() {
        super("Flight", "Makes you fly.", Category.MOVEMENT, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static Flight getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Flight();
        }
        return INSTANCE;
    }

    @SubscribeEvent
    public void onTickEvent(TickEvent.ClientTickEvent event) {
        if(fullNullCheck() || mode.getValue() != Mode.DESCEND) {
            return;
        }

        if (event.phase == TickEvent.Phase.END) {
            if (!mc.player.isElytraFlying()) {
                if (counter < 1) {
                    counter += cooldown.getValue();
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 0.03, mc.player.posZ, true));
                } else {
                    counter -= 1;
                }
            }
        } else {
            if (ascend.getValue()) {
                mc.player.motionY = speed.getValue();
            } else {
                mc.player.motionY = -speed.getValue();
            }
        }
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            return;
        }

        if(mode.getValue() == Mode.PACKET) {
            teleportId = 0;
            packets.clear();
            final CPacketPlayer bounds = new CPacketPlayer.Position(mc.player.posX, 0, mc.player.posZ, mc.player.onGround);
            packets.add(bounds);
            mc.player.connection.sendPacket(bounds);
        }

        if(mode.getValue() == Mode.CREATIVE) {
            mc.player.capabilities.isFlying = true;
            if (mc.player.capabilities.isCreativeMode) return;
            mc.player.capabilities.allowFlying = true;
        }

        if(mode.getValue() == Mode.SPOOF) {
            flySwitch.enable();
        }

        if(mode.getValue() == Mode.DAMAGE) {
            level = 0;
            if (format.getValue() == Format.PACKET) {
                if (mc.world != null) {
                    this.teleportId = 0;
                    this.packets.clear();
                    final CPacketPlayer bounds = new CPacketPlayer.Position(mc.player.posX, mc.player.posY <= 10 ? 255 : 1, mc.player.posZ, mc.player.onGround);
                    this.packets.add(bounds);
                    mc.player.connection.sendPacket(bounds);
                }
            }
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if(mode.getValue() == Mode.DAMAGE) {
            if (format.getValue() == Format.DAMAGE) {
                if (event.getStage() == 0) {
                    mc.player.motionY = 0;
                    double motionY = 0.42f;
                    if (mc.player.onGround) {
                        if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                            motionY += (mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1f;
                        }
                        Phobos.positionManager.setPlayerPosition(mc.player.posX, mc.player.motionY = motionY, mc.player.posZ);
                        //event.getLocation().setY(mc.player.motionY = motionY);
                        this.moveSpeed *= 2.149;
                    }
                }
                if (mc.player.ticksExisted % 2 == 0) {
                    mc.player.setPosition(mc.player.posX, mc.player.posY + MathUtil.getRandom(1.2354235325235235E-14, 1.2354235325235233E-13), mc.player.posZ);
                }
                if (mc.gameSettings.keyBindJump.isKeyDown())
                    mc.player.motionY += speed.getValue() / 2;
                if (mc.gameSettings.keyBindSneak.isKeyDown())
                    mc.player.motionY -= speed.getValue() / 2;
            }
            if (format.getValue() == Format.NORMAL) {
                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.motionY = speed.getValue();
                } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.motionY = -speed.getValue();
                } else {
                    mc.player.motionY = 0;
                }
                if (noKick.getValue()) {
                    if (mc.player.ticksExisted % 5 == 0) {
                        Phobos.positionManager.setPlayerPosition(mc.player.posX, mc.player.posY - 0.03125D, mc.player.posZ, true);
                        //mc.player.onGround = true; TODO: ???
                        //event.getLocation().setY(event.getLocation().getY() - 0.03125D);
                        //mc.player.onGround = true;
                        //event.getLocation().setOnGround(true);
                    }
                }
                double[] dir = EntityUtil.forward(speed.getValue());
                mc.player.motionX = dir[0];
                mc.player.motionZ = dir[1];
            }
            if (format.getValue() == Format.PACKET) {
                if (this.teleportId <= 0) {
                    final CPacketPlayer bounds = new CPacketPlayer.Position(mc.player.posX, mc.player.posY <= 10 ? 255 : 1, mc.player.posZ, mc.player.onGround);
                    this.packets.add(bounds);
                    mc.player.connection.sendPacket(bounds);
                    return;
                }
                mc.player.setVelocity(0,0,0);
                double posY = -0.00000001;
                if (!mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()){
                    if (EntityUtil.isMoving()) {
                        for (double x = 0.0625; x < this.speed.getValue(); x += 0.262) {
                            final double[] dir = EntityUtil.forward(x);
                            mc.player.setVelocity(dir[0], posY, dir[1]);
                            move(dir[0], posY, dir[1]);
                        }
                    }
                } else {
                    if (mc.gameSettings.keyBindJump.isKeyDown()) {
                        for (int i = 0; i <= 3; i++) {
                            //mc.player.getEntityBoundingBox().offset(0, 0.10000545, 0);
                            mc.player.setVelocity(0, mc.player.ticksExisted % 20 == 0 ? -0.04f : 0.062f * i, 0);
                            move(0, mc.player.ticksExisted % 20 == 0 ? -0.04f : 0.062f * i, 0);
                        }
                    } else if (mc.gameSettings.keyBindSneak.isKeyDown()){
                        for (int i = 0; i <= 3; i++) {
                            mc.player.setVelocity(0, posY - 0.0625 * i, 0);
                            move(0, posY - 0.0625 * i, 0);
                        }
                    }
                }
            }
            if (format.getValue() == Format.SLOW) {
                double posX = mc.player.posX;
                double posY = mc.player.posY;
                double posZ = mc.player.posZ;
                boolean ground = mc.player.onGround;
                mc.player.setVelocity(0,0,0);
                if (!mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) {
                    double[] dir = EntityUtil.forward(0.0625);
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(posX + dir[0], posY, posZ + dir[1], ground));
                    mc.player.setPositionAndUpdate(posX + dir[0], posY, posZ + dir[1]);

                } else {
                    if (mc.gameSettings.keyBindJump.isKeyDown()) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(posX, posY + 0.0625, posZ, ground));
                        mc.player.setPositionAndUpdate(posX, posY + 0.0625, posZ);

                    } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(posX, posY - 0.0625, posZ, ground));
                        mc.player.setPositionAndUpdate(posX, posY - 0.0625, posZ);
                    }
                }
                mc.player.connection.sendPacket(new CPacketPlayer.Position(posX + mc.player.motionX, mc.player.posY <= 10 ? 255 : 1, posZ + mc.player.motionZ, ground));
            }

            if (format.getValue() == Format.DELAY) {
                if (delayTimer.passedMs(1000))
                    delayTimer.reset();
                if (delayTimer.passedMs(600)) {
                    mc.player.setVelocity(0,0,0);
                    return;
                }
                if (this.teleportId <= 0) {
                    final CPacketPlayer bounds = new CPacketPlayer.Position(mc.player.posX, mc.player.posY <= 10 ? 255 : 1, mc.player.posZ, mc.player.onGround);
                    this.packets.add(bounds);
                    mc.player.connection.sendPacket(bounds);
                    return;
                }
                mc.player.setVelocity(0,0,0);
                double posY = -0.00000001;
                if (!mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()){
                    if (EntityUtil.isMoving()) {
                        final double[] dir = EntityUtil.forward(0.2);
                        mc.player.setVelocity(dir[0], posY, dir[1]);
                        move(dir[0], posY, dir[1]);
                    }
                } else {
                    if (mc.gameSettings.keyBindJump.isKeyDown()) {
                        mc.player.setVelocity(0, 0.062f, 0);
                        move(0, 0.062f, 0);
                    } else if (mc.gameSettings.keyBindSneak.isKeyDown()){
                        mc.player.setVelocity(0, 0.0625, 0);
                        move(0, 0.0625, 0);
                    }
                }
            }

            if (noClip.getValue()) {
                mc.player.noClip = true;
            }
        }

        if(event.getStage() == 0) {
            if(mode.getValue() == Mode.CREATIVE) {
                mc.player.capabilities.setFlySpeed(speed.getValue());
                mc.player.capabilities.isFlying = true;
                if (mc.player.capabilities.isCreativeMode) return;
                mc.player.capabilities.allowFlying = true;
            }

            if (mode.getValue() == Mode.VANILLA) {
                mc.player.setVelocity(0, 0, 0);
                mc.player.jumpMovementFactor = speed.getValue();
                if (noKick.getValue()) {
                    if (mc.player.ticksExisted % 4 == 0) {
                        mc.player.motionY = -0.04f;
                    }
                }
                final double[] dir = MathUtil.directionSpeed(speed.getValue());
                if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                    mc.player.motionX = dir[0];
                    mc.player.motionZ = dir[1];
                } else {
                    mc.player.motionX = 0;
                    mc.player.motionZ = 0;
                }
                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    if (noKick.getValue()) {
                        mc.player.motionY = mc.player.ticksExisted % 20 == 0 ? -0.04f : speed.getValue();
                    } else {
                        mc.player.motionY += speed.getValue();
                    }
                }
                if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.motionY -= speed.getValue();
                }
            }

            if (mode.getValue() == Mode.PACKET && !better.getValue()) {
                doNormalPacketFly();
            }

            if(mode.getValue() == Mode.PACKET && better.getValue()) {
                doBetterPacketFly();
            }
        }
    }

    private void doNormalPacketFly() {
        if (teleportId <= 0) {
            final CPacketPlayer bounds = new CPacketPlayer.Position(mc.player.posX, 0, mc.player.posZ, mc.player.onGround);
            packets.add(bounds);
            mc.player.connection.sendPacket(bounds);
            return;
        }

        mc.player.setVelocity(0, 0, 0);

        if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(-0.0625d, 0, -0.0625d)).isEmpty()) {
            double ySpeed = 0;

            if (mc.gameSettings.keyBindJump.isKeyDown()) {

                if (noKick.getValue()) {
                    ySpeed = mc.player.ticksExisted % 20 == 0 ? -0.04f : 0.062f;
                } else {
                    ySpeed = 0.062f;
                }
            } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                ySpeed = -0.062d;
            } else {
                ySpeed = mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(-0.0625d, -0.0625d, -0.0625d)).isEmpty() ? (mc.player.ticksExisted % 4 == 0) ? (noKick.getValue() ? -0.04f : 0.0f) : 0.0f : 0.0f;
            }

            final double[] directionalSpeed = MathUtil.directionSpeed(speed.getValue());

            if (mc.gameSettings.keyBindJump.isKeyDown() || mc.gameSettings.keyBindSneak.isKeyDown() || mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown()) {
                if (directionalSpeed[0] != 0.0d || directionalSpeed[1] != 0.0d) { // || ySpeed != 0.0d
                    if (mc.player.movementInput.jump && (mc.player.moveStrafing != 0 || mc.player.moveForward != 0)) {
                        mc.player.setVelocity(0, 0, 0);
                        move(0, 0, 0);
                        for (int i = 0; i <= 3; i++) {
                            mc.player.setVelocity(0, ySpeed * i, 0);
                            move(0, ySpeed * i, 0);
                        }
                    } else {
                        if (mc.player.movementInput.jump) {
                            mc.player.setVelocity(0, 0, 0);
                            move(0, 0, 0);
                            for (int i = 0; i <= 3; i++) {
                                mc.player.setVelocity(0, ySpeed * i, 0);
                                move(0, ySpeed * i, 0);
                            }
                        } else {
                            for (int i = 0; i <= 2; i++) {
                                mc.player.setVelocity(directionalSpeed[0] * i, ySpeed * i, directionalSpeed[1] * i);
                                move(directionalSpeed[0] * i, ySpeed * i, directionalSpeed[1] * i);
                            }
                        }
                    }
                }
            } else {
                if (noKick.getValue()) {
                    if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(-0.0625d, -0.0625d, -0.0625d)).isEmpty()) {
                        mc.player.setVelocity(0, (mc.player.ticksExisted % 2 == 0) ? 0.04f : -0.04f, 0);
                        move(0, (mc.player.ticksExisted % 2 == 0) ? 0.04f : -0.04f, 0);
                    }
                }
            }
        }
    }

    private void doBetterPacketFly() {
        if (this.teleportId <= 0) {
            final CPacketPlayer bounds = new CPacketPlayer.Position(mc.player.posX, 10000, mc.player.posZ, mc.player.onGround);
            this.packets.add(bounds);
            mc.player.connection.sendPacket(bounds);
            return;
        }

        mc.player.setVelocity(0, 0, 0);

        if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(-0.0625d, 0, -0.0625d)).isEmpty()) {
            double ySpeed = 0;

            if (mc.gameSettings.keyBindJump.isKeyDown()) {

                if (this.noKick.getValue()) {
                    ySpeed = mc.player.ticksExisted % 20 == 0 ? -0.04f : 0.062f;
                } else {
                    ySpeed = 0.062f;
                }
            } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                ySpeed = -0.062d;
            } else {
                ySpeed = mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(-0.0625d, -0.0625d, -0.0625d)).isEmpty() ? (mc.player.ticksExisted % 4 == 0) ? (this.noKick.getValue() ? -0.04f : 0.0f) : 0.0f : 0.0f;
            }

            final double[] directionalSpeed = MathUtil.directionSpeed(this.speed.getValue());

            if (mc.gameSettings.keyBindJump.isKeyDown() || mc.gameSettings.keyBindSneak.isKeyDown() || mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown()) {
                if (directionalSpeed[0] != 0.0d || directionalSpeed[1] != 0.0d) { // || ySpeed != 0.0d
                    if (mc.player.movementInput.jump && (mc.player.moveStrafing != 0 || mc.player.moveForward != 0)) {
                        mc.player.setVelocity(0, 0, 0);
                        move(0, 0, 0);
                        for (int i = 0; i <= 3; i++) {
                            mc.player.setVelocity(0, ySpeed * i, 0);
                            move(0, ySpeed * i, 0);
                        }
                    } else {
                        if (mc.player.movementInput.jump) {
                            mc.player.setVelocity(0, 0, 0);
                            move(0, 0, 0);
                            for (int i = 0; i <= 3; i++) {
                                mc.player.setVelocity(0, ySpeed * i, 0);
                                move(0, ySpeed * i, 0);
                            }
                        } else {
                            for (int i = 0; i <= 2; i++) {
                                mc.player.setVelocity(directionalSpeed[0] * i, ySpeed * i, directionalSpeed[1] * i);
                                move(directionalSpeed[0] * i, ySpeed * i, directionalSpeed[1] * i);
                            }
                        }
                    }
                }
            } else {
                if (this.noKick.getValue()) {
                    if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(-0.0625d, -0.0625d, -0.0625d)).isEmpty()) {
                        mc.player.setVelocity(0, (mc.player.ticksExisted % 2 == 0) ? 0.04f : -0.04f, 0);
                        move(0, (mc.player.ticksExisted % 2 == 0) ? 0.04f : -0.04f, 0);
                    }
                }
            }
        }
    }

    @Override
    public void onUpdate() {
        if(mode.getValue() == Mode.SPOOF) {
            if (fullNullCheck()) {
                return;
            }

            if (!mc.player.capabilities.allowFlying) {
                flySwitch.disable();
                flySwitch.enable();
                mc.player.capabilities.isFlying = false;
            }

            mc.player.capabilities.setFlySpeed(0.05f * speed.getValue());
        }
    }

    @Override
    public void onDisable() {
        if(mode.getValue() == Mode.CREATIVE && mc.player != null) {
            mc.player.capabilities.isFlying = false;
            mc.player.capabilities.setFlySpeed(0.05f);
            if (mc.player.capabilities.isCreativeMode) return;
            mc.player.capabilities.allowFlying = false;
        }

        if(mode.getValue() == Mode.SPOOF) {
            flySwitch.disable();
        }

        if(mode.getValue() == Mode.DAMAGE) {
            Phobos.timerManager.reset();
            mc.player.setVelocity(0, 0, 0);
            this.moveSpeed = Strafe.getBaseMoveSpeed();
            this.lastDist = 0;
            if (noClip.getValue()) {
                mc.player.noClip = false;
            }
        }
    }

    @Override
    public String getDisplayInfo() {
        return mode.currentEnumName();
    }

    @Override
    public void onLogout() {
        if(this.isOn()) {
            this.disable();
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if(event.getStage() == 0 && mode.getValue() == Mode.DAMAGE && format.getValue() == Format.DAMAGE) {
            double forward = mc.player.movementInput.moveForward;
            double strafe = mc.player.movementInput.moveStrafe;
            final float yaw = mc.player.rotationYaw;
            if (forward == 0.0 && strafe == 0.0) {
                event.setX(0.0);
                event.setZ(0.0);
            }
            if (forward != 0.0 && strafe != 0.0) {
                forward *= Math.sin(0.7853981633974483);
                strafe *= Math.cos(0.7853981633974483);
            }
            if (this.level != 1 || (mc.player.moveForward == 0.0f && mc.player.moveStrafing == 0.0f)) {
                if (this.level == 2) {
                    ++this.level;
                }
                else if (this.level == 3) {
                    ++this.level;
                    final double difference = (double)((((mc.player.ticksExisted % 2 == 0) ? -0.05 : 0.1)) * (this.lastDist - Strafe.getBaseMoveSpeed()));
                    this.moveSpeed = this.lastDist - difference;
                }
                else {
                    if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0)).size() > 0 || mc.player.collidedVertically) {
                        this.level = 1;
                    }
                    this.moveSpeed = this.lastDist - this.lastDist / 159.0;
                }
            }
            else {
                this.level = 2;
                final double boost = mc.player.isPotionActive(MobEffects.SPEED) ? 1.86 : 2.05;
                this.moveSpeed = boost * Strafe.getBaseMoveSpeed() - 0.01;
            }
            this.moveSpeed = Math.max(this.moveSpeed, Strafe.getBaseMoveSpeed());
            final double mx = -Math.sin(Math.toRadians(yaw));
            final double mz = Math.cos(Math.toRadians(yaw));
            event.setX(forward * this.moveSpeed * mx + strafe * this.moveSpeed * mz);
            event.setZ(forward * this.moveSpeed * mz - strafe * this.moveSpeed * mx);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if(event.getStage() == 0) {
            if(mode.getValue() == Mode.PACKET) {
                if (fullNullCheck()) {
                    return;
                }

                if (event.getPacket() instanceof CPacketPlayer && !(event.getPacket() instanceof CPacketPlayer.Position)) {
                    event.setCanceled(true);
                }
                if (event.getPacket() instanceof CPacketPlayer) {
                    final CPacketPlayer packet = event.getPacket();
                    if (packets.contains(packet)) {
                        packets.remove(packet);
                        return;
                    }
                    event.setCanceled(true);
                }
            }

            if(mode.getValue() == Mode.SPOOF) {
                if (fullNullCheck()) {
                    return;
                }

                if (!groundSpoof.getValue() || !(event.getPacket() instanceof CPacketPlayer)
                        || !mc.player.capabilities.isFlying) {
                    return;
                }

                CPacketPlayer packet = event.getPacket();
                if(!packet.moving) {
                    return;
                }

                AxisAlignedBB range = mc.player.getEntityBoundingBox().expand(0, -mc.player.posY, 0).contract(0, -mc.player.height, 0);
                List<AxisAlignedBB> collisionBoxes = mc.player.world.getCollisionBoxes(mc.player, range);
                AtomicReference<Double> newHeight = new AtomicReference<>(0D);
                collisionBoxes.forEach(box -> newHeight.set(Math.max(newHeight.get(), box.maxY)));

                packet.y = newHeight.get();
                packet.onGround = true;
            }

            if(mode.getValue() == Mode.DAMAGE) {
                if (format.getValue() == Format.PACKET || format.getValue() == Format.DELAY) {
                    if (event.getPacket() instanceof CPacketPlayer && !(event.getPacket() instanceof CPacketPlayer.Position)) {
                        event.setCanceled(true);
                    }
                    if (event.getPacket() instanceof CPacketPlayer) {
                        final CPacketPlayer packet = event.getPacket();
                        if (packets.contains(packet)) {
                            packets.remove(packet);
                            return;
                        }
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if(event.getStage() == 0) {
            if (mode.getValue() == Mode.PACKET) {
                if(fullNullCheck()) {
                    return;
                }
                if (event.getPacket() instanceof SPacketPlayerPosLook) {
                    final SPacketPlayerPosLook packet = event.getPacket();
                    if (mc.player.isEntityAlive() && mc.world.isBlockLoaded(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)) && !(mc.currentScreen instanceof GuiDownloadTerrain)) {
                        if (teleportId <= 0) {
                            teleportId = packet.getTeleportId();
                        } else {
                            event.setCanceled(true);
                        }
                    }
                }
            }

            if(mode.getValue() == Mode.SPOOF) {
                if(fullNullCheck()) {
                    return;
                }

                if (!antiGround.getValue() || !(event.getPacket() instanceof SPacketPlayerPosLook) || !mc.player.capabilities.isFlying) {
                    return;
                }

                SPacketPlayerPosLook packet = event.getPacket();
                double oldY = mc.player.posY;
                mc.player.setPosition(packet.x, packet.y, packet.z);

                AxisAlignedBB range = mc.player.getEntityBoundingBox().expand(0, 256 - mc.player.height - mc.player.posY, 0).contract(0, mc.player.height, 0);
                List<AxisAlignedBB> collisionBoxes = mc.player.world.getCollisionBoxes(mc.player, range);
                AtomicReference<Double> newY = new AtomicReference<>(256D);
                collisionBoxes.forEach(box -> newY.set(Math.min(newY.get(), box.minY - mc.player.height)));
                packet.y = Math.min(oldY, newY.get());
            }

            if(mode.getValue() == Mode.DAMAGE) {
                if (format.getValue() == Format.PACKET || format.getValue() == Format.DELAY) {
                    if (event.getPacket() instanceof SPacketPlayerPosLook) {
                        final SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();
                        if (mc.player.isEntityAlive() && mc.world.isBlockLoaded(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)) && !(mc.currentScreen instanceof GuiDownloadTerrain)) {
                            if (this.teleportId <= 0) {
                                this.teleportId = packet.getTeleportId();
                            } else {
                                event.setCanceled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        if(event.getStage() == 2) {
            if(event.getSetting() != null && event.getSetting().getFeature() != null && event.getSetting().getFeature().equals(this)) {
                if(this.isEnabled() && !event.getSetting().equals(this.enabled)) {
                    this.disable();
                }
            }
        }
    }

    @SubscribeEvent
    public void onPush(PushEvent event) {
        if(event.getStage() == 1 && mode.getValue() == Flight.Mode.PACKET && better.getValue() && phase.getValue()) {
            event.setCanceled(true);
        }
    }

    private void move(double x, double y, double z) {
        final CPacketPlayer pos = new CPacketPlayer.Position(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z, mc.player.onGround);
        packets.add(pos);
        mc.player.connection.sendPacket(pos);

        final CPacketPlayer bounds;
        if(better.getValue()) {
            bounds = createBoundsPacket(x, y, z);
        } else {
            bounds = new CPacketPlayer.Position(mc.player.posX + x, 0, mc.player.posZ + z, mc.player.onGround);
        }
        packets.add(bounds);
        mc.player.connection.sendPacket(bounds);

        teleportId++;
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(teleportId - 1));
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(teleportId));
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(teleportId + 1));
    }

    public enum Mode {
        CREATIVE,
        VANILLA,
        PACKET,
        SPOOF,
        DESCEND,
        DAMAGE
    }

    public enum Format {
        DAMAGE,
        SLOW,
        DELAY,
        NORMAL,
        PACKET
    }

    private enum PacketMode {
        Up,
        Down,
        Zero,
        Y,
        X,
        Z,
        XZ
    }

    private CPacketPlayer createBoundsPacket(double x, double y, double z) {
        switch (type.getValue()) {
            case Up: return new CPacketPlayer.Position(mc.player.posX + x, 10000, mc.player.posZ + z, mc.player.onGround);
            case Down: return new CPacketPlayer.Position(mc.player.posX + x, -10000, mc.player.posZ + z, mc.player.onGround);
            case Zero: return new CPacketPlayer.Position(mc.player.posX + x, 0, mc.player.posZ + z, mc.player.onGround);
            case Y: return new CPacketPlayer.Position(mc.player.posX + x, mc.player.posY + y <= 10 ? 255 : 1, mc.player.posZ + z, mc.player.onGround);
            case X: return new CPacketPlayer.Position(mc.player.posX + x + 75, mc.player.posY + y, mc.player.posZ + z, mc.player.onGround);
            case Z: return new CPacketPlayer.Position(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z + 75, mc.player.onGround);
            case XZ: return new CPacketPlayer.Position(mc.player.posX + x + 75, mc.player.posY + y, mc.player.posZ + z + 75, mc.player.onGround);
            default: return new CPacketPlayer.Position(mc.player.posX + x, 2000, mc.player.posZ + z, mc.player.onGround);
        }
        //throw new RuntimeException("WTF");
    }

    private static class Fly {
        protected void enable() {
            mc.addScheduledTask(() -> {
                if (mc.player == null || mc.player.capabilities == null) {
                    return;
                }

                mc.player.capabilities.allowFlying = true;
                mc.player.capabilities.isFlying = true;
            });
        }

        protected void disable() {
            mc.addScheduledTask(() -> {
                if (mc.player == null || mc.player.capabilities == null) {
                    return;
                }

                PlayerCapabilities gmCaps = new PlayerCapabilities();
                mc.playerController.getCurrentGameType().configurePlayerCapabilities(gmCaps);
                PlayerCapabilities capabilities = mc.player.capabilities;
                capabilities.allowFlying = gmCaps.allowFlying;
                capabilities.isFlying = gmCaps.allowFlying && capabilities.isFlying;
                capabilities.setFlySpeed(gmCaps.getFlySpeed());
            });
        }
    }
}
