package mekanism.api.recipes.cache;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.recipes.FluidGasToGasRecipe;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidGasToGasCachedRecipe extends CachedRecipe<FluidGasToGasRecipe> {

    private final BiFunction<@NonNull GasStack, Boolean, Boolean> addToOutput;
    private final Supplier<@NonNull FluidTank> fluidTank;
    private final Supplier<@NonNull GasTank> gasTank;

    public FluidGasToGasCachedRecipe(FluidGasToGasRecipe recipe, Supplier<@NonNull FluidTank> fluidTank, Supplier<@NonNull GasTank> gasTank,
          BiFunction<@NonNull GasStack, Boolean, Boolean> addToOutput) {
        super(recipe);
        this.fluidTank = fluidTank;
        this.gasTank = gasTank;
        this.addToOutput = addToOutput;
    }

    @Nonnull
    private GasTank getGasTank() {
        return gasTank.get();
    }

    @Nonnull
    private FluidTank getFluidTank() {
        return fluidTank.get();
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        //TODO: Move hasResourcesForTick and hasRoomForOutput into this calculation
        //TODO: Parts that need to be checked: gasInput, fluidInput, output, energy (checked above)
        return 1;
    }

    @Override
    public boolean hasResourcesForTick() {
        GasStack gasInput = getGasTank().getGas();
        if (gasInput == null) {
            return false;
        }
        FluidStack fluidStack = getFluidTank().getFluid();
        if (fluidStack == null || fluidStack.amount == 0) {
            return false;
        }
        return recipe.test(fluidStack, gasInput);
    }

    @Override
    public boolean hasRoomForOutput() {
        return addToOutput.apply(recipe.getOutput(getFluidTank().getFluid(), getGasTank().getGas()), true);
    }

    @Override
    protected void finishProcessing(int operations) {
        addToOutput.apply(recipe.getOutput(getFluidTank().getFluid(), getGasTank().getGas()), false);
    }
}