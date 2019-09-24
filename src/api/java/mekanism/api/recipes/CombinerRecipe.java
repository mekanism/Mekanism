package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.outputs.OreDictSupplier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.Tag;

/**
 * Inputs: ItemStack (main item) + ItemStack (material to combine with) Output: ItemStack (combined)
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public class CombinerRecipe implements IMekanismRecipe, BiPredicate<@NonNull ItemStack, @NonNull ItemStack> {

    private final ItemStackIngredient mainInput;
    private final ItemStackIngredient extraInput;
    private ItemStack outputDefinition;

    public CombinerRecipe(ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack outputDefinition) {
        this.mainInput = mainInput;
        this.extraInput = extraInput;
        this.outputDefinition = outputDefinition.copy();
    }

    @Override
    public boolean test(@NonNull ItemStack input, @NonNull ItemStack extra) {
        return mainInput.test(input) && extraInput.test(extra);
    }

    public ItemStackIngredient getMainInput() {
        return mainInput;
    }

    public ItemStackIngredient getExtraInput() {
        return extraInput;
    }

    public ItemStack getOutput(@NonNull ItemStack input, @NonNull ItemStack extra) {
        return outputDefinition.copy();
    }

    /**
     * For JEI, gets a display stack
     *
     * @return Representation of output, MUST NOT be modified
     */
    public List<ItemStack> getOutputDefinition() {
        return outputDefinition.isEmpty() ? Collections.emptyList() : Collections.singletonList(outputDefinition);
    }

    public static class CombinerRecipeOre extends CombinerRecipe {

        private final OreDictSupplier outputSupplier;

        public CombinerRecipeOre(ItemStackIngredient mainInput, ItemStackIngredient extraInput, Tag<Item> outputTag) {
            super(mainInput, extraInput, ItemStack.EMPTY);
            this.outputSupplier = new OreDictSupplier(outputTag);
        }

        @Override
        public ItemStack getOutput(@NonNull ItemStack input, @NonNull ItemStack extra) {
            return this.outputSupplier.get();
        }

        @Override
        public List<ItemStack> getOutputDefinition() {
            return this.outputSupplier.getPossibleOutputs();
        }
    }
}
