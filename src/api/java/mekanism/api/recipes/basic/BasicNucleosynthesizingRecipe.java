package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class BasicNucleosynthesizingRecipe extends NucleosynthesizingRecipe implements IBasicItemStackOutput {

    protected final ItemStackIngredient itemInput;
    protected final ChemicalStackIngredient chemicalInput;
    protected final ItemStack output;
    private final int duration;
    private final boolean perTickUsage;

    /**
     * @param itemInput     Item input.
     * @param chemicalInput Chemical input.
     * @param output        Output.
     * @param duration      Duration in ticks that it takes the recipe to complete. Must be greater than zero.
     * @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
     */
    public BasicNucleosynthesizingRecipe(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStack output, int duration, boolean perTickUsage) {
        this.itemInput = Objects.requireNonNull(itemInput, "Item input cannot be null.");
        this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be a number greater than zero.");
        }
        this.duration = duration;
        this.perTickUsage = perTickUsage;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public boolean perTickUsage() {
        return perTickUsage;
    }

    @Override
    public ItemStackIngredient getItemInput() {
        return itemInput;
    }

    @Override
    public ChemicalStackIngredient getChemicalInput() {
        return chemicalInput;
    }

    @Override
    @Contract(value = "_, _ -> new", pure = true)
    public ItemStack getOutput(ItemStack inputItem, ChemicalStack inputChemical) {
        return output.copy();
    }

    @NotNull
    @Override
    public ItemStack getResultItem(@NotNull HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public boolean test(ItemStack itemStack, ChemicalStack chemicalStack) {
        return itemInput.test(itemStack) && chemicalInput.test(chemicalStack);
    }

    @Override
    public List<@NotNull ItemStack> getOutputDefinition() {
        return Collections.singletonList(output);
    }

    @Override
    public ItemStack getOutputRaw() {
        return output;
    }

    @Override
    public RecipeSerializer<BasicNucleosynthesizingRecipe> getSerializer() {
        return MekanismRecipeSerializers.NUCLEOSYNTHESIZING.get();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicNucleosynthesizingRecipe other = (BasicNucleosynthesizingRecipe) o;
        return duration == other.duration && perTickUsage == other.perTickUsage && itemInput.equals(other.itemInput) && chemicalInput.equals(other.chemicalInput) &&
               ItemStack.matches(output, other.output);
    }

    @Override
    public int hashCode() {
        int result = itemInput.hashCode();
        result = 31 * result + chemicalInput.hashCode();
        result = 31 * result + duration;
        result = 31 * result + Boolean.hashCode(perTickUsage);
        result = 31 * result + ItemStack.hashItemAndComponents(output);
        result = 31 * result + output.getCount();
        return result;
    }
}