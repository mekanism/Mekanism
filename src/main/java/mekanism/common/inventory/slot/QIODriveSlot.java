package mekanism.common.inventory.slot;

import mekanism.api.Action;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.content.qio.IQIODriveHolder;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.content.qio.QIODriveData.QIODriveKey;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class QIODriveSlot extends BasicInventorySlot {

    private IQIODriveHolder driveHolder;
    private int slot;

    public <TILE extends IMekanismInventory & IQIODriveHolder> QIODriveSlot(TILE inventory, int slot, int x, int y) {
        super((stack, automationType) -> automationType != AutomationType.EXTERNAL,
              (stack, automationType) -> automationType != AutomationType.EXTERNAL,
              (stack) -> stack.getItem() instanceof IQIODriveItem,
              inventory, x, y);
        this.driveHolder = inventory;
        this.slot = slot;
    }

    @Override
    public void setStack(ItemStack stack) {
        // if we're about to empty this slot and a drive already exists here, remove the current drive from the frequency
        if (!isRemote() && !getStack().isEmpty() && stack.isEmpty()) {
            removeDrive();
        }
        super.setStack(stack);
        // if we just added a new drive, add it to the frequency
        // (note that both of these operations can happen in this order if a user replaces the drive in the slot)
        if (!isRemote() && !getStack().isEmpty()) {
            addDrive(getStack());
        }
    }

    @Override
    public ItemStack insertItem(ItemStack stack, Action action, AutomationType automationType) {
        ItemStack ret = super.insertItem(stack, action, automationType);
        if (!isRemote() && action.execute() && ret.isEmpty()) {
            addDrive(stack);
        }
        return ret;
    }

    @Override
    public ItemStack extractItem(int amount, Action action, AutomationType automationType) {
        if (!isRemote() && action.execute()) {
            ItemStack ret = super.extractItem(amount, Action.SIMULATE, automationType);
            if (!ret.isEmpty()) {
                removeDrive();
            }
        }
        return super.extractItem(amount, action, automationType);
    }

    private boolean isRemote() {
        return ((TileEntity) driveHolder).getWorld().isRemote();
    }

    private void addDrive(ItemStack stack) {
        if (driveHolder.getQIOFrequency() != null)
            driveHolder.getQIOFrequency().addDrive(new QIODriveKey(driveHolder, slot));
    }

    private void removeDrive() {
        if (driveHolder.getQIOFrequency() != null)
            driveHolder.getQIOFrequency().removeDrive(new QIODriveKey(driveHolder, slot), true);
    }
}
