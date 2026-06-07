package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.transaction.RateLimitTracker;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.entity.FuelValues;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FuelInventorySlot extends BasicInventorySlot {

    public static FuelInventorySlot forFuel(ToIntFunction<ItemResource> fuelValue, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fuelValue, "Fuel value calculator cannot be null");
        return new FuelInventorySlot((itemType, automationType) -> !automationType.isExternal() || fuelValue.applyAsInt(itemType) == 0,
              (itemType, automationType) -> automationType.isInternal() || fuelValue.applyAsInt(itemType) != 0, ConstantPredicates.alwaysTrue(), null, null, listener, x, y);
    }

    private FuelInventorySlot(BiPredicate<ItemResource, AutomationType> canExtract, BiPredicate<ItemResource, AutomationType> canInsert, Predicate<ItemResource> validator,
          @Nullable RateLimitTracker insertionRateLimiter, @Nullable RateLimitTracker extractionRateLimiter, @Nullable IContentsListener listener, int x, int y) {
        super(canExtract, canInsert, validator, insertionRateLimiter, extractionRateLimiter, listener, x, y);
    }

    public int burn(FuelValues fuelValues, @Nullable TransactionContext transaction) {
        if (!isEmpty()) {
            int burnTime = resource().toStack().getBurnTime(null, fuelValues) / 2;
            if (burnTime > 0) {
                try (Transaction subTransaction = Transaction.open(transaction)) {
                    if (consumeAndReplace(this, subTransaction)) {
                        subTransaction.commit();
                        return burnTime;
                    }
                }
            }
        }
        return 0;
    }

    /// @param fuelSlot    Slot to consume an item of, and then attempt to insert any remainder into
    /// @param transaction Transaction in charge of managing whether changes go through or are rolled back
    ///
    /// @return Whether consuming and replacing was successful, or if the transaction should be aborted and allowed to roll back.
    public static boolean consumeAndReplace(IInventorySlot fuelSlot, Transaction transaction) {
        if (fuelSlot.isEmpty()) {
            return false;
        }
        ItemResource currentType = fuelSlot.resource();
        //Try to consume the current item
        if (fuelSlot.extract(currentType, 1, transaction, AutomationType.INTERNAL) == 0) {
            return false;
        }
        ItemStackTemplate container = currentType.toStack().getCraftingRemainder();
        if (container == null) {
            //No remainder, we can just return that consuming was successful
            return true;
        }
        //If the item has a container, then try to insert the container, if there was more than one of the current type stored
        // this will fail unless for some reason the item is being converted into itself and is effectively an infinite source
        int containerSize = container.count();
        //If we couldn't insert the entire use remainder, return that the transaction should bail
        return fuelSlot.insert(ItemResource.of(container), containerSize, transaction, AutomationType.INTERNAL) == containerSize;
    }
}