package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.core.TypedInstance;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;


public class BasicChemicalCrystallizerRecipe extends ChemicalCrystallizerRecipe {

    protected final ChemicalStackIngredient input;
    protected final ItemStackTemplate output;

    /// @param input  Input.
    /// @param output Output.
    public BasicChemicalCrystallizerRecipe(ChemicalStackIngredient input, ItemStackTemplate output) {
        this.input = Objects.requireNonNull(input, "Input cannot be null.");
        this.output = Objects.requireNonNull(output, "Output cannot be null.");
    }

    @Override
    @Contract(pure = true)
    public ItemStackTemplate getOutput(TypedInstance<Chemical> input) {
        return output;
    }

    @Override
    public List<ItemStackTemplate> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public ChemicalStackIngredient getInput() {
        return input;
    }

    /// For Serializer usage only. Do not modify the returned stack!
    ///
    /// @return the uncopied output definition
    public ItemStackTemplate getOutputRaw() {
        return this.output;
    }

    @Override
    public RecipeSerializer<BasicChemicalCrystallizerRecipe> getSerializer() {
        return MekanismRecipeSerializers.CRYSTALLIZING.get();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicChemicalCrystallizerRecipe other = (BasicChemicalCrystallizerRecipe) o;
        return input.equals(other.input) && output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int hash = input.hashCode();
        hash = 31 * hash + output.hashCode();
        return hash;
    }
}