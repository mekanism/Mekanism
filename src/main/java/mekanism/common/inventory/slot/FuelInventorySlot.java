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
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.level.block.entity.FuelValues;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FuelInventorySlot extends BasicInventorySlot {

    public static FuelInventorySlot forFuel(ToIntFunction<ItemResource> fuelValue, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fuelValue, "Fuel value calculator cannot be null");
        return new FuelInventorySlot((itemType, automationType) -> automationType.isManual() || fuelValue.applyAsInt(itemType) == 0,
              (itemType, _) -> fuelValue.applyAsInt(itemType) != 0, ConstantPredicates.alwaysTrue(), listener, x, y);
    }

    private FuelInventorySlot(BiPredicate<ItemResource, AutomationType> canExtract, BiPredicate<ItemResource, AutomationType> canInsert, Predicate<ItemResource> validator,
          @Nullable IContentsListener listener, int x, int y) {
        super(canExtract, canInsert, validator, listener, x, y);
    }

    public int burn(FuelValues fuelValues) {
        if (!isEmpty()) {
            int burnTime = resource().toStack().getBurnTime(null, fuelValues) / 2;
            if (burnTime > 0) {
                try (Transaction transaction = Transaction.openRoot()) {
                    if (consumeAndReplace(this, transaction)) {
                        transaction.commit();
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
        int extracted = fuelSlot.extract(currentType, 1, transaction, AutomationType.INTERNAL);
        if (extracted != 1) {
            return false;
        }
        UseRemainder remainder = currentType.get(DataComponents.USE_REMAINDER);
        if (remainder == null) {
            return true;
        }
        //If the item has a container, then try to insert the container, if there was more than one of the current type stored
        // this will fail unless for some reason the item is being converted into itself and is effectively an infinite source
        ItemStackTemplate container = remainder.convertInto();
        int inserted = fuelSlot.insert(ItemResource.of(container), container.count(), transaction, AutomationType.INTERNAL);
        //If we couldn't insert the entire use remainder, return that the transaction should bail
        return inserted == container.count();
    }
}