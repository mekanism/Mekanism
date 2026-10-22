package mekanism.api.recipes;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.book.DissolutionBookCategory;
import mekanism.api.recipes.book.DissolutionBookCategory.DissolutionBookInfo;
import mekanism.api.recipes.display.recipe.DissolutionRecipeDisplay;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

public abstract class ChemicalDissolutionRecipe extends ItemStackChemicalToObjectRecipe<ChemicalStackTemplate, DissolutionBookCategory, DissolutionBookInfo> {

    private static final Holder<Item> CHEMICAL_DISSOLUTION_CHAMBER = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_dissolution_chamber"));

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    ///
    /// @since 10.8.0
    protected ChemicalDissolutionRecipe(Recipe.CommonInfo commonInfo, DissolutionBookInfo bookInfo) {
        super(commonInfo, bookInfo);
    }

    @Override
    public final RecipeType<ChemicalDissolutionRecipe> getType() {
        return MekanismRecipeTypes.TYPE_DISSOLUTION.value();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new DissolutionRecipeDisplay(
              getItemInput().display(),
              getChemicalInput().display(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(CHEMICAL_DISSOLUTION_CHAMBER)
        ));
    }

    /// Helper interface for constructing [ChemicalDissolutionRecipes][ChemicalDissolutionRecipe]
    ///
    /// @since 10.8.0
    @FunctionalInterface
    public interface Factory<RECIPE extends ChemicalDissolutionRecipe> {

        /// @param commonInfo  Common recipe info.
        /// @param bookInfo    Recipe book info.
        /// @param itemInput     Item input.
        /// @param chemicalInput Chemical input.
        /// @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
        /// @param output        Output.
        RECIPE create(CommonInfo commonInfo, DissolutionBookInfo bookInfo, ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, boolean perTickUsage,
              ChemicalStackTemplate output);
    }
}
