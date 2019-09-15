package mekanism.api.recipes.cache;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.apache.commons.lang3.tuple.Pair;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class PressurizedReactionCachedRecipe extends CachedRecipe<PressurizedReactionRecipe> {

    private final BiFunction<@NonNull Pair<@NonNull ItemStack, @NonNull GasStack>, Boolean, Boolean> addToOutput;
    private final Supplier<@NonNull ItemStack> inputStack;
    private final Supplier<@NonNull FluidTank> fluidInputTank;
    private final Supplier<@NonNull GasTank> gasInputTank;

    public PressurizedReactionCachedRecipe(PressurizedReactionRecipe recipe, Supplier<@NonNull ItemStack> inputStack, Supplier<@NonNull FluidTank> fluidInputTank,
          Supplier<@NonNull GasTank> gasInputTank, BiFunction<@NonNull Pair<@NonNull ItemStack, @NonNull GasStack>, Boolean, Boolean> addToOutput) {
        super(recipe);
        this.inputStack = inputStack;
        this.fluidInputTank = fluidInputTank;
        this.gasInputTank = gasInputTank;
        this.addToOutput = addToOutput;
    }

    private ItemStack getItemInput() {
        return inputStack.get();
    }

    private FluidTank getFluidTank() {
        return fluidInputTank.get();
    }

    private GasTank getGasTank() {
        return gasInputTank.get();
    }

    @Override
    protected int getOperationsThisTick(int currentMax) {
        //TODO: Move hasResourcesForTick and hasRoomForOutput into this calculation
        return 1;
    }

    @Override
    public boolean hasResourcesForTick() {
        GasStack gas = getGasTank().getGas();
        if (gas == null || gas.amount == 0) {
            return false;
        }
        FluidStack fluid = getFluidTank().getFluid();
        if (fluid == null || fluid.amount == 0) {
            return false;
        }
        return recipe.test(getItemInput(), fluid, gas);
    }

    @Override
    public boolean hasRoomForOutput() {
        return addToOutput.apply(recipe.getOutput(getItemInput(), getFluidTank().getFluid(), getGasTank().getGas()), true);
    }

    @Override
    protected void finishProcessing(int operations) {
        addToOutput.apply(recipe.getOutput(getItemInput(), getFluidTank().getFluid(), getGasTank().getGas()), false);
    }
}