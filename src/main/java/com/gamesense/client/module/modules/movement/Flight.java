package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.DoubleSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketPlayer;

import java.util.Arrays;

@Module.Declaration(name = "Flight", category = Category.Movement)
public class Flight extends Module {

    float flyspeed;
    boolean bounded;

    ModeSetting mode = registerMode("Mode", Arrays.asList("Vanilla", "Static", "Packet", "Damage"), "Static");

    ModeSetting damage = registerMode("Damage Mode", Arrays.asList("LB", "WI"), "WI", () -> mode.getValue().equalsIgnoreCase("Damage"));

    ModeSetting packetMode = registerMode("Packet Mode", Arrays.asList("Position", "Update"), "Position");
    DoubleSetting packetFactor = registerDouble("Packet Factor", 1, 0, 5, () -> mode.getValue().equalsIgnoreCase("Packet"));
    ModeSetting bound = registerMode("Bounds", Arrays.asList("Up", "Alternate", "Down", "Zero"), "Up", () -> mode.getValue().equalsIgnoreCase("Packet"));
    ModeSetting antiKick = registerMode("AntiKick", Arrays.asList("None", "Down", "Bounce"), "Bounce", () -> mode.getValue().equalsIgnoreCase("Packet"));

    DoubleSetting speed = registerDouble("Speed", 2, 0, 10, () -> !mode.getValue().equalsIgnoreCase("Packet"));
    DoubleSetting ySpeed = registerDouble("Y Speed", 1, 0, 10, () -> !mode.getValue().equalsIgnoreCase("Packet"));
    DoubleSetting glideSpeed = registerDouble("Glide Speed", 0, -10, 10, () -> !mode.getValue().equalsIgnoreCase("Packet"));

    @EventHandler
    private final Listener<PlayerMoveEvent> playerMoveEventListener = new Listener<>(event -> {

        if (mode.getValue().equalsIgnoreCase("Static")) {
            if (mc.gameSettings.keyBindJump.isKeyDown()) {

                event.setY(ySpeed.getValue());

            } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {

                event.setY(-ySpeed.getValue());

            } else {

                event.setY(-glideSpeed.getValue());

            }

            if (MotionUtil.isMoving(mc.player)) {
                MotionUtil.setSpeed(mc.player, speed.getValue());
            } else {

                event.setX(0);
                event.setZ(0);

            }
        } else if (mode.getValue().equalsIgnoreCase("Vanilla")) {

            mc.player.capabilities.setFlySpeed(flyspeed * speed.getValue().floatValue());
            mc.player.capabilities.isFlying = true;

        } else if (mode.getValue().equalsIgnoreCase("Packet")) {

            mc.player.setVelocity(0, 0, 0);
            event.setY(0);

            if (mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown()) {

                if (packetMode.getValue().equalsIgnoreCase("Position")){
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + mc.player.motionX, mc.player.posY - 0.0624, mc.player.posZ + mc.player.motionZ, false));
                } else {
                    mc.player.setPosition(mc.player.posX + mc.player.motionX, mc.player.posY - 0.0624, mc.player.posZ + mc.player.motionZ);
                }
                bounded = true;

            }
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                if (packetMode.getValue().equalsIgnoreCase("Position")){
                    mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 0.0624, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                } else {
                    mc.player.setPosition(mc.player.posX, mc.player.posY + 0.0624, mc.player.posZ);
                }
                bounded = true;

            }
            if (mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown()) {

                double[] dir = MotionUtil.forward(0.0624 * packetFactor.getValue());

                if (packetMode.getValue().equalsIgnoreCase("Position")){
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + (dir[0]), mc.player.posY, mc.player.posZ + (dir[1]), mc.player.onGround));
                } else {
                    mc.player.setPosition(mc.player.posX + (dir[0]), mc.player.posY, mc.player.posZ + (dir[1]));
                }
                bounded = true;

            }


            if (!antiKick.getValue().equalsIgnoreCase("None") && mc.player.ticksExisted % 4 == 0) {

                event.setY(-0.01);
                bounded = true;

            } else if (antiKick.getValue().equalsIgnoreCase("Bounce") && mc.player.ticksExisted % 4 == 2) {

                event.setY(0.01);
                bounded = true;

            } else if (antiKick.getValue().equalsIgnoreCase("None")) {

                event.setY(0);
                bounded = true;

            }

            doBounds();

        } else if (mode.getValue().equalsIgnoreCase("Damage")) {

            if (MotionUtil.isMoving(mc.player)) {
                MotionUtil.setSpeed(mc.player, speed.getValue());
            } else {

                event.setX(0);
                event.setZ(0);

            }

            event.setY(-0.001);

        }


    });

    private void doBounds() {
        if (bounded){
            switch (bound.getValue()) {

                case "Up":
                    mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                case "Down":
                    mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                case "Zero":
                    mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, 0, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                default:
                    if (mc.player.ticksExisted % 2 == 0)
                        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY + 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));
                    else
                        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - 69420, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, false));


            }
        }
    }

    @Override
    protected void onEnable() {
        flyspeed = mc.player.capabilities.getFlySpeed();

        if (mode.getValue().equalsIgnoreCase("Damage")) {

            damage();
            mc.player.jump();

        }
    }

    @Override
    protected void onDisable() {
        mc.player.capabilities.setFlySpeed(flyspeed);
        mc.player.capabilities.isFlying = false;
        mc.player.motionX = mc.player.motionY = mc.player.motionZ = 0;
    }

    public void damage() {

        if (damage.getValue().equalsIgnoreCase("WI")) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 3.1, mc.player.posZ, false)); // send the player up
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.05, mc.player.posZ, false));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, true)); // set onground to true and deal damage

            mc.player.motionY = -5; // go down fast (idk if will help at all)}

        } else {

            for (int i = 0; i < 64; i++) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.049, mc.player.posZ, false));
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
            }

            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1, mc.player.posZ, true));

        }

    }


}
