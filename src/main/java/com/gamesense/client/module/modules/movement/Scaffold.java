package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.ColorSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PredictUtil;
import com.gamesense.api.util.render.RenderUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.api.util.world.MotionUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.lang.management.MonitorInfo;

@Module.Declaration(name = "Scaffold", category = Category.Movement)
public class Scaffold extends Module {

    BooleanSetting render = registerBoolean("Render", false);
    ColorSetting mainColor = registerColor("Color");

    IntegerSetting tickPredict = registerInteger("Tick Predict", 8, 0, 30);
    BooleanSetting calculateYPredict = registerBoolean("Calculate Y Predict", true);
    IntegerSetting startDecrease = registerInteger("Start Decrease", 39, 0, 200, () -> calculateYPredict.getValue());
    IntegerSetting exponentStartDecrease = registerInteger("Exponent Start", 2, 1, 5, () -> calculateYPredict.getValue());
    IntegerSetting decreaseY = registerInteger("Decrease Y", 2, 1, 5, () -> calculateYPredict.getValue());
    IntegerSetting exponentDecreaseY = registerInteger("Exponent Decrease Y", 1, 1, 3, () -> calculateYPredict.getValue());
    IntegerSetting increaseY = registerInteger("Increase Y", 3, 1, 5, () -> calculateYPredict.getValue());
    IntegerSetting exponentIncreaseY = registerInteger("Exponent Increase Y", 2, 1, 3, () -> calculateYPredict.getValue());
    BooleanSetting splitXZ = registerBoolean("Split XZ", true);
    IntegerSetting width = registerInteger("Line Width", 2, 1, 5);
    BooleanSetting debug = registerBoolean("Debug", false);
    BooleanSetting showPredictions = registerBoolean("Show Predictions", false);
    BooleanSetting manualOutHole = registerBoolean("Manual Out Hole", false);
    BooleanSetting aboveHoleManual = registerBoolean("Above Hole Manual", false, () -> manualOutHole.getValue());

    int targetBlockSlot;
    int oldSlot;
    int direction;

    boolean doSupport;
    boolean replacable;
    boolean doDown;
    boolean crouched;

    BlockPos belowPlayerBlock;
    BlockPos playerBlock;
    BlockPos supportBlock;
    BlockPos supportBlockTwo;

    com.gamesense.api.util.misc.Timer timer = new com.gamesense.api.util.misc.Timer();


    PredictUtil.PredictSettings predictSettings;


    @Override
    public void onUpdate() {

        //safewalk

        if (!mc.player.isSneaking() &&mc.world.getBlockState(new BlockPos(mc.player.posX,mc.player.posY-2,mc.player.posZ)).getMaterial().isReplaceable() && !doDown) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            crouched = true;
        }

        predictSettings = new PredictUtil.PredictSettings(tickPredict.getValue(), calculateYPredict.getValue(), startDecrease.getValue(), exponentStartDecrease.getValue(), decreaseY.getValue(), exponentDecreaseY.getValue(), increaseY.getValue(), exponentIncreaseY.getValue(), splitXZ.getValue(), width.getValue(), debug.getValue(), showPredictions.getValue(), manualOutHole.getValue(), aboveHoleManual.getValue());

        if (render.getValue()) {

            EntityPlayer clonedPlayer = PredictUtil.predictPlayer(mc.player, predictSettings);
            RenderUtil.drawBoundingBox(clonedPlayer.getEntityBoundingBox(), width.getValue(), mainColor.getColor());

        }

        direction = (MathHelper.floor((double) (mc.player.rotationYaw * 8.0F / 360.0F) + 0.5D) & 7);


/*        if (mc.gameSettings.keyBindJump.isPressed()) {

            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.42, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.75, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.01, mc.player.posZ, true));
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16, mc.player.posZ, true));

            }*/

        targetBlockSlot =

                oldSlot = mc.player.inventory.currentItem;

        if (targetBlockSlot == -1) {
            MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "You dont have any obby lol");
            disable();
        }

        mc.player.connection.sendPacket(new CPacketHeldItemChange(targetBlockSlot));

            /*
        switch (MathHelper.floor((double) (mc.player.rotationYaw * 8.0F / 360.0F) + 0.5D) & 7) {
            case 0:
                return "+Z";
            case 1:
                return "-X +Z";
            case 2:
                return "-X";
            case 3:
                return "-X -Z";
            case 4:
                return "-Z";
            case 5:
                return "+X -Z";
            case 6:
                return "+X";
            case 7:
                return "+X +Z";
            }
        }
        *///this might be useful

        playerBlock = new BlockPos(PredictUtil.predictPlayer(mc.player, predictSettings));

        supportBlock = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
        supportBlockTwo = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);

        if (mc.gameSettings.keyBindSprint.isKeyDown()) {
            belowPlayerBlock = playerBlock.add(0, -2, 0);
            supportBlock = supportBlock.add(0, -2, 0);
            supportBlockTwo = supportBlockTwo.add(-1,-2,0);
            doDown = true;
        } else {
            belowPlayerBlock = playerBlock.add(0, -1, 0);
            supportBlock = supportBlock.add(1, -1, 0);
            supportBlockTwo = supportBlockTwo.add(-1,-1,0);
            doDown = false;
        }

        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.motionX *= 0.3;
            mc.player.motionZ *= 0.3;
            mc.player.jump();
            if (timer.hasReached(1500,true)){
                mc.player.motionY = -0.28;
            }

            mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));

            if (!mc.world.getBlockState(belowPlayerBlock).getMaterial().isReplaceable()
                    || mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(belowPlayerBlock)).stream().anyMatch(entity -> entity instanceof EntityPlayer && entity != mc.player)) {
                replacable = true;
                return;
            }

            int newSlot;
            newSlot = InventoryUtil.findObsidianSlot(false, false);

            if (newSlot == -1)
                return;

            int oldSlot;
            oldSlot = mc.player.inventory.currentItem;
            mc.player.inventory.currentItem = newSlot;

            // place block

            if (crouched) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            if (!doDown) {
                placeBlockPacket(null, belowPlayerBlock);
                placeBlockPacket(null, supportBlock);
                placeBlockPacket(null, supportBlockTwo);
            }
            if (doDown) {
                placeBlockPacket(null, supportBlock);
            }

            mc.player.inventory.currentItem = oldSlot;

            doSupport = false;
            if (!doDown) {
                mc.player.setSneaking(true);
            }
        }
    }

    void placeBlockPacket(EnumFacing side, BlockPos pos) {

        if (side == null) {
            side = BlockUtil.getPlaceableSide(pos);
        }
        if (side == null){

            doSupport = true;
            return;
        }
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d vec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));

        // idk why these but PlayerControllerMP use them
        float f = (float) (vec.x - (double) pos.getX());
        float f1 = (float) (vec.y - (double) pos.getY());
        float f2 = (float) (vec.z - (double) pos.getZ());

        // Place
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(
                neighbour
                , opposite, EnumHand.MAIN_HAND, f, f1, f2));

        // Swing
        mc.player.swingArm(EnumHand.MAIN_HAND);
    }
}


