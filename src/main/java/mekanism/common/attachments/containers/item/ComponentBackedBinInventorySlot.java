
package mekanism.common.attachments.containers.item;

import com.google.common.primitives.Ints;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.attachments.LockData;
import mekanism.common.attachments.containers.AttachedResources;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.BinTier;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class ComponentBackedBinInventorySlot extends ComponentBackedInventorySlot {

    private final boolean isCreative;

    public static ComponentBackedBinInventorySlot create(ContainerType<?, ?, ?> ignored, ItemAccess attachedAccess, int tankIndex) {
        if (!(attachedAccess.getResource().getItem() instanceof ItemBlockBin item)) {
            throw new IllegalStateException("Attached to should always be a bin item");
        }
        return new ComponentBackedBinInventorySlot(attachedAccess, tankIndex, item.getTier());
    }

    private ComponentBackedBinInventorySlot(ItemAccess attachedAccess, int slotIndex, BinTier tier) {
        super(attachedAccess, slotIndex, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), BinInventorySlot.validator, false, tier.getStorage());
        isCreative = tier == BinTier.CREATIVE;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    protected int insert(AttachedResources<ItemResource> attachedItems, ItemResource currentType, @Range(from = 0, to = Long.MAX_VALUE) long currentAmount, ItemResource resource,
          @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        if (currentType.isEmpty()) {
            ItemResource lockType = getLockType();
            if (!lockType.isEmpty() && !resource.equals(lockType)) {
                // When locked, we need to make sure the correct item type is being inserted
                return 0;
            } else if (isCreative && !automationType.isExternal()) {
                //If a player manually inserts into a creative bin, that is empty we need to allow setting the type,
                // Note: We check that it is not external insertion because an empty creative bin acts as a "void" for automation
                try (Transaction simulation = Transaction.open(transaction)) {
                    if (super.insert(attachedItems, currentType, currentAmount, resource, amount, simulation, automationType) == 0) {
                        return 0;
                    }
                }
                //If we managed to insert anything, set the contents to the maximum amount of that item type
                if (setContents(attachedItems, resource, capacityAsLong(resource), transaction)) {
                    //Return that we accepted the entire amount we were passed
                    return amount;
                }
                //If we couldn't update the backing item access, return that we didn't actually insert anything
                return 0;
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
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int extract(ItemResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
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
        LargeResourceStack<ItemResource> stack = asStack();
        ItemResource current = stack.resource();
        return current.toStack(Math.min(Ints.saturatedCast(stack.amount()), current.getMaxStackSize()));
    }

    /**
     * For use by upgrade recipes
     *
     * @see BinInventorySlot#setLockType(ItemResource)
     */
    public boolean setLockType(ItemResource lockType) {
        //Note: The attached access should handle snapshotting the backing stack
        //If anything changed in the item access, that means it was able to perform the transfer, so return that things changed from the call to setContents
        ItemResource resource = attachedAccess.getResource();
        if (lockType.isEmpty()) {
            return ItemAccessUtils.exchange(attachedAccess, resource.without(MekanismDataComponents.LOCK), null);
        }
        return ItemAccessUtils.exchange(attachedAccess, resource.with(MekanismDataComponents.LOCK, LockData.create(lockType)), null);
    }

    public ItemResource getLockType() {
        return attachedAccess.getResource().getOrDefault(MekanismDataComponents.LOCK, LockData.EMPTY).lock();
    }

    @Override
    public void copyContents(IResourceContainer<ItemResource> other) {
        super.copyContents(other);
        if (other instanceof ComponentBackedBinInventorySlot otherSlot) {
            setLockType(otherSlot.getLockType());
        } else if (other instanceof BinInventorySlot otherSlot) {
            setLockType(otherSlot.getLockType());
        }
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
        setLockType(input.read(SerializationConstants.LOCK_TYPE, ItemResource.CODEC).orElse(ItemResource.EMPTY));
        super.deserialize(input);
    }
}