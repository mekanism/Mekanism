package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.ingredients.GasStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

@NothingNullByDefault
public class BasicChemicalDissolutionRecipe extends ChemicalDissolutionRecipe {

    protected final ItemStackIngredient itemInput;
    protected final GasStackIngredient gasInput;
    protected final BoxedChemicalStack output;

    /**
     * @param itemInput Item input.
     * @param gasInput  Gas input.
     * @param output    Output.
     */
    public BasicChemicalDissolutionRecipe(ItemStackIngredient itemInput, GasStackIngredient gasInput, ChemicalStack<?> output) {
        this.itemInput = Objects.requireNonNull(itemInput, "Item input cannot be null.");
        this.gasInput = Objects.requireNonNull(gasInput, "Gas input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = BoxedChemicalStack.box(output.copy());
    }

    @Override
    public ItemStackIngredient getItemInput() {
        return itemInput;
    }

    @Override
    public GasStackIngredient getGasInput() {
        return gasInput;
    }

    @Override
    public BoxedChemicalStack getOutput(ItemStack inputItem, GasStack inputGas) {
        return output.copy();
    }

    @Override
    public boolean test(ItemStack itemStack, GasStack gasStack) {
        return itemInput.test(itemStack) && gasInput.test(gasStack);
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
    public RecipeSerializer<BasicChemicalDissolutionRecipe> getSerializer() {
        return MekanismRecipeSerializers.DISSOLUTION.get();
    }
}