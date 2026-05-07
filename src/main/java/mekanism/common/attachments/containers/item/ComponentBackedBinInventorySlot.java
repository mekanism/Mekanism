
package mekanism.common.attachments.containers.item;

import mekanism.api.AutomationType;
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
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

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
    public int insert(AttachedItems attachedItems, ItemResource currentType, int currentAmount, ItemResource resource, int amount, TransactionContext transaction,
          AutomationType automationType) {
        if (currentType.isEmpty()) {
            ItemResource lockType = getLockType();
            if (!lockType.isEmpty() && !resource.equals(lockType)) {
                // When locked, we need to make sure the correct item type is being inserted
                return 0;
            } else if (isCreative && automationType != AutomationType.EXTERNAL) {
                //If a player manually inserts into a creative bin, that is empty we need to allow setting the type,
                // Note: We check that it is not external insertion because an empty creative bin acts as a "void" for automation
                int limit = getLimit(resource);
                //Try to insert the entire limit so that then it just updates to being a full stack
                int inserted = super.insert(attachedItems, currentType, currentAmount, resource, limit, transaction, automationType);
                //If we did manage to insert anything then return that we inserted the entire amount that we were passed
                return inserted == 0 ? 0 : amount;
            }
        }
        if (isCreative) {
            //Return the result without actually changing the contents (accepting without providing any changes
            try (Transaction simulation = Transaction.open(transaction)) {
                return super.insert(attachedItems, currentType, currentAmount, resource, amount, simulation, automationType);
            }
        }
        return super.insert(attachedItems, currentType, currentAmount, resource, amount, transaction, automationType);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction, AutomationType automationType) {
        if (isCreative) {
            try (Transaction simulation = Transaction.open(transaction)) {
                //Use a sub transaction that is not committed to effectively just simulate what will happen without making any changes
                return super.extract(resource, amount, simulation, automationType);
            }
        }
        return super.extract(resource, amount, transaction, automationType);
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
        ItemStack stack = getStack();
        ItemResource current = ItemResource.of(stack);
        return current.toStack(Math.min(stack.count(), current.getMaxStackSize()));
    }

    /**
     * For use by upgrade recipes
     *
     * @see BinInventorySlot#setLockStack(ItemStackTemplate)
     */
    public void setLockType(ItemResource lockType) {
        if (lockType.isEmpty()) {
            attachedTo.remove(MekanismDataComponents.LOCK);
        } else {
            attachedTo.set(MekanismDataComponents.LOCK, LockData.create(lockType));
        }
    }

    public ItemResource getLockType() {
        return attachedTo.getOrDefault(MekanismDataComponents.LOCK, LockData.EMPTY).lock();
    }

    @Override
    public void serialize(ValueOutput output) {
        super.serialize(output);
        ItemResource lockType = getLockType();
        if (!lockType.isEmpty()) {
            output.store(SerializationConstants.LOCK_TYPE, ItemResource.CODEC, lockType);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        //TODO - 26.1: Does this properly handle the behavior of when things are empty
        setLockType(input.read(SerializationConstants.LOCK_TYPE, ItemResource.CODEC).orElse(ItemResource.EMPTY));
        super.deserialize(input);
    }
}