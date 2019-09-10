package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.GasIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.outputs.OreDictSupplier;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;

/**
 * Inputs: ItemStack + GasStack Output: ItemStack
 *
 * Ex-AdvancedMachineInput based; InjectionRecipe, OsmiumCompressorRecipe, PurificationRecipe
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemStackGasToItemStackRecipe implements IMekanismRecipe, BiPredicate<@NonNull ItemStack, @NonNull Gas> {

    private final ItemStackIngredient itemInput;

    private final GasIngredient gasInput;

    private final ItemStack outputDefinition;

    public ItemStackGasToItemStackRecipe(ItemStackIngredient itemInput, GasIngredient gasInput, ItemStack outputDefinition) {
        this.itemInput = itemInput;
        this.gasInput = gasInput;
        this.outputDefinition = outputDefinition;
    }

    public ItemStackIngredient getItemInput() {
        return itemInput;
    }

    public GasIngredient getGasInput() {
        return gasInput;
    }

    public ItemStack getOutput(ItemStack inputItem, GasStack inputGas) {
        return outputDefinition.copy();
    }

    @Override
    public boolean test(@NonNull ItemStack itemStack, @NonNull Gas gas) {
        return itemInput.test(itemStack) && gasInput.test(gas);
    }

    public @NonNull List<@NonNull ItemStack> getOutputDefinition() {
        return Collections.singletonList(outputDefinition);
    }

    public static class ItemStackGasToItemStackRecipeOre extends ItemStackGasToItemStackRecipe {

        private final OreDictSupplier outputSupplier;

        public ItemStackGasToItemStackRecipeOre(ItemStackIngredient itemInput, GasIngredient gasInput, String outputOreName) {
            super(itemInput, gasInput, ItemStack.EMPTY);
            this.outputSupplier = new OreDictSupplier(outputOreName);
        }

        @Override
        public ItemStack getOutput(ItemStack inputItem, GasStack inputGas) {
            return this.outputSupplier.get();
        }

        @Override
        public @NonNull List<@NonNull ItemStack> getOutputDefinition() {
            return this.outputSupplier.getPossibleOutputs();
        }
    }
}
