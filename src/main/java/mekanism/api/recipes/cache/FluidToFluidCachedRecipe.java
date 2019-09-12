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
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class FluidToFluidCachedRecipe extends CachedRecipe<FluidToFluidRecipe> {

    private final BiFunction<@NonNull FluidStack, Boolean, Boolean> addToOutput;
    private final Supplier<@NonNull FluidTank> inputTank;

    public FluidToFluidCachedRecipe(FluidToFluidRecipe recipe, BooleanSupplier canTileFunction, DoubleSupplier perTickEnergy, DoubleSupplier storedEnergy,
          IntSupplier requiredTicks, Consumer<Boolean> setActive, DoubleConsumer useEnergy, Runnable onFinish, Supplier<@NonNull FluidTank> inputTank,
          BiFunction<@NonNull FluidStack, Boolean, Boolean> addToOutput) {
        super(recipe, canTileFunction, perTickEnergy, storedEnergy, requiredTicks, setActive, useEnergy, onFinish);
        this.inputTank = inputTank;
        this.addToOutput = addToOutput;
    }

    private FluidTank getInputTank() {
        return inputTank.get();
    }

    @Override
    public boolean hasResourcesForTick() {
        FluidStack fluid = getInputTank().getFluid();
        return fluid != null && recipe.test(fluid);
    }

    @Override
    public boolean hasRoomForOutput() {
        return addToOutput.apply(recipe.getOutput(getInputTank().getFluid()), true);
    }

    @Override
    protected void useResources() {
        super.useResources();
        //TODO: Use any secondary resources or remove this override
    }

    @Override
    protected void finishProcessing() {
        addToOutput.apply(recipe.getOutput(getInputTank().getFluid()), false);
    }
}