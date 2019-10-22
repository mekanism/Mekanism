package mekanism.common.inventory.slot;

import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IMekanismInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class FuelInventorySlot extends BasicInventorySlot {

    public static FuelInventorySlot forFuel(Function<@NonNull ItemStack, Integer> fuelValue, IMekanismInventory inventory, int x, int y) {
        return new FuelInventorySlot(stack -> fuelValue.apply(stack) == 0, stack -> fuelValue.apply(stack) > 0, inventory, x, y);
    }

    public static FuelInventorySlot forFuel(Function<@NonNull ItemStack, Integer> fuelValue, Predicate<@NonNull FluidStack> validFuel, IMekanismInventory inventory,
          int x, int y) {
        //TODO: Eventually maybe add a check for inserting to check against the tank of the inventory
        return new FuelInventorySlot(stack -> {
            FluidStack fluidContained = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
            //If we have no fuel stored, or it is not a valid fuel; and we also don't have a fuel value for our item
            // then allow it to be extracted as something went wrong.
            return (fluidContained.isEmpty() || !validFuel.test(fluidContained)) && fuelValue.apply(stack) == 0;
        }, stack -> {
            FluidStack fluidContained = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
            if (fluidContained.isEmpty()) {
                return fuelValue.apply(stack) > 0;
            }
            return validFuel.test(fluidContained) || fuelValue.apply(stack) > 0;
        }, inventory, x, y);
    }

    private FuelInventorySlot(Predicate<@NonNull ItemStack> canExtract, @Nonnull Predicate<@NonNull ItemStack> validator, IMekanismInventory inventory, int x, int y) {
        //TODO: Re-evaluate this can extract
        super((stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack), alwaysTrueBi, validator, inventory, x, y);
    }
}