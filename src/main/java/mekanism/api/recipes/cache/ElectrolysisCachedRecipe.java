package mekanism.api.recipes.cache;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.apache.commons.lang3.tuple.Pair;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class ElectrolysisCachedRecipe extends CachedRecipe<ElectrolysisRecipe> {

    private final BiFunction<@NonNull Pair<GasStack, GasStack>, Boolean, Boolean> addToOutput;
    private final Supplier<@NonNull FluidTank> inputTank;
    private final IntSupplier speedUpgrades;

    public ElectrolysisCachedRecipe(ElectrolysisRecipe recipe, BooleanSupplier canTileFunction, DoubleSupplier perTickEnergy, DoubleSupplier storedEnergy,
          IntSupplier requiredTicks, Consumer<Boolean> setActive, DoubleConsumer useEnergy, Runnable onFinish, Supplier<@NonNull FluidTank> inputTank,
          IntSupplier speedUpgrades, BiFunction<@NonNull Pair<GasStack, GasStack>, Boolean, Boolean> addToOutput) {
        super(recipe, canTileFunction, perTickEnergy, storedEnergy, requiredTicks, setActive, useEnergy, onFinish);
        this.inputTank = inputTank;
        this.speedUpgrades = speedUpgrades;
        this.addToOutput = addToOutput;
    }

    private FluidTank getTank() {
        return inputTank.get();
    }

    private int getSpeedUpgrades() {
        return speedUpgrades.getAsInt();
    }

    @Override
    public boolean hasResourcesForTick() {
        FluidStack fluidStack = getTank().getFluid();
        return fluidStack != null && recipe.test(fluidStack);
    }

    @Override
    public boolean hasRoomForOutput() {
        return addToOutput.apply(recipe.getOutput(getTank().getFluid()), true);
    }

    @Override
    protected void finishProcessing() {
        //TODO: DO NOT PASS NULL HERE
        Pair<GasStack, GasStack> output = recipe.getOutput(null);

        FluidTank fluidTank = getTank();
        int possibleProcess = (int) Math.pow(2, getSpeedUpgrades());
        if (leftTank.getGasType() == recipe.recipeOutput.leftGas.getGas()) {
            possibleProcess = Math.min(leftTank.getNeeded() / output.getLeft().amount, possibleProcess);
            possibleProcess = Math.min(rightTank.getNeeded() / output.getRight().amount, possibleProcess);
        } else {
            possibleProcess = Math.min(leftTank.getNeeded() / output.getRight().amount, possibleProcess);
            possibleProcess = Math.min(rightTank.getNeeded() / output.getLeft().amount, possibleProcess);
        }
        possibleProcess = Math.min((int) (getStoredElectricity() / getEnergyPerTick()), possibleProcess);
        possibleProcess = Math.min(fluidTank.getFluidAmount() / recipe.recipeInput.ingredient.amount, possibleProcess);

        //TODO: Handle processing stuff
        addToOutput.apply(recipe.getOutput(getTank().getFluid()), false);
    }
}