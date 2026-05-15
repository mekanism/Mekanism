package mekanism.generators.common.slot;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IFluidTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.FuelInventorySlot;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of FluidInventorySlot to make it be able to handle raw items as fuels
 */
@NothingNullByDefault
public class FluidFuelInventorySlot extends FluidInventorySlot {

    public static FluidFuelInventorySlot forFuel(IFluidTank fluidTank, ToIntFunction<ItemResource> fuelValue,
          FluidResource fuelType, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        Objects.requireNonNull(fuelType, "Fuel fluid type cannot be null");
        Objects.requireNonNull(fuelValue, "Fuel value calculator cannot be null");
        if (fuelType.isEmpty()) {
            throw new IllegalArgumentException("Fuel fluid type cannot be empty");
        }
        Predicate<ItemResource> fillPredicate = getFillPredicate(fluidTank);
        return new FluidFuelInventorySlot(fluidTank, fuelType, fuelValue, itemType -> {
            ResourceHandler<FluidResource> itemHandler = Capabilities.FLUID.getCapability(itemType);
            if (itemHandler != null) {
                for (int tank = 0, tanks = itemHandler.size(); tank < tanks; tank++) {
                    FluidResource fluidType = itemHandler.getResource(tank);
                    if (!fluidType.isEmpty() && fluidTank.isValid(fluidType)) {
                        //False if the items contents are still valid
                        return false;
                    }
                }
                //Only allow extraction if our item is out of fluid, but also verify there is no conversion for it
            }
            //Always allow extraction if something went horribly wrong, and we are not a fluid item AND we can't provide a valid type of chemical
            // This might happen after a reload for example
            return fuelValue.applyAsInt(itemType) == 0;
        }, itemType -> fuelValue.applyAsInt(itemType) > 0 || fillPredicate.test(itemType), listener, x, y);
    }

    private final ToIntFunction<ItemResource> fuelValue;
    private final FluidResource fuelType;

    private FluidFuelInventorySlot(IFluidTank fluidTank, FluidResource fuelType, ToIntFunction<ItemResource> fuelValue, Predicate<ItemResource> canExtract,
          Predicate<ItemResource> canInsert, @Nullable IContentsListener listener, int x, int y) {
        super(fluidTank, canExtract, canInsert, listener, x, y);
        this.fuelType = fuelType;
        this.fuelValue = fuelValue;
    }

    /**
     * Fills tank from slot, allowing for the item to also be converted to fluid if need be
     */
    public void fillOrBurn() {
        if (!isEmpty()) {
            int needed = fluidTank.getNeededAsInt(fluidTank.getResource());
            //Fill the tank from the item
            if (needed > 0 && !fillTank()) {
                //If filling from item failed, try doing it by conversion
                ItemResource currentType = getResource();
                int fuel = fuelValue.applyAsInt(currentType);
                if (fuel > 0 && fuel <= needed) {
                    try (Transaction transaction = Transaction.openRoot()) {
                        if (FuelInventorySlot.consumeAndReplace(this, transaction)) {
                            int inserted = fluidTank.insert(fuelType, fuel, transaction, AutomationType.INTERNAL);
                            if (inserted == fuel) {
                                //If we were able to insert it all the fuel into the fluid tank, commit all of the changes
                                transaction.commit();
                            }
                        }
                    }
                }
            }
        }
    }
}