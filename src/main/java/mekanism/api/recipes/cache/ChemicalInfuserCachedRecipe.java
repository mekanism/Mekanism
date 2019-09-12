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
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.common.util.FieldsAreNonnullByDefault;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ChemicalInfuserCachedRecipe extends CachedRecipe<ChemicalInfuserRecipe> {

    private final BiFunction<@NonNull GasStack, Boolean, Boolean> addToOutput;
    private final Supplier<@NonNull GasTank> leftTank;
    private final Supplier<@NonNull GasTank> rightTank;
    private final IntSupplier speedUpgrades;

    public ChemicalInfuserCachedRecipe(ChemicalInfuserRecipe recipe, BooleanSupplier canTileFunction, DoubleSupplier perTickEnergy, DoubleSupplier storedEnergy,
          Consumer<Boolean> setActive, DoubleConsumer useEnergy, Runnable onFinish, Supplier<@NonNull GasTank> leftTank, Supplier<@NonNull GasTank> rightTank,
          IntSupplier speedUpgrades, BiFunction<@NonNull GasStack, Boolean, Boolean> addToOutput) {
        this(recipe, canTileFunction, perTickEnergy, storedEnergy, () -> 1, setActive, useEnergy, onFinish, leftTank, rightTank, speedUpgrades, addToOutput);
    }

    public ChemicalInfuserCachedRecipe(ChemicalInfuserRecipe recipe, BooleanSupplier canTileFunction, DoubleSupplier perTickEnergy, DoubleSupplier storedEnergy,
          IntSupplier requiredTicks, Consumer<Boolean> setActive, DoubleConsumer useEnergy, Runnable onFinish, Supplier<@NonNull GasTank> leftTank,
          Supplier<@NonNull GasTank> rightTank, IntSupplier speedUpgrades, BiFunction<@NonNull GasStack, Boolean, Boolean> addToOutput) {
        super(recipe, canTileFunction, perTickEnergy, storedEnergy, requiredTicks, setActive, useEnergy, onFinish);
        this.leftTank = leftTank;
        this.rightTank = rightTank;
        this.speedUpgrades = speedUpgrades;
        this.addToOutput = addToOutput;
    }

    @Nonnull
    private GasTank getLeftTank() {
        return leftTank.get();
    }

    @Nonnull
    private GasTank getRightTank() {
        return rightTank.get();
    }

    private int getSpeedUpgrades() {
        return speedUpgrades.getAsInt();
    }

    @Override
    public boolean hasResourcesForTick() {
        GasStack leftInput = getLeftTank().getGas();
        GasStack rightInput = getRightTank().getGas();
        return leftInput != null && rightInput != null && recipe.test(leftInput, rightInput);
    }

    @Override
    public boolean hasRoomForOutput() {
        return addToOutput.apply(recipe.getOutput(getLeftTank().getGas(), getRightTank().getGas()), true);
    }

    @Override
    protected void finishProcessing() {
        GasTank leftTank = getLeftTank();
        GasTank rightTank = getRightTank();
        int possibleProcess = (int) Math.pow(2, getSpeedUpgrades());
        GasStackIngredient leftInput = recipe.getLeftInput();
        GasStackIngredient rightInput = recipe.getRightInput();
        if (leftInput.testType(leftTank.getGasType())) {
            //If the "left input" is in the left tank
            possibleProcess = Math.min(leftTank.getStored() / leftInput.amount, possibleProcess);
            possibleProcess = Math.min(rightTank.getStored() / rightInput.amount, possibleProcess);
        } else {
            //If the "left input" is actually in the right tank
            possibleProcess = Math.min(leftTank.getStored() / rightInput.amount, possibleProcess);
            possibleProcess = Math.min(rightTank.getStored() / leftInput.amount, possibleProcess);
        }
        possibleProcess = Math.min(centerTank.getNeeded() / recipe.getOutput(leftInput, rightInput).amount, possibleProcess);
        possibleProcess = Math.min((int) (getStoredElectricity() / getEnergyPerTick()), possibleProcess);

        //TODO: Handle processing stuff
        addToOutput.apply(recipe.getOutput(getLeftTank().getGas(), getRightTank().getGas()), false);
    }
}