package mekanism.generators.common.slot;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.NonNull;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.inventory.slot.FluidInventorySlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

/**
 * Extension of FluidInventorySlot to make it be able to handle raw items as fuels
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidFuelInventorySlot extends FluidInventorySlot {

    public static FluidFuelInventorySlot forFuel(IExtendedFluidTank fluidTank, ToIntFunction<@NonNull ItemStack> fuelValue,
          Int2ObjectFunction<@NonNull FluidStack> fuelCreator, Predicate<@NonNull FluidStack> validFuel, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        Objects.requireNonNull(fuelCreator, "Fuel fluid stack creator cannot be null");
        Objects.requireNonNull(fuelValue, "Fuel value calculator cannot be null");
        Objects.requireNonNull(validFuel, "Fuel validity check cannot be null");
        //TODO: Eventually maybe add a check for inserting to check against the tank of the inventory
        //TODO: FluidHandler - improve the logic of these checks, and remove the need for passing a valid fuel predicate, due to it being
        // contained by our fluid tank
        //TODO: FluidHandler - Evaluate usage of getFluidContained, as if it is rate limited this won't return the actual amount of fluid it has contained
        // and some places we may care about that
        return new FluidFuelInventorySlot(fluidTank, fuelValue, fuelCreator, stack -> {
            FluidStack fluidContained = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
            //If we have no fuel stored, or it is not a valid fuel; and we also don't have a fuel value for our item
            // then allow it to be extracted as something went wrong.
            return (fluidContained.isEmpty() || !validFuel.test(fluidContained)) && fuelValue.applyAsInt(stack) == 0;
        }, stack -> {
            FluidStack fluidContained = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
            if (fluidContained.isEmpty()) {
                return fuelValue.applyAsInt(stack) > 0;
            }
            return validFuel.test(fluidContained) || fuelValue.applyAsInt(stack) > 0;
        }, stack -> {
            //Allow for all fluid containers and items with a fuel value to be valid, so that we don't crash
            // on empty buckets after draining, but we can't insert them because of the more accurate insertion checks
            return FluidUtil.getFluidHandler(stack).isPresent() || fuelValue.applyAsInt(stack) > 0;
        }, inventory, x, y);
    }

    private final Int2ObjectFunction<@NonNull FluidStack> fuelCreator;
    private final ToIntFunction<@NonNull ItemStack> fuelValue;

    private FluidFuelInventorySlot(IExtendedFluidTank fluidTank, ToIntFunction<@NonNull ItemStack> fuelValue,  Int2ObjectFunction<@NonNull FluidStack> fuelCreator,
          Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert, Predicate<@NonNull ItemStack> validator,
          @Nullable IMekanismInventory inventory, int x, int y) {
        super(fluidTank, canExtract, canInsert, validator, inventory, x, y);
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
                    boolean hasContainer = current.hasContainerItem();
                    if (hasContainer && current.getCount() > 1) {
                        //If we have a container but have more than a single stack of it somehow
                        // just exit
                        return;
                    }
                    fluidTank.insert(fuelCreator.apply(fuel), Action.EXECUTE, AutomationType.INTERNAL);
                    if (hasContainer) {
                        //If the item has a container, then replace it with the container
                        setStack(current.getContainerItem());
                    } else {
                        //Otherwise shrink the size of the stack by one
                        if (shrinkStack(1, Action.EXECUTE) != 1) {
                            //TODO: Print error that something went wrong
                        }
                    }
                }
            }
        }
    }
}