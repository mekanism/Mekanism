package mekanism.common.inventory.slot;

import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.resource.LargeResourceStack;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.content.qio.IQIODriveHolder;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.content.qio.QIODriveData.QIODriveKey;
import mekanism.common.content.qio.QIOFrequency;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class QIODriveSlot extends BasicInventorySlot {

    public static final Predicate<ItemResource> IS_QIO_ITEM = itemType -> itemType.getItem() instanceof IQIODriveItem;

    private final Supplier<@Nullable Level> levelSupplier;
    private final IQIODriveHolder driveHolder;
    private final QIODriveKey key;

    public QIODriveSlot(IQIODriveHolder driveHolder, int slot, Supplier<@Nullable Level> levelSupplier, @Nullable IContentsListener listener, int x, int y) {
        super(ConstantPredicates.notExternal(), ConstantPredicates.notExternal(), IS_QIO_ITEM, listener, x, y);
        this.driveHolder = driveHolder;
        this.levelSupplier = levelSupplier;
        this.key = new QIODriveKey(this.driveHolder, slot);
    }

    //TODO - 26.1: Re-evaluate these two overrides
    // I think we don't want them, but we do need to make sure we add/remove when loading saved data
    /*@Override
    public void setStack(ItemResource itemType, int storedAmount) {
        // if we're about to empty this slot and a drive already exists here, remove the current drive from the frequency
        // Note: We don't check to see if the new stack is empty so that we properly are able to handle direct changes
        if (!isRemote() && !isEmpty()) {
            removeDrive();
        }
        super.setStack(itemType, storedAmount);
        // if we just added a new drive, add it to the frequency
        // (note that both of these operations can happen in this order if a user replaces the drive in the slot)
        if (!isRemote() && !isEmpty()) {
            addDrive();
        }
    }

    @Override
    public void setStackUnchecked(ItemResource itemType, int storedAmount) {
        // if we're about to empty this slot and a drive already exists here, remove the current drive from the frequency
        // Note: We don't check to see if the new stack is empty so that we properly are able to handle direct changes
        if (!isRemote() && !isEmpty()) {
            removeDrive();
        }
        super.setStackUnchecked(itemType, storedAmount);
        // if we just added a new drive, add it to the frequency
        // (note that both of these operations can happen in this order if a user replaces the drive in the slot)
        if (!isRemote() && !isEmpty()) {
            addDrive();
        }
    }*/

    //TODO - 26.1: On extracting we need to make sure that the drive's metadata is updated so that it returns the correct resource(?)

    @Override
    protected void onRootCommit(LargeResourceStack<ItemResource> originalState) {
        super.onRootCommit(originalState);
        //TODO - 26.1: Should we do this before or after calling super (and setting the contents changed)
        ItemResource newDrive = getResource();
        ItemResource originalDrive = originalState.resource();
        if (!isRemote() && !newDrive.equals(originalDrive)) {
            QIOFrequency frequency = driveHolder.getQIOFrequency();
            if (frequency != null) {
                //If the item type changed, start by removing the old drive and then adding the new one
                if (!originalState.isEmpty() && IS_QIO_ITEM.test(originalDrive)) {
                    frequency.removeDrive(key, true, originalDrive);
                }
                if (!newDrive.isEmpty() && IS_QIO_ITEM.test(newDrive)) {
                    frequency.addDrive(key, newDrive);
                }
            }
        }
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
}
