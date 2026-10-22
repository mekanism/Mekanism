package mekanism.api.recipes;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.recipes.book.NucleosynthesizingBookCategory;
import mekanism.api.recipes.book.NucleosynthesizingBookCategory.NucleosynthesizingBookInfo;
import mekanism.api.recipes.display.recipe.NucleosynthesizingRecipeDisplay;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Extension of [ItemStackChemicalToItemStackRecipe] with a defined amount of ticks needed to process.
///
/// Input: ItemStack
///
/// Input: Chemical (Base value, will be multiplied by a per tick amount)
///
/// Output: ItemStack
///
/// @apiNote Nucleosynthesizers can process this recipe type.
public abstract class NucleosynthesizingRecipe extends ItemStackChemicalToItemStackRecipe<NucleosynthesizingBookCategory, NucleosynthesizingBookInfo> {

    private static final Holder<Item> ANTIPROTONIC_NUCLEOSYNTHESIZER = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "antiprotonic_nucleosynthesizer"));

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    ///
    /// @since 10.8.0
    protected NucleosynthesizingRecipe(Recipe.CommonInfo commonInfo, NucleosynthesizingBookInfo bookInfo) {
        super(commonInfo, bookInfo);
    }

    @Override
    public final RecipeType<NucleosynthesizingRecipe> getType() {
        return MekanismRecipeTypes.TYPE_NUCLEOSYNTHESIZING.value();
    }

    /// Gets the duration in ticks this recipe takes to complete.
    public abstract int getDuration();

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new NucleosynthesizingRecipeDisplay(
              getItemInput().display(),
              getChemicalInput().display(),
              perTickUsage(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(ANTIPROTONIC_NUCLEOSYNTHESIZER),
              getDuration()
        ));
    }

    /// Helper interface for constructing [NucleosynthesizingRecipes][NucleosynthesizingRecipe]
    ///
    /// @since 10.8.0
    @FunctionalInterface
    public interface Factory<RECIPE extends NucleosynthesizingRecipe> {

        /// @param commonInfo    Common recipe info.
        /// @param bookInfo      Recipe book info.
        /// @param itemInput     Item input ingredient.
        /// @param chemicalInput Chemical input ingredient.
        /// @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
        /// @param output        Output item.
        /// @param duration      Duration in ticks that it takes the recipe to complete. Must be greater than zero.
        RECIPE create(CommonInfo commonInfo, NucleosynthesizingBookInfo bookInfo, ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, boolean perTickUsage,
              ItemStackTemplate output, int duration);
    }
}
