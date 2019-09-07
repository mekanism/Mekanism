package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.outputs.OreDictSupplier;
import mekanism.common.util.FieldsAreNonnullByDefault;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

/**
 * Inputs: ItemStack (main item) + ItemStack (material to combine with)
 * Output: ItemStack (combined)
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public class CombinerRecipe implements IMekanismRecipe, BiPredicate<@NonNull ItemStack, @NonNull ItemStack> {

    private final Ingredient mainInput;

    private final Ingredient extraInput;

    private ItemStack outputDefinition;

    public CombinerRecipe(Ingredient mainInput, Ingredient extraInput, ItemStack outputDefinition) {
        this.mainInput = mainInput;
        this.extraInput = extraInput;
        this.outputDefinition = outputDefinition.copy();
    }

    @Override
    public boolean test(@NonNull ItemStack input, @NonNull ItemStack extra) {
        return mainInput.apply(input) && extraInput.apply(extra);
    }

    public Ingredient getMainInput() {
        return mainInput;
    }

    public Ingredient getExtraInput() {
        return extraInput;
    }

    public ItemStack getOutput(@NonNull ItemStack input, @NonNull ItemStack extra) {
        return outputDefinition.copy();
    }

    /**
     * For JEI, gets a display stack
     * @return Representation of output, MUST NOT be modified
     */
    public List<ItemStack> getOutputDefinition() {
        return Collections.singletonList(outputDefinition);
    }

    public static class CombinerRecipeOre extends CombinerRecipe {

        private final OreDictSupplier outputSupplier;

        public CombinerRecipeOre(Ingredient mainInput, Ingredient extraInput, String outputOreName) {
            super(mainInput, extraInput, ItemStack.EMPTY);
            this.outputSupplier = new OreDictSupplier(outputOreName);
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
