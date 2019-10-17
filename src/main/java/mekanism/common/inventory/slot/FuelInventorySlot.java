package mekanism.common.inventory.slot;

import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class FuelInventorySlot extends BasicInventorySlot {

    public static FuelInventorySlot forFuel(Function<@NonNull ItemStack, Integer> fuelValue, int x, int y) {
        return new FuelInventorySlot(item -> fuelValue.apply(item) == 0, item -> fuelValue.apply(item) > 0, x, y);
    }

    public static FuelInventorySlot forFuel(Function<@NonNull ItemStack, Integer> fuelValue, Predicate<@NonNull FluidStack> validFuel, int x, int y) {
        //TODO: Eventually maybe add a check for inserting to check against the tank of the inventory
        return new FuelInventorySlot(item -> {
            FluidStack fluidContained = FluidUtil.getFluidContained(item).orElse(FluidStack.EMPTY);
            //If we have no fuel stored, or it is not a valid fuel; and we also don't have a fuel value for our item
            // then allow it to be extracted as something went wrong.
            return (fluidContained.isEmpty() || !validFuel.test(fluidContained)) && fuelValue.apply(item) == 0;
        }, item -> {
            FluidStack fluidContained = FluidUtil.getFluidContained(item).orElse(FluidStack.EMPTY);
            if (fluidContained.isEmpty()) {
                return fuelValue.apply(item) > 0;
            }
            return validFuel.test(fluidContained) || fuelValue.apply(item) > 0;
        }, x, y);
    }

    private FuelInventorySlot(Predicate<@NonNull ItemStack> canExtract, @Nonnull Predicate<@NonNull ItemStack> validator, int x, int y) {
        super(canExtract, alwaysTrue, validator, x, y);
    }
}