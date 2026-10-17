package mekanism.api.recipes;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

/// Base class for defining item chemical to item recipes.
///
/// Input: ItemStack
///
/// Input: Chemical
///
/// Output: OUTPUT
///
/// @since 10.7.0
public abstract class ItemStackChemicalToObjectRecipe<OUTPUT, BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>>
      extends TwoInputMekRecipe<Item, ItemStack, ItemStackIngredient, Chemical, ChemicalStack, ChemicalStackIngredient, SingleItemChemicalRecipeInput, OUTPUT, BOOK_CATEGORY, BOOK_INFO> {

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    ///
    /// @since 10.8.0
    protected ItemStackChemicalToObjectRecipe(Recipe.CommonInfo commonInfo, BOOK_INFO bookInfo) {
        super(commonInfo, bookInfo);
    }

    /// Represents whether this recipe consumes the chemical each tick.
    ///
    /// @since 10.7.0
    public abstract boolean perTickUsage();

    /// Gets the input item ingredient.
    public abstract ItemStackIngredient getItemInput();

    @Override
    public final ItemStackIngredient getInputA() {
        return getItemInput();
    }

    /// Gets the input chemical ingredient.
    public abstract ChemicalStackIngredient getChemicalInput();

    @Override
    public final ChemicalStackIngredient getInputB() {
        return getChemicalInput();
    }

    @Override
    public boolean matches(SingleItemChemicalRecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && test(input.item(), input.chemical());
    }
}