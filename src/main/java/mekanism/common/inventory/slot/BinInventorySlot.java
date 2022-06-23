package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NonNull;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.tier.BinTier;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BinInventorySlot extends BasicInventorySlot {

    private static final Predicate<@NonNull ItemStack> validator = stack -> !(stack.getItem() instanceof ItemBlockBin);

    public static BinInventorySlot create(@Nullable IContentsListener listener, BinTier tier) {
        Objects.requireNonNull(tier, "Bin tier cannot be null");
        return new BinInventorySlot(listener, tier);
    }

    private final boolean isCreative;
    private boolean isLocked;
    @Nonnull
    private ItemStack lockStack = ItemStack.EMPTY;

    private BinInventorySlot(@Nullable IContentsListener listener, BinTier tier) {
        super(tier.getStorage(), alwaysTrueBi, alwaysTrueBi, validator, listener, 0, 0);
        isCreative = tier == BinTier.CREATIVE;
        obeyStackLimit = false;
    }

    @Override
    public ItemStack insertItem(ItemStack stack, Action action, AutomationType automationType) {
        if (isLocked && !ItemHandlerHelper.canItemStacksStack(stack, lockStack)) {
            // When locked, we need to make sure an item of other type is not inserted
            return stack;
        }
        if (isCreative && isEmpty() && action.execute() && automationType != AutomationType.EXTERNAL) {
            //If a player manually inserts into a creative bin, that is empty we need to allow setting the type,
            // Note: We check that it is not external insertion because an empty creative bin acts as a "void" for automation
            ItemStack simulatedRemainder = super.insertItem(stack, Action.SIMULATE, automationType);
            if (simulatedRemainder.isEmpty()) {
                //If we are able to insert it then set perform the action of setting it to full
                setStackUnchecked(StackUtils.size(stack, getLimit(stack)));
            }
            return simulatedRemainder;
        }
        return super.insertItem(stack, action.combine(!isCreative), automationType);
    }

    @Override
    public ItemStack extractItem(int amount, Action action, AutomationType automationType) {
        return super.extractItem(amount, action.combine(!isCreative), automationType);
    }

    /**
     * {@inheritDoc}
     *
     * Note: We are only patching {@link #setStackSize(int, Action)}, as both {@link #growStack(int, Action)} and {@link #shrinkStack(int, Action)} are wrapped through
     * this method.
     */
    @Override
    public int setStackSize(int amount, Action action) {
        return super.setStackSize(amount, action.combine(!isCreative));
    }

    @Override
    public int getLimit(ItemStack stack) {
        final int normalLimit = super.getLimit(stack);
        return isLocked ? normalLimit - 1 : normalLimit;
    }

    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot() {
        return null;
    }

    /**
     * Gets the "bottom" stack for the bin, this is the stack that can be extracted/interacted with directly.
     *
     * @return The "bottom" stack for the bin
     *
     * @apiNote The returned stack can be safely modified.
     */
    public ItemStack getBottomStack() {
        if (isEmpty()) {
            return ItemStack.EMPTY;
        }
        // If locked, the last item can't be extracted
        return StackUtils.size(current, Math.min(getCount(), current.getMaxStackSize()));
    }

    /**
     * Modifies the lock state of the slot.
     * @param locked if the slot should be locked
     * @param clientSide if this method is called client side
     * @return if the lock state was modified
     */
    @CanIgnoreReturnValue
    public boolean setLocked(boolean locked, boolean clientSide) {
        if (this.isLocked == locked || (getStack().isEmpty() && !this.isLocked)) {
            return false;
        }
        this.isLocked = locked;
        if (locked) {
            lockStack = getStack().copy();
            lockStack.setCount(1);
            if (!clientSide) {
                current.shrink(1); // One of the items is now frozen
            }
        } else {
            lockStack = ItemStack.EMPTY;
            if (!clientSide) {
                current.grow(1); // We want to re-add the frozen item
            }
        }
        return true;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public ItemStack getRenderStack() {
        return isLocked() ? getLockStack() : getStack();
    }

    public ItemStack getLockStack() {
        return lockStack;
    }

    /**
     * Similar to {@link #getCount()} except, if the bin is locked, the "lock item" is added to the count.
     * @return the actual stack count
     */
    public int getActualCount() {
        final int count = getCount();
        return isLocked() ? count + 1 : count;
    }

    @Override
    public CompoundTag serializeNBT() {
        final CompoundTag nbt = super.serializeNBT();
        nbt.putBoolean(NBTConstants.LOCKED, isLocked);
        nbt.put("lockStack", lockStack.serializeNBT());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        NBTUtils.setBooleanIfPresent(nbt, NBTConstants.LOCKED, t -> setLocked(t, false));
        NBTUtils.setItemStackIfPresent(nbt, "lockStack", s -> this.lockStack = s);
    }
}