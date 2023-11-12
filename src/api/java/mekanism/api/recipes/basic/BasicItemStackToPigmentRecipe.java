package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Contract;

@NothingNullByDefault
public class BasicItemStackToPigmentRecipe extends ItemStackToPigmentRecipe implements IBasicChemicalOutput<Pigment, PigmentStack> {

    protected final ItemStackIngredient input;
    protected final PigmentStack output;

    /**
     * @param input  Input.
     * @param output Output.
     */
    public BasicItemStackToPigmentRecipe(ItemStackIngredient input, PigmentStack output) {
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
    public PigmentStack getOutput(ItemStack input) {
        return output.copy();
    }

    @Override
    public List<PigmentStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public PigmentStack getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<BasicItemStackToPigmentRecipe> getSerializer() {
        return MekanismRecipeSerializers.PIGMENT_EXTRACTING.get();
    }
}