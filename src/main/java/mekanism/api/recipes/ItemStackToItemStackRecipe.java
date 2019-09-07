package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.outputs.OreDictSupplier;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

/**
 * Inputs: ItemStack (item)
 * Output: ItemStack (transformed)
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public class ItemStackToItemStackRecipe implements IMekanismRecipe, Predicate<@NonNull ItemStack> {

    private final Ingredient mainInput;

    private ItemStack outputDefinition;

    public ItemStackToItemStackRecipe(Ingredient mainInput, ItemStack outputDefinition) {
        this.mainInput = mainInput;
        this.outputDefinition = outputDefinition.copy();
    }

    @Override
    public boolean test(@NonNull ItemStack input) {
        return mainInput.apply(input);
    }

    public Ingredient getInput() {
        return mainInput;
    }

    public ItemStack getOutput(@NonNull ItemStack input) {
        return outputDefinition.copy();
    }

    /**
     * For JEI, gets a display stack
     * @return Representation of output, MUST NOT be modified
     */
    public List<ItemStack> getOutputDefinition() {
        return Collections.singletonList(outputDefinition);
    }

    public static class ItemStackToItemStackRecipeOre extends ItemStackToItemStackRecipe {

        private final OreDictSupplier outputSupplier;

        public ItemStackToItemStackRecipeOre(Ingredient mainInput, String outputOreName) {
            super(mainInput, ItemStack.EMPTY);
            this.outputSupplier = new OreDictSupplier(outputOreName);
        }

        @Override
        public ItemStack getOutput(@NonNull ItemStack input) {
            return this.outputSupplier.get();
        }

        @Override
        public List<ItemStack> getOutputDefinition() {
            return this.outputSupplier.getPossibleOutputs();
        }
    }
}
