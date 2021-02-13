package mekanism.common.inventory.slot;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.content.qio.IQIODriveHolder;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.content.qio.QIODriveData.QIODriveKey;
import mekanism.common.content.qio.QIOFrequency;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class QIODriveSlot extends BasicInventorySlot {

    private final IQIODriveHolder driveHolder;
    private final QIODriveKey key;

    public <TILE extends IMekanismInventory & IQIODriveHolder> QIODriveSlot(TILE inventory, int slot, int x, int y) {
        super(notExternal, notExternal, stack -> stack.getItem() instanceof IQIODriveItem, inventory, x, y);
        key = new QIODriveKey(inventory, slot);
        driveHolder = inventory;
    }

    @Override
    public void setStack(ItemStack stack) {
        // if we're about to empty this slot and a drive already exists here, remove the current drive from the frequency
        // Note: We don't check to see if the new stack is empty so that we properly are able to handle direct changes
        if (!isRemote() && !isEmpty()) {
            removeDrive();
        }
        super.setStack(stack);
        // if we just added a new drive, add it to the frequency
        // (note that both of these operations can happen in this order if a user replaces the drive in the slot)
        if (!isRemote() && !isEmpty()) {
            addDrive(getStack());
        }
    }

    @Override
    protected void setStackUnchecked(ItemStack stack) {
        // if we're about to empty this slot and a drive already exists here, remove the current drive from the frequency
        // Note: We don't check to see if the new stack is empty so that we properly are able to handle direct changes
        if (!isRemote() && !isEmpty()) {
            removeDrive();
        }
        super.setStackUnchecked(stack);
        // if we just added a new drive, add it to the frequency
        // (note that both of these operations can happen in this order if a user replaces the drive in the slot)
        if (!isRemote() && !isEmpty()) {
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

    public QIODriveKey getKey() {
        return key;
    }

    private boolean isRemote() {
        World world = ((TileEntity) driveHolder).getWorld();
        //Treat world as remote if it is null (hasn't been assigned yet)
        // which may happen when loading the drives from memory
        return world == null || world.isRemote();
    }

    private void addDrive(ItemStack stack) {
        QIOFrequency frequency = driveHolder.getQIOFrequency();
        if (frequency != null) {
            frequency.addDrive(key);
        }
    }

    private void removeDrive() {
        QIOFrequency frequency = driveHolder.getQIOFrequency();
        if (frequency != null) {
            frequency.removeDrive(key, true);
        }
    }
}
