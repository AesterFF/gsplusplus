package com.gamesense.client.module.modules.misc;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.setting.values.IntegerSetting;
import com.gamesense.api.setting.values.ModeSetting;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.misc.Timer;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PlayerUtil;
import com.gamesense.api.util.player.social.SocialManager;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Mouse;

import java.util.Arrays;

@Module.Declaration(name = "MouseClickAction", category = Category.Misc)
public class MouseClickAction extends Module {

    BooleanSetting friend = registerBoolean("friend", true);
    ModeSetting friendButton = registerMode("FriendButton", Arrays.asList("MOUSE3", "MOUSE4", "MOUSE5"), "MOUSE3",() -> friend.getValue());
    BooleanSetting pearl = registerBoolean("pearl", true);
    ModeSetting pearlButton = registerMode("PearlButton", Arrays.asList("MOUSE3", "MOUSE4", "MOUSE5"), "MOUSE4",() -> pearl.getValue());
    BooleanSetting clipRotate = registerBoolean("clipRotate", false, () -> pearl.getValue());
    IntegerSetting pearlPitch = registerInteger("Pitch", 85, -90, 90, () -> clipRotate.getValue());
    BooleanSetting onGroundCheck = registerBoolean("onGround", true, () -> clipRotate.getValue());
    BooleanSetting silentSwitch = registerBoolean("silentSwitch", false);
    BooleanSetting silentInv = registerBoolean("Silent Inventory", false, () -> silentSwitch.getValue());

    int MCPButtonCode;
    int MCFButtonCode;
    int pearlInvSlot;
    boolean swapBack;

    public void onUpdate() {

        final Timer MCPdelayTimer = new Timer();

        float pearlPitchFloat = pearlPitch.getValue().floatValue();

        if (pearlButton.getValue().equalsIgnoreCase("MOUSE3")) {
            MCPButtonCode = 2; //Mouse3 (used for MCF so using this will retard your gameplay)
        } else if (pearlButton.getValue().equalsIgnoreCase("MOUSE4")) {
            MCPButtonCode = 3; //Mouse4
        } else if (pearlButton.getValue().equalsIgnoreCase("MOUSE5")) {
            MCPButtonCode = 4; //Mouse5
        } else {
            MCPButtonCode = 2; //User Error Protection
        }

        if (friendButton.getValue().equalsIgnoreCase("MOUSE3")) {
            MCFButtonCode = 2; //Mouse3 (used for MCF so using this will retard your gameplay)
        } else if (friendButton.getValue().equalsIgnoreCase("MOUSE4")) {
            MCFButtonCode = 3; //Mouse4
        } else if (friendButton.getValue().equalsIgnoreCase("MOUSE5")) {
            MCFButtonCode = 4; //Mouse5
        } else {
            MCFButtonCode = 2; //User Error Protection
        }

        if ((Mouse.isButtonDown(MCPButtonCode) && onGroundCheck.getValue() && mc.player.onGround && pearl.getValue()) || Mouse.isButtonDown(MCPButtonCode) && !onGroundCheck.getValue() && pearl.getValue()) { //We check for button press and don't check for miss :rage:
            int oldSlot = mc.player.inventory.currentItem;

            int pearlSlot = InventoryUtil.findFirstItemSlot(ItemEnderPearl.class, 0, 8);

                if (!silentSwitch.getValue()) {
                    mc.player.inventory.currentItem = pearlSlot;

                } else {

                    if (silentInv.getValue() && pearlSlot == -1) {

                        pearlInvSlot = InventoryUtil.findFirstItemSlot(Items.ENDER_PEARL.getClass(), 0, 69);

                        if (pearlInvSlot != -1)
                            swap(pearlInvSlot, mc.player.inventory.currentItem);
                        else disable();
                        swapBack = true;

                    }

                    if (pearlSlot != -1){
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(pearlSlot));
                    } else if (!silentInv.getValue())
                        disable();

                }

                if (clipRotate.getValue()) {

                    //ROUNDING
                    float yawRounded;

                    float yaw = Math.abs(Math.round(mc.player.rotationYaw) % 360);
                    float division = (int) Math.floor(yaw / 45);
                    float remainder = (int) (yaw % 45);
                    if (remainder < 45 / 2) {
                        yawRounded = 45 * division;
                    } else {
                        yawRounded = 45 * (division + 1);
                    }
                    //END OF ROUNDING

                    //new rotate

                    //mc.player.setPositionAndRotationDirect(mc.player.posX,mc.player.posY,mc.player.posZ,yawRounded,pearlPitchFloat,1,true);

                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yawRounded, pearlPitchFloat, true)); // rotate for phasing
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND); // Throw the pearl (thanks TechAle :D)

                    // mc.player.connection.sendPacket(new CPacketPlayer.Rotation(oldYaw, oldPitch, true)); // rotate back (disabled)
                    mc.player.inventory.currentItem = oldSlot; // return to old slot

                } else { // same as Hoosiers' code previous code

                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);

                    if (!silentSwitch.getValue()) {

                        mc.player.inventory.currentItem = oldSlot;

                    } else { //Undo desync?

                        mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));

                        if (swapBack) {
                            swap(pearlInvSlot, mc.player.inventory.currentItem);
                            swapBack = false;
                        }

                        pearlInvSlot = -1;

                    }
                }
            }
        }

    @EventHandler
        final Listener<InputEvent.MouseInputEvent> listener = new Listener<>(event -> {
            if (mc.objectMouseOver.typeOfHit.equals(RayTraceResult.Type.ENTITY) && mc.objectMouseOver.entityHit instanceof EntityPlayer && Mouse.isButtonDown(MCFButtonCode) && friend.getValue()) {
                if (SocialManager.isFriend(mc.objectMouseOver.entityHit.getName())) {
                    SocialManager.delFriend(mc.objectMouseOver.entityHit.getName());
                    MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getDisabledColor() + "Removed " + mc.objectMouseOver.entityHit.getName() + " from friends list");
                } else {
                    SocialManager.addFriend(mc.objectMouseOver.entityHit.getName());
                    MessageBus.sendClientPrefixMessage(ModuleManager.getModule(ColorMain.class).getEnabledColor() + "Added " + mc.objectMouseOver.entityHit.getName() + " to friends list");
                }
            }
        });

    void swap(int slot1, int slot2) {

        // pick up inventory slot
        mc.playerController.windowClick(0, slot1, 0, ClickType.PICKUP, mc.player);

        // click on hotbar slot
        // 36 is the offset for start of hotbar in inventoryContainer
        mc.playerController.windowClick(0, slot2 + 36, 0, ClickType.PICKUP, mc.player);

        // put back inventory slot
        mc.playerController.windowClick(0, slot1, 0, ClickType.PICKUP, mc.player);

        mc.playerController.updateController();

    }

    void mcp() {

        if (PlayerUtil.nullCheck()) {
            int slot = -1;
            int oldslot = mc.player.inventory.currentItem;

            for (int i = 9; i < 45; i++) {

                if (mc.player.inventory.getStackInSlot(i).item.equals(Items.ENDER_PEARL)) {

                    slot = i;

                }

            }

            swap(slot, oldslot);

            mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));

            swap(oldslot, slot);
        }

    }

}

