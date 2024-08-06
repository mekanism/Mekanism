package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class BasicChemicalDissolutionRecipe extends ChemicalDissolutionRecipe {

    protected final ItemStackIngredient itemInput;
    protected final ChemicalStackIngredient gasInput;
    protected final ChemicalStack output;

    /**
     * @param itemInput Item input.
     * @param gasInput  Gas input.
     * @param output    Output.
     */
    public BasicChemicalDissolutionRecipe(ItemStackIngredient itemInput, ChemicalStackIngredient gasInput, ChemicalStack output) {
        this.itemInput = Objects.requireNonNull(itemInput, "Item input cannot be null.");
        this.gasInput = Objects.requireNonNull(gasInput, "Gas input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Override
    public ItemStackIngredient getItemInput() {
        return itemInput;
    }

    @Override
    public ChemicalStackIngredient getGasInput() {
        return gasInput;
    }

    @Override
    public ChemicalStack getOutput(ItemStack inputItem, ChemicalStack inputGas) {
        return output.copy();
    }

    @Override
    public boolean test(ItemStack itemStack, ChemicalStack gasStack) {
        return itemInput.test(itemStack) && gasInput.test(gasStack);
    }

    @Override
    public List<ChemicalStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    /**
     * For Serializer usage only. Do not modify the returned stack!
     *
     * @return the uncopied output definition
     */
    public ChemicalStack getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<BasicChemicalDissolutionRecipe> getSerializer() {
        return MekanismRecipeSerializers.DISSOLUTION.get();
    }
}