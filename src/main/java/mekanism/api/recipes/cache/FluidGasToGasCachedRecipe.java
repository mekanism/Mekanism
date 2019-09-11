package mekanism.api.recipes.cache;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
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
    private final Supplier<@NonNull FluidTank> cleansingTank;
    private final Supplier<@NonNull GasTank> inputTank;
    private final IntSupplier maxOperations;

    public FluidGasToGasCachedRecipe(FluidGasToGasRecipe recipe, BooleanSupplier canTileFunction, DoubleSupplier perTickEnergy, DoubleSupplier storedEnergy,
          IntSupplier requiredTicks, Consumer<Boolean> setActive, DoubleConsumer useEnergy, Runnable onFinish, Supplier<@NonNull FluidTank> cleansingTank,
          Supplier<@NonNull GasTank> inputTank, IntSupplier maxOperations, BiFunction<@NonNull GasStack, Boolean, Boolean> addToOutput) {
        super(recipe, canTileFunction, perTickEnergy, storedEnergy, requiredTicks, setActive, useEnergy, onFinish);
        this.cleansingTank = cleansingTank;
        this.inputTank = inputTank;
        this.addToOutput = addToOutput;
        this.maxOperations = maxOperations;
    }

    @Nonnull
    private GasTank getGasTank() {
        return inputTank.get();
    }

    @Nonnull
    private FluidTank getCleansingTank() {
        return cleansingTank.get();
    }

    private int getMaxOperations() {
        //TODO: Use this
        return maxOperations.getAsInt();
    }

    @Override
    public boolean hasResourcesForTick() {
        GasStack gasInput = getGasTank().getGas();
        if (gasInput == null) {
            return false;
        }
        FluidStack fluidStack = getCleansingTank().getFluid();
        if (fluidStack == null || fluidStack.amount == 0) {
            return false;
        }
        return recipe.test(fluidStack, gasInput);
    }

    @Override
    public boolean hasRoomForOutput() {
        return addToOutput.apply(recipe.getOutput(getCleansingTank().getFluid(), getGasTank().getGas()), true);
    }

    @Override
    protected void finishProcessing() {
        //TODO: Use/calculate proper amount here based on max operations, as in the has resources/has room we only care if it has enough for at least one operation
        int maxOperations = getMaxOperations();
        addToOutput.apply(recipe.getOutput(getCleansingTank().getFluid(), getGasTank().getGas()), false);
    }
}