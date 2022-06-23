package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
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

    private BinInventorySlot(@Nullable IContentsListener listener, BinTier tier) {
        super(tier.getStorage(), alwaysTrueBi, alwaysTrueBi, validator, listener, 0, 0);
        isCreative = tier == BinTier.CREATIVE;
        obeyStackLimit = false;
    }

    @Override
    public ItemStack insertItem(ItemStack stack, Action action, AutomationType automationType) {
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
        // TODO: this doesn't seem to work; hoppers infinitely extract
        final ItemStack old = getStack().copy();
        final ItemStack extracted = super.extractItem(amount, action.combine(!isCreative), automationType);
        if (isLocked && current.isEmpty()) {
            old.setCount(1);
            setStackUnchecked(old);
            extracted.shrink(1);
        }
        return extracted;
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
        return StackUtils.size(current, Math.min(isLocked ? getCount() - 1 : getCount(), current.getMaxStackSize()));
    }

    public void setLocked(boolean locked) {
        this.isLocked = locked;
    }

    public boolean isLocked() {
        return isLocked;
    }

    @Override
    public CompoundTag serializeNBT() {
        final CompoundTag nbt = super.serializeNBT();
        nbt.putBoolean(NBTConstants.LOCKED, isLocked);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        NBTUtils.setBooleanIfPresent(nbt, NBTConstants.LOCKED, this::setLocked);
    }
}