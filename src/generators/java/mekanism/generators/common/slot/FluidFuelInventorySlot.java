package mekanism.generators.common.slot;

import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of FluidInventorySlot to make it be able to handle raw items as fuels
 */
@NothingNullByDefault
public class FluidFuelInventorySlot extends FluidInventorySlot {

    public static FluidFuelInventorySlot forFuel(IExtendedFluidTank fluidTank, ToIntFunction<@NotNull ItemStack> fuelValue,
          IntFunction<@NotNull FluidStack> fuelCreator, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        Objects.requireNonNull(fuelCreator, "Fuel fluid stack creator cannot be null");
        Objects.requireNonNull(fuelValue, "Fuel value calculator cannot be null");
        Predicate<ItemStack> fillPredicate = getFillPredicate(fluidTank);
        return new FluidFuelInventorySlot(fluidTank, fuelValue, fuelCreator, stack -> {
            IFluidHandlerItem fluidHandlerItem = Capabilities.FLUID.getCapability(stack);
            if (fluidHandlerItem != null) {
                for (int tank = 0, tanks = fluidHandlerItem.getTanks(); tank < tanks; tank++) {
                    if (fluidTank.isFluidValid(fluidHandlerItem.getFluidInTank(tank))) {
                        //False if the items contents are still valid
                        return false;
                    }
                }
                //Only allow extraction if our item is out of fluid, but also verify there is no conversion for it
            }
            //Always allow extraction if something went horribly wrong, and we are not a fluid item AND we can't provide a valid type of chemical
            // This might happen after a reload for example
            return fuelValue.applyAsInt(stack) == 0;
        }, stack -> fuelValue.applyAsInt(stack) > 0 || fillPredicate.test(stack), listener, x, y);
    }

    private final IntFunction<@NotNull FluidStack> fuelCreator;
    private final ToIntFunction<@NotNull ItemStack> fuelValue;

    private FluidFuelInventorySlot(IExtendedFluidTank fluidTank, ToIntFunction<@NotNull ItemStack> fuelValue, IntFunction<@NotNull FluidStack> fuelCreator,
          Predicate<@NotNull ItemStack> canExtract, Predicate<@NotNull ItemStack> canInsert, @Nullable IContentsListener listener, int x, int y) {
        super(fluidTank, canExtract, canInsert, listener, x, y);
        this.fuelCreator = fuelCreator;
        this.fuelValue = fuelValue;
    }

    /**
     * Fills tank from slot, allowing for the item to also be converted to chemical if need be
     */
    public void fillOrBurn() {
        if (!isEmpty()) {
            int needed = fluidTank.getNeeded();
            //Fill the tank from the item
            if (needed > 0 && !fillTank()) {
                //If filling from item failed, try doing it by conversion
                int fuel = fuelValue.applyAsInt(current);
                if (fuel > 0 && fuel <= needed) {
                    boolean hasContainer = current.hasCraftingRemainingItem();
                    if (hasContainer && current.getCount() > 1) {
                        //If we have a container but have more than a single stack of it somehow just exit
                        return;
                    }
                    fluidTank.insert(fuelCreator.apply(fuel), Action.EXECUTE, AutomationType.INTERNAL);
                    if (hasContainer) {
                        //If the item has a container, then replace it with the container
                        setStack(current.getCraftingRemainingItem());
                    } else {
                        //Otherwise, shrink the size of the stack by one
                        MekanismUtils.logMismatchedStackSize(shrinkStack(1, Action.EXECUTE), 1);
                    }
                }
            }
        }
    }
}