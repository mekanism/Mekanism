package mekanism.api.recipes.cache;

import java.util.function.BiFunction;
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

    public ElectrolysisCachedRecipe(ElectrolysisRecipe recipe, Supplier<@NonNull FluidTank> inputTank,
          BiFunction<@NonNull Pair<GasStack, GasStack>, Boolean, Boolean> addToOutput) {
        super(recipe);
        this.inputTank = inputTank;
        this.addToOutput = addToOutput;
    }

    private FluidTank getTank() {
        return inputTank.get();
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        //TODO: Move hasResourcesForTick and hasRoomForOutput into this calculation
        //TODO: Make sure to check both tanks in case the left is in the right and the right is in the left
        // Actually is that even needed given those are OUTPUT tanks so it *should* really match what our recipe believes
        return 1;
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
    protected void finishProcessing(int operations) {
        //TODO: Handle processing stuff
        addToOutput.apply(recipe.getOutput(getTank().getFluid()), false);
    }
}