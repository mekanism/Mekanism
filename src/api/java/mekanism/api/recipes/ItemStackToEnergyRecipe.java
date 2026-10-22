package mekanism.api.recipes;

import it.unimi.dsi.fastutil.ints.IntList;
import mekanism.api.recipes.SingleInputRecipe.ItemInputRecipe;
import mekanism.api.recipes.book.EnergyConversionBookCategory;
import mekanism.api.recipes.book.EnergyConversionBookCategory.EnergyConversionBookInfo;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.TypedInstance;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Range;

/// Input: ItemStack
///
/// Output: int
///
/// @apiNote Energy conversion recipes can be used in any slots in Mekanism machines that are able to convert items into energy.
public abstract class ItemStackToEnergyRecipe extends ItemInputRecipe<Integer, EnergyConversionBookCategory, EnergyConversionBookInfo> {

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    ///
    /// @since 10.8.0
    protected ItemStackToEnergyRecipe(Recipe.CommonInfo commonInfo, EnergyConversionBookInfo bookInfo) {
        super(commonInfo, bookInfo);
    }

    /// Gets the output based on the given input.
    ///
    /// @param input Specific input.
    ///
    /// @return Output as a constant.
    ///
    /// @apiNote While Mekanism does not currently make use of the input, it is important to support it and pass the proper value in case any addons define input based
    /// outputs where things like NBT may be different.
    /// @implNote The passed in input should **NOT** be modified.
    @Override
    @Range(from = 1, to = Integer.MAX_VALUE)
    public abstract Integer getOutput(TypedInstance<Item> input);

    @Override
    public abstract IntList getOutputDefinition(ContextMap contextMap);

    @Override
    public final RecipeType<ItemStackToEnergyRecipe> getType() {
        return MekanismRecipeTypes.TYPE_ENERGY_CONVERSION.value();
    }

    /// Helper interface for constructing [ItemStackToEnergyRecipes][ItemStackToEnergyRecipe]
    ///
    /// @since 10.8.0
    @FunctionalInterface
    public interface Factory<RECIPE extends ItemStackToEnergyRecipe> {

        /// @param commonInfo Common recipe info.
        /// @param bookInfo   Recipe book info.
        /// @param input      Input ingredient.
        /// @param output     Output energy. Must be greater than zero.
        RECIPE create(CommonInfo commonInfo, EnergyConversionBookInfo bookInfo, ItemStackIngredient input, int output);
    }
}
