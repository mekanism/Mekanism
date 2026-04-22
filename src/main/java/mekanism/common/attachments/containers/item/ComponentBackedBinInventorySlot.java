
package mekanism.common.attachments.containers.item;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.ItemStackTemplateHelper;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.attachments.LockData;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.BinTier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ComponentBackedBinInventorySlot extends ComponentBackedInventorySlot {

    private final boolean isCreative;

    public static ComponentBackedBinInventorySlot create(ContainerType<?, ?, ?> ignored, ItemStack attachedTo, int tankIndex) {
        if (!(attachedTo.getItem() instanceof ItemBlockBin item)) {
            throw new IllegalStateException("Attached to should always be a bin item");
        }
        return new ComponentBackedBinInventorySlot(attachedTo, tankIndex, item.getTier());
    }

    private ComponentBackedBinInventorySlot(ItemStack attachedTo, int slotIndex, BinTier tier) {
        super(attachedTo, slotIndex, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), BinInventorySlot.validator, false, tier.getStorage());
        isCreative = tier == BinTier.CREATIVE;
    }

    @Override
    public ItemStack insertItem(AttachedItems attachedItems, ItemStack current, ItemStack stack, Action action, AutomationType automationType) {
        if (current.isEmpty()) {
            ItemStackTemplate lockStack = getLockStack();
            if (lockStack != null && !ItemStack.isSameItemSameComponents(stack, lockStack)) {
                // When locked, we need to make sure the correct item type is being inserted
                return stack;
            } else if (isCreative && action.execute() && automationType != AutomationType.EXTERNAL) {
                //If a player manually inserts into a creative bin, that is empty we need to allow setting the type,
                // Note: We check that it is not external insertion because an empty creative bin acts as a "void" for automation
                ItemStack simulatedRemainder = super.insertItem(attachedItems, current, stack, Action.SIMULATE, automationType);
                if (simulatedRemainder.isEmpty()) {
                    //If we are able to insert it then set perform the action of setting it to full
                    setContents(attachedItems, stack.copyWithCount(getLimit(stack)));
                }
                return simulatedRemainder;
            }
        }
        return super.insertItem(attachedItems, current, stack, action.combine(!isCreative), automationType);
    }

    @Override
    public ItemStack extractItem(int amount, Action action, AutomationType automationType) {
        return super.extractItem(amount, action.combine(!isCreative), automationType);
    }

    /**
     * {@inheritDoc}
     *
     * Note: We are only patching {@link #setStackSize(AttachedItems, ItemStack, int, Action)}, as both {@link #growStack(int, Action)} and
     * {@link #shrinkStack(int, Action)} are wrapped through this method.
     */
    @Override
    protected int setStackSize(AttachedItems attachedItems, ItemStack current, int amount, Action action) {
        return super.setStackSize(attachedItems, current, amount, action.combine(!isCreative));
    }

    /**
     * @see BinInventorySlot#getBottomStack()
     */
    public ItemStack getBottomStack() {
        ItemStack current = getStack();
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return current.copyWithCount(Math.min(current.getCount(), current.getMaxStackSize()));
    }

    /**
     * For use by upgrade recipes
     *
     * @see BinInventorySlot#setLockStack(ItemStackTemplate)
     */
    public void setLockStack(@Nullable ItemStackTemplate stack) {
        if (stack == null) {
            attachedTo.remove(MekanismDataComponents.LOCK);
        } else {
            attachedTo.set(MekanismDataComponents.LOCK, LockData.create(stack));
        }
    }

    public @Nullable ItemStackTemplate getLockStack() {
        return attachedTo.getOrDefault(MekanismDataComponents.LOCK, LockData.EMPTY).lock();
    }

    @Override
    public void serialize(ValueOutput output) {
        super.serialize(output);
        ItemStackTemplate lockStack = getLockStack();
        if (lockStack != null) {
            output.store(SerializationConstants.LOCK_STACK, ItemStackTemplateHelper.NO_COUNT_CODEC, lockStack);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        //TODO - 26.1: Does this properly handle the behavior of when things are empty
        setLockStack(input.read(SerializationConstants.LOCK_STACK, ItemStackTemplateHelper.NO_COUNT_CODEC).orElse(null));
        super.deserialize(input);
    }
}