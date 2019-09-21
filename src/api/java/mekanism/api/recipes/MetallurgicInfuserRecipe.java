package mekanism.api.recipes;

import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.infuse.InfusionContainer;
import mekanism.api.recipes.inputs.InfusionIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.outputs.OreDictSupplier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.Tag;

/**
 * Created by Thiakil on 14/07/2019.
 */
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MetallurgicInfuserRecipe implements IMekanismRecipe, BiPredicate<InfusionContainer, ItemStack> {

    private final ItemStackIngredient itemInput;
    private final InfusionIngredient infusionInput;
    private final ItemStack outputDefinition;

    public MetallurgicInfuserRecipe(ItemStackIngredient itemInput, InfusionIngredient infusionInput, ItemStack outputDefinition) {
        this.itemInput = itemInput;
        this.infusionInput = infusionInput;
        this.outputDefinition = outputDefinition.copy();
    }

    @Override
    public boolean test(InfusionContainer infusionContainer, ItemStack itemStack) {
        return infusionInput.test(infusionContainer) && itemInput.test(itemStack);
    }

    public @NonNull List<@NonNull ItemStack> getOutputDefinition() {
        return Collections.singletonList(this.outputDefinition);
    }

    public ItemStack getOutput(InfusionContainer inputInfuse, ItemStack inputItem) {
        return this.outputDefinition.copy();
    }

    public InfusionIngredient getInfusionInput() {
        return this.infusionInput;
    }

    public ItemStackIngredient getItemInput() {
        return this.itemInput;
    }

    public static class MetallurgicInfuserRecipeOre extends MetallurgicInfuserRecipe {

        private final OreDictSupplier oreOutput;

        public MetallurgicInfuserRecipeOre(ItemStackIngredient itemInput, InfusionIngredient infusionInput, Tag<Item> outputTag) {
            super(itemInput, infusionInput, ItemStack.EMPTY);
            this.oreOutput = new OreDictSupplier(outputTag);
        }

        @Override
        public @NonNull List<@NonNull ItemStack> getOutputDefinition() {
            return oreOutput.getPossibleOutputs();
        }

        @Override
        public ItemStack getOutput(InfusionContainer inputInfuse, ItemStack inputItem) {
            return oreOutput.get();
        }
    }
}
