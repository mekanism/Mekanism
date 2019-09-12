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
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.common.Upgrade;
import mekanism.common.util.FieldsAreNonnullByDefault;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class GasToGasCachedRecipe extends CachedRecipe<GasToGasRecipe> {

    private final BiFunction<@NonNull GasStack, Boolean, Boolean> addToOutput;
    private final Supplier<@NonNull GasTank> inputTank;
    private final IntSupplier speedUpgrades;

    public GasToGasCachedRecipe(GasToGasRecipe recipe, BooleanSupplier canTileFunction, Consumer<Boolean> setActive, Runnable onFinish,
          Supplier<@NonNull GasTank> inputTank, IntSupplier speedUpgrades, BiFunction<@NonNull GasStack, Boolean, Boolean> addToOutput) {
        this(recipe, canTileFunction, () -> 0, () -> 0, () -> 1, setActive, energy -> {}, onFinish, inputTank, speedUpgrades, addToOutput);
    }

    public GasToGasCachedRecipe(GasToGasRecipe recipe, BooleanSupplier canTileFunction, DoubleSupplier perTickEnergy, DoubleSupplier storedEnergy,
          IntSupplier requiredTicks, Consumer<Boolean> setActive, DoubleConsumer useEnergy, Runnable onFinish, Supplier<@NonNull GasTank> inputTank,
          IntSupplier speedUpgrades, BiFunction<@NonNull GasStack, Boolean, Boolean> addToOutput) {
        super(recipe, canTileFunction, perTickEnergy, storedEnergy, requiredTicks, setActive, useEnergy, onFinish);
        this.inputTank = inputTank;
        this.addToOutput = addToOutput;
        this.speedUpgrades = speedUpgrades;
    }

    @Nonnull
    private GasTank getGasTank() {
        return inputTank.get();
    }

    private int getSpeedUpgrades() {
        return speedUpgrades.getAsInt();
    }

    @Override
    public boolean hasResourcesForTick() {
        GasStack gasInput = getGasTank().getGas();
        return gasInput != null && recipe.test(gasInput);
    }

    @Override
    public boolean hasRoomForOutput() {
        return addToOutput.apply(recipe.getOutput(getGasTank().getGas()), true);
    }

    @Override
    protected void finishProcessing() {
        GasTank inputTank = getGasTank();
        int possibleProcess = (int) Math.pow(2, getSpeedUpgrades());
        possibleProcess = Math.min(Math.min(inputTank.getStored(), outputTank.getNeeded()), possibleProcess);

        //TODO: Handle processing stuff
        addToOutput.apply(recipe.getOutput(getGasTank().getGas()), false);
    }
}