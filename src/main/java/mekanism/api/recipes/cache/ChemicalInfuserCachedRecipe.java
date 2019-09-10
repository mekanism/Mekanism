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
import mekanism.common.util.FieldsAreNonnullByDefault;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ChemicalInfuserCachedRecipe extends CachedRecipe<ChemicalInfuserRecipe> {

    private final BiFunction<@NonNull GasStack, Boolean, Boolean> addToOutput;
    private final Supplier<@NonNull GasTank> leftTank;
    private final Supplier<@NonNull GasTank> rightTank;

    public ChemicalInfuserCachedRecipe(ChemicalInfuserRecipe recipe, BooleanSupplier canTileFunction, DoubleSupplier perTickEnergy, DoubleSupplier storedEnergy,
          IntSupplier requiredTicks, Consumer<Boolean> setActive, DoubleConsumer useEnergy, Runnable onFinish, Supplier<@NonNull GasTank> leftTank,
          Supplier<@NonNull GasTank> rightTank, BiFunction<@NonNull GasStack, Boolean, Boolean> addToOutput) {
        super(recipe, canTileFunction, perTickEnergy, storedEnergy, requiredTicks, setActive, useEnergy, onFinish);
        this.leftTank = leftTank;
        this.rightTank = rightTank;
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
        addToOutput.apply(recipe.getOutput(getLeftTank().getGas(), getRightTank().getGas()), false);
    }
}