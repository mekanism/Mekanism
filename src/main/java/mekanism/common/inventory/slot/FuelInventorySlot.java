package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IMekanismInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FuelInventorySlot extends BasicInventorySlot {

    public static FuelInventorySlot forFuel(ToIntFunction<@NonNull ItemStack> fuelValue, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(fuelValue, "Fuel value calculator cannot be null");
        return new FuelInventorySlot(stack -> fuelValue.applyAsInt(stack) == 0, stack -> fuelValue.applyAsInt(stack) > 0, inventory, x, y);
    }

    public static FuelInventorySlot forFuel(ToIntFunction<@NonNull ItemStack> fuelValue, Predicate<@NonNull FluidStack> validFuel, @Nullable IMekanismInventory inventory,
          int x, int y) {
        Objects.requireNonNull(fuelValue, "Fuel value calculator cannot be null");
        Objects.requireNonNull(validFuel, "Fuel validity check cannot be null");
        //TODO: Eventually maybe add a check for inserting to check against the tank of the inventory
        return new FuelInventorySlot(stack -> {
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
        }, inventory, x, y);
    }

    private FuelInventorySlot(Predicate<@NonNull ItemStack> canExtract, @Nonnull Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        //TODO: Re-evaluate this can extract
        super((stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack), alwaysTrueBi, validator, inventory, x, y);
    }
}