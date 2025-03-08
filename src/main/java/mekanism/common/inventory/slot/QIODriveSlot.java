package mekanism.common.inventory.slot;

import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.content.qio.IQIODriveHolder;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.content.qio.QIODriveData.QIODriveKey;
import mekanism.common.content.qio.QIOFrequency;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class QIODriveSlot extends BasicInventorySlot {

    public static final Predicate<ItemStack> IS_QIO_ITEM = stack -> stack.getItem() instanceof IQIODriveItem;

    private final Supplier<@Nullable Level> levelSupplier;
    private final IQIODriveHolder driveHolder;
    private final QIODriveKey key;

    public QIODriveSlot(IQIODriveHolder driveHolder, int slot, Supplier<@Nullable Level> levelSupplier, @Nullable IContentsListener listener, int x, int y) {
        super(ConstantPredicates.notExternal(), ConstantPredicates.notExternal(), IS_QIO_ITEM, listener, x, y);
        this.driveHolder = driveHolder;
        this.levelSupplier = levelSupplier;
        this.key = new QIODriveKey(this.driveHolder, slot);
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
    public void setStackUnchecked(ItemStack stack) {
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
        Level level = levelSupplier.get();
        //Treat world as remote if it is null (hasn't been assigned yet)
        // which may happen when loading the drives from memory
        return level == null || level.isClientSide();
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
