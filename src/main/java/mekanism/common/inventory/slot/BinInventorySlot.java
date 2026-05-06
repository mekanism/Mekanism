package mekanism.common.inventory.slot;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.containers.item.ComponentBackedBinInventorySlot;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.tier.BinTier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BinInventorySlot extends BasicInventorySlot {

    public static final Predicate<@NotNull ItemResource> validator = itemType -> !(itemType.getItem() instanceof ItemBlockBin);

    @Nullable
    public static ComponentBackedBinInventorySlot getForStack(@NotNull ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemBlockBin) {
            IMekanismInventory attachment = ContainerType.ITEM.createHandler(stack);
            if (attachment != null) {
                List<IInventorySlot> slots = attachment.getInventorySlots();
                if (slots.size() == 1) {
                    IInventorySlot slot = slots.getFirst();
                    if (slot instanceof ComponentBackedBinInventorySlot binSlot) {
                        return binSlot;
                    }
                }
            }
        }
        return null;
    }

    public static BinInventorySlot create(@Nullable IContentsListener listener, BinTier tier) {
        Objects.requireNonNull(tier, "Bin tier cannot be null");
        return new BinInventorySlot(listener, tier);
    }

    private final boolean isCreative;
    private ItemResource lockType = ItemResource.EMPTY;

    private BinInventorySlot(@Nullable IContentsListener listener, BinTier tier) {
        super(tier.getStorage(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), validator, listener, 0, 0);
        isCreative = tier == BinTier.CREATIVE;
        obeyStackLimit = false;
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) {
            //"Fail quick" if there is nothing to add
            return 0;
        }
        if (isEmpty()) {
            if (isLocked() && !lockType.equals(resource)) {
                // When locked, we need to make sure the correct item type is being inserted
                return 0;
            } else if (isCreative && automationType != AutomationType.EXTERNAL) {
                //If a player manually inserts into a creative bin, that is empty we need to allow setting the type,
                // Note: We check that it is not external insertion because an empty creative bin acts as a "void" for automation
                int limit = getLimit(resource);
                //Try to insert the entire limit so that then it just updates to being a full stack
                int inserted = super.insert(resource, limit, transaction, automationType);
                //If we did manage to insert anything then return that we inserted the entire amount that we were passed
                return inserted == 0 ? 0 : amount;
            }
        }
        if (isCreative) {
            //Return the result without actually changing the contents (accepting without providing any changes)
            try (Transaction simulation = Transaction.open(transaction)) {
                return super.insert(resource, amount, simulation, automationType);
            }
        }
        return super.insert(resource, amount, transaction, automationType);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction, AutomationType automationType) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (isEmpty() || amount == 0) {
            //"Fail quick" if we are empty, nothing is being extracted
            return 0;
        }
        if (isCreative) {
            try (Transaction simulation = Transaction.open(transaction)) {
                //Use a sub transaction that is not committed to effectively just simulate what will happen without making any changes
                return super.extract(resource, amount, simulation, automationType);
            }
        }
        return super.extract(resource, amount, transaction, automationType);
    }

    @Override
    public int setStackSize(int amount, TransactionContext transaction) {
        if (isCreative) {
            try (Transaction simulation = Transaction.open(transaction)) {
                //Use a sub transaction that is not committed to effectively just simulate what will happen without making any changes
                return super.setStackSize(amount, simulation);
            }
        }
        return super.setStackSize(amount, transaction);
    }

    @Override
    public int getCurrentLimit() {
        return getLimit(getBinItemType());
    }

    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot() {
        return null;
    }

    /**
     * Modifies the lock state of the slot.
     *
     * @param lock if the slot should be locked
     *
     * @return if the lock state was modified
     */
    public boolean setLocked(boolean lock) {
        // Don't lock if:
        // - We are a creative bin
        // - We already have the same state as the one we're supposed to switch to
        // - We were asked to lock, but we're empty
        if (isCreative || isLocked() == lock || (lock && isEmpty())) {
            return false;
        }
        lockType = lock ? getResource() : ItemResource.EMPTY;
        return true;
    }

    /**
     * For use by tier installers and parsing placement data, do not use this in place of {@link #setLocked(boolean)}
     */
    public void setLockStack(@Nullable ItemStackTemplate template) {
        lockType = template == null ? ItemResource.EMPTY : ItemResource.of(template);
    }

    /**
     * For use by tier installers and parsing placement data, do not use this in place of {@link #setLocked(boolean)}
     */
    public void setLockStack(ItemResource lockType) {
        this.lockType = lockType;
    }

    public boolean isLocked() {
        return !lockType.isEmpty();
    }

    public ItemResource getBinItemType() {
        return isLocked() ? lockType : getResource();
    }

    public ItemResource getLockType() {
        return lockType;
    }

    @Override
    public void serialize(ValueOutput output) {
        //Note: While we are able to store this extra data for saving and stuff, when converting to an item we need to have
        // the tile copy the lock stack as a component
        super.serialize(output);
        if (isLocked()) {
            output.store(SerializationConstants.LOCK_TYPE, ItemResource.CODEC, lockType);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        //TODO - 26.1: Does this properly handle the behavior of when things are empty
        this.lockType = input.read(SerializationConstants.LOCK_TYPE, ItemResource.CODEC).orElse(ItemResource.EMPTY);
        super.deserialize(input);
    }
}