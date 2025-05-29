package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public abstract class BasicItemStackChemicalToItemStackRecipe extends ItemStackChemicalToItemStackRecipe implements IBasicItemStackOutput {

    private final RecipeType<? extends ItemStackChemicalToItemStackRecipe> recipeType;
    protected final ItemStackIngredient itemInput;
    protected final ChemicalStackIngredient chemicalInput;
    protected final ItemStack output;
    private final boolean perTickUsage;

    /**
     * @param itemInput     Item input.
     * @param chemicalInput Chemical input.
     * @param output        Output.
     * @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
     */
    public BasicItemStackChemicalToItemStackRecipe(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStack output, boolean perTickUsage,
          RecipeType<? extends ItemStackChemicalToItemStackRecipe> recipeType) {
        this.recipeType = Objects.requireNonNull(recipeType, "Recipe type cannot be null");
        this.itemInput = Objects.requireNonNull(itemInput, "Item input cannot be null.");
        this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        if (output.isEmpty()) {
            throw new IllegalArgumentException("Output cannot be empty.");
        }
        this.output = output.copy();
        this.perTickUsage = perTickUsage;
    }

    @Override
    public final RecipeType<? extends ItemStackChemicalToItemStackRecipe> getType() {
        return recipeType;
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

    @Override
    public final boolean perTickUsage() {
        return perTickUsage;
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
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicItemStackChemicalToItemStackRecipe other = (BasicItemStackChemicalToItemStackRecipe) o;
        //Note: We don't need to compare the recipe type as that gets covered by the explicit class type check above
        return perTickUsage == other.perTickUsage && itemInput.equals(other.itemInput) && chemicalInput.equals(other.chemicalInput) && ItemStack.matches(output, other.output);
    }

    @Override
    public int hashCode() {
        int result = itemInput.hashCode();
        result = 31 * result + chemicalInput.hashCode();
        result = 31 * result + Boolean.hashCode(perTickUsage);
        result = 31 * result + ItemStack.hashItemAndComponents(output);
        result = 31 * result + output.getCount();
        return result;
    }
}