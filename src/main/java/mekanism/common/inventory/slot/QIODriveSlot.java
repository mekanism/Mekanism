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

    //TODO - 26.1: On extracting we need to make sure that the drive's metadata is updated so that it returns the correct resource(?)

    @Override
    protected void onContentsChanged(LargeResourceStack<ItemResource> originalState) {
        super.onContentsChanged(originalState);
        ItemResource newDrive = resource();
        ItemResource originalDrive = originalState.resource();
        if (!isRemote() && !newDrive.equals(originalDrive)) {
            QIOFrequency frequency = driveHolder.getQIOFrequency();
            if (frequency != null) {
                // if we're about to empty this slot and a drive already exists here, remove the current drive from the frequency
                if (!originalState.isEmpty() && IS_QIO_ITEM.test(originalDrive)) {
                    frequency.removeDrive(key, true, originalDrive);
                }
                // if we just added a new drive, add it to the frequency
                // (note that both of these operations can happen in this order if a user replaces the drive in the slot)
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
