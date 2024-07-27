package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalOxidizerRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class BasicChemicalOxidizerRecipe extends ChemicalOxidizerRecipe {

    protected final ItemStackIngredient input;
    protected final BoxedChemicalStack output;

    /**
     * @param input     Input.
     * @param output    Output.
     */
    public BasicChemicalOxidizerRecipe(ItemStackIngredient input, ChemicalStack<?> output) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = BoxedChemicalStack.box(output.copy());
    }

    @Override
    public ItemStackIngredient getInput() {
        return input;
    }

    @Override
    public BoxedChemicalStack getOutput(ItemStack input) {
        return output.copy();
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return input.test(itemStack);
    }

    @Override
    public List<BoxedChemicalStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    /**
     * For Serializer usage only. Do not modify the returned stack!
     *
     * @return the uncopied output definition
     */
    public BoxedChemicalStack getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<BasicChemicalOxidizerRecipe> getSerializer() {
        return MekanismRecipeSerializers.OXIDIZING.get();
    }
}