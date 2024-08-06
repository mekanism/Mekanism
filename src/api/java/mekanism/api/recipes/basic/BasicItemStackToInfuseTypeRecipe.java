package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public class BasicItemStackToInfuseTypeRecipe extends ItemStackToInfuseTypeRecipe implements IBasicChemicalOutput {

    protected final ItemStackIngredient input;
    protected final ChemicalStack output;

    /**
     * @param input  Input.
     * @param output Output.
     */
    public BasicItemStackToInfuseTypeRecipe(ItemStackIngredient input, ChemicalStack output) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return input.test(itemStack);
    }

    @Override
    public ItemStackIngredient getInput() {
        return input;
    }

    @Override
    @Contract(value = "_ -> new", pure = true)
    public ChemicalStack getOutput(ItemStack input) {
        return output.copy();
    }

    @Override
    public List<ChemicalStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public ChemicalStack getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<BasicItemStackToInfuseTypeRecipe> getSerializer() {
        return MekanismRecipeSerializers.INFUSION_CONVERSION.get();
    }
}