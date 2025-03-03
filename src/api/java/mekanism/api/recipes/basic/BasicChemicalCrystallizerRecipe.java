package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


@NothingNullByDefault
public class BasicChemicalCrystallizerRecipe extends ChemicalCrystallizerRecipe {

    protected final ChemicalStackIngredient input;
    protected final ItemStack output;

    /**
     * @param input  Input.
     * @param output Output.
     */
    public BasicChemicalCrystallizerRecipe(ChemicalStackIngredient input, ItemStack output) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
    }

    @Contract(value = "_ -> new", pure = true)
    @Override
    public ItemStack getOutput(ChemicalStack input) {
        return output.copy();
    }

    @NotNull
    @Override
    public ItemStack getResultItem(@NotNull HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public List<ItemStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public boolean test(ChemicalStack stack) {
        return input.test(stack);
    }

    @Override
    public boolean testType(ChemicalStack stack) {
        return input.testType(stack);
    }

    @Override
    public ChemicalStackIngredient getInput() {
        return input;
    }

    /**
     * For Serializer usage only. Do not modify the returned stack!
     *
     * @return the uncopied output definition
     */
    public ItemStack getOutputRaw() {
        return this.output;
    }

    @Override
    public RecipeSerializer<BasicChemicalCrystallizerRecipe> getSerializer() {
        return MekanismRecipeSerializers.CRYSTALLIZING.get();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicChemicalCrystallizerRecipe other = (BasicChemicalCrystallizerRecipe) o;
        return input.equals(other.input) && ItemStack.matches(output, other.output);
    }

    @Override
    public int hashCode() {
        int hash = input.hashCode();
        hash = 31 * hash + ItemStack.hashItemAndComponents(output);
        hash = 31 * hash + output.getCount();
        return hash;
    }
}