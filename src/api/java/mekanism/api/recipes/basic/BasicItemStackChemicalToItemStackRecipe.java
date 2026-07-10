package mekanism.api.recipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.TypedInstance;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public abstract class BasicItemStackChemicalToItemStackRecipe extends ItemStackChemicalToItemStackRecipe implements IBasicItemStackOutput {

    private final RecipeType<? extends ItemStackChemicalToItemStackRecipe> recipeType;
    protected final ItemStackIngredient itemInput;
    protected final ChemicalStackIngredient chemicalInput;
    protected final ItemStackTemplate output;
    private final boolean perTickUsage;

    /// @param itemInput     Item input.
    /// @param chemicalInput Chemical input.
    /// @param output        Output.
    /// @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
    public BasicItemStackChemicalToItemStackRecipe(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStackTemplate output, boolean perTickUsage,
          RecipeType<? extends ItemStackChemicalToItemStackRecipe> recipeType) {
        this.recipeType = Objects.requireNonNull(recipeType, "Recipe type cannot be null");
        this.itemInput = Objects.requireNonNull(itemInput, "Item input cannot be null.");
        this.chemicalInput = Objects.requireNonNull(chemicalInput, "Chemical input cannot be null.");
        Objects.requireNonNull(output, "Output cannot be null.");
        this.output = output;
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
    @Contract(pure = true)
    public ItemStackTemplate getOutput(TypedInstance<Item> inputItem, TypedInstance<Chemical> inputChemical) {
        return output;
    }

    @Override
    public final boolean perTickUsage() {
        return perTickUsage;
    }

    @Override
    public List<ItemStackTemplate> getOutputDefinition(ContextMap contextMap) {
        return Collections.singletonList(output);
    }

    @Override
    public SlotDisplay getOutputDisplay() {
        return new SlotDisplay.ItemStackSlotDisplay(output);
    }

    @Override
    public ItemStackTemplate getOutputRaw() {
        return output;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasicItemStackChemicalToItemStackRecipe other = (BasicItemStackChemicalToItemStackRecipe) o;
        //Note: We don't need to compare the recipe type as that gets covered by the explicit class type check above
        return perTickUsage == other.perTickUsage && itemInput.equals(other.itemInput) && chemicalInput.equals(other.chemicalInput) && output.equals(other.output);
    }

    @Override
    public int hashCode() {
        int result = itemInput.hashCode();
        result = 31 * result + chemicalInput.hashCode();
        result = 31 * result + Boolean.hashCode(perTickUsage);
        result = 31 * result + output.hashCode();
        return result;
    }
}