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
import mekanism.api.function.BooleanConsumer;
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
    private final IntSupplier speedUpgrades;

    public FluidGasToGasCachedRecipe(FluidGasToGasRecipe recipe, BooleanSupplier canTileFunction, DoubleSupplier perTickEnergy, DoubleSupplier storedEnergy,
          BooleanConsumer setActive, DoubleConsumer useEnergy, Runnable onFinish, Supplier<@NonNull FluidTank> fluidTank, Supplier<@NonNull GasTank> gasTank,
          IntSupplier speedUpgrades, BiFunction<@NonNull GasStack, Boolean, Boolean> addToOutput) {
        this(recipe, canTileFunction, perTickEnergy, storedEnergy, () -> 1, setActive, useEnergy, onFinish, fluidTank, gasTank, speedUpgrades, addToOutput);
    }

    public FluidGasToGasCachedRecipe(FluidGasToGasRecipe recipe, BooleanSupplier canTileFunction, DoubleSupplier perTickEnergy, DoubleSupplier storedEnergy,
          IntSupplier requiredTicks, BooleanConsumer setActive, DoubleConsumer useEnergy, Runnable onFinish, Supplier<@NonNull FluidTank> fluidTank,
          Supplier<@NonNull GasTank> gasTank, IntSupplier speedUpgrades, BiFunction<@NonNull GasStack, Boolean, Boolean> addToOutput) {
        super(recipe, canTileFunction, perTickEnergy, storedEnergy, requiredTicks, setActive, useEnergy, onFinish);
        this.fluidTank = fluidTank;
        this.gasTank = gasTank;
        this.addToOutput = addToOutput;
        this.speedUpgrades = speedUpgrades;
    }

    @Nonnull
    private GasTank getGasTank() {
        return gasTank.get();
    }

    @Nonnull
    private FluidTank getFluidTank() {
        return fluidTank.get();
    }

    private int getSpeedUpgrades() {
        return speedUpgrades.getAsInt();
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        //TODO: Move hasResourcesForTick and hasRoomForOutput into this calculation
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
        GasTank gasTank = getGasTank();
        FluidTank fluidTank = getFluidTank();
        int possibleProcess = (int) Math.pow(2, getSpeedUpgrades());
        possibleProcess = Math.min(Math.min(gasTank.getStored(), outputTank.getNeeded()), possibleProcess);
        possibleProcess = Math.min((int) (getStoredElectricity() / getEnergyPerTick()), possibleProcess);
        //TODO: Instead of water, use the recipe's cleansing fluid amount

        //TODO: Make a way to get the "size" of the current matching ingredient
        possibleProcess = Math.min(fluidTank.getFluidAmount() / recipeFluid.amount, possibleProcess);

        //TODO: Should the possibleProcess stuff be offloaded upwards to a method that defaults to returning 1
        // And should we finally make the stuff passed to getOutput do "something" and return based on how many outputs it would make
        addToOutput.apply(recipe.getOutput(getFluidTank().getFluid(), getGasTank().getGas()), false);
    }
}