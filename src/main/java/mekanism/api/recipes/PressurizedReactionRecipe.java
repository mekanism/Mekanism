package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.outputs.OreDictSupplier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;


public class PressurizedReactionRecipe {

    private final Ingredient inputSolid;
    private final FluidStackIngredient inputFluid;
    private final GasStackIngredient gasInput;
    protected final Gas outputGas;
    protected final int outputGasAmount;
    private final double energyRequired;
    private final int duration;
    private final ItemStack outputDefinition;
    protected GasStack gasOutputDefinition;

    public PressurizedReactionRecipe(Ingredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient gasInput, Gas outputGas, int outputGasAmount, double energyRequired, int duration, ItemStack outputDefinition) {
        this.inputSolid = inputSolid;
        this.inputFluid = inputFluid;
        this.gasInput = gasInput;
        this.outputGas = outputGas;
        this.outputGasAmount = outputGasAmount;
        this.energyRequired = energyRequired;
        this.duration = duration;
        this.outputDefinition = outputDefinition;
        this.gasOutputDefinition = new GasStack(this.outputGas, this.outputGasAmount);
    }

    public Ingredient getInputSolid() {
        return inputSolid;
    }

    public FluidStackIngredient getInputFluid() {
        return inputFluid;
    }

    public GasStackIngredient getGasInput() {
        return gasInput;
    }

    public double getEnergyRequired() {
        return energyRequired;
    }

    public int getDuration() {
        return duration;
    }

    public boolean test(ItemStack solid, FluidStack liquid, GasStack gas) {
        return this.inputSolid.apply(solid) && this.inputFluid.test(liquid) && this.gasInput.test(gas);
    }

    public @NonNull Pair<List<@NonNull ItemStack>, @NonNull GasStack> getOutputDefinition() {
        return Pair.of(Collections.singletonList(this.outputDefinition), this.gasOutputDefinition);
    }

    public @NonNull Pair<@NonNull ItemStack, @NonNull GasStack> getOutput(ItemStack solid, FluidStack liquid, GasStack gas) {
        return Pair.of(this.outputDefinition.copy(), this.gasOutputDefinition.copy());
    }

    public static class PressurizedReactionRecipeOre extends PressurizedReactionRecipe {

        private final OreDictSupplier outputSupplier;

        public PressurizedReactionRecipeOre(Ingredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient gasInput, Gas outputGas, int outputGasAmount, double energyRequired, int duration, String outputOreName) {
            super(inputSolid, inputFluid, gasInput, outputGas, outputGasAmount, energyRequired, duration, ItemStack.EMPTY);
            this.outputSupplier = new OreDictSupplier(outputOreName);
        }

        @Override
        public @NonNull Pair<List<@NonNull ItemStack>, @NonNull GasStack> getOutputDefinition() {
            return Pair.of(this.outputSupplier.getPossibleOutputs(), gasOutputDefinition);
        }

        @Override
        public @NonNull Pair<@NonNull ItemStack, @NonNull GasStack> getOutput(ItemStack solid, FluidStack liquid, GasStack gas) {
            return Pair.of(this.outputSupplier.get(), this.gasOutputDefinition.copy());
        }
    }
}
