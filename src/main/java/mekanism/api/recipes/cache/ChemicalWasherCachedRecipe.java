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
import mekanism.api.recipes.ChemicalWasherRecipe;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraftforge.fluids.FluidTank;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ChemicalWasherCachedRecipe extends CachedRecipe<ChemicalWasherRecipe> {

    private final BiFunction<@NonNull GasStack, Boolean, Boolean> addToOutput;
    private final Supplier<@NonNull FluidTank> cleansingTank;
    private final Supplier<@NonNull GasTank> inputTank;
    private final IntSupplier maxOperations;

    public ChemicalWasherCachedRecipe(ChemicalWasherRecipe recipe, BooleanSupplier canTileFunction, DoubleSupplier perTickEnergy, DoubleSupplier storedEnergy,
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
        return gasInput != null && recipe.test(, gasInput);
    }

    @Override
    public boolean hasRoomForOutput() {
        return addToOutput.apply(recipe.getOutput(, getGasTank().getGas()), true);
    }

    @Override
    protected void finishProcessing() {
        addToOutput.apply(recipe.getOutput(, getGasTank().getGas()), false);
    }
}