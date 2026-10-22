package mekanism.api.recipes;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.recipes.TwoInputMekRecipe.SimpleTwoInputRecipe;
import mekanism.api.recipes.book.CombiningBookCategory;
import mekanism.api.recipes.book.CombiningBookCategory.CombiningBookInfo;
import mekanism.api.recipes.display.recipe.CombiningRecipeDisplay;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.vanilla_input.BiItemRecipeInput;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.Nullable;

/// Main Input: ItemStack
///
/// Secondary/Extra Input: ItemStack
///
/// Output: ItemStack
///
/// @apiNote Combiners and Combining Factories can process this recipe type.
public abstract class CombinerRecipe extends SimpleTwoInputRecipe<Item, ItemStack, ItemStackIngredient, BiItemRecipeInput, ItemStackTemplate, CombiningBookCategory, CombiningBookInfo> {

    private static final Holder<Item> COMBINER = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "combiner"));

    @Nullable
    private PlacementInfo placementInfo;

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    ///
    /// @since 10.8.0
    protected CombinerRecipe(Recipe.CommonInfo commonInfo, CombiningBookInfo bookInfo) {
        super(commonInfo, bookInfo);
    }

    /// Gets the main input ingredient.
    public abstract ItemStackIngredient getMainInput();

    @Override
    public final ItemStackIngredient getInputA() {
        return getMainInput();
    }

    /// Gets the secondary input ingredient.
    public abstract ItemStackIngredient getExtraInput();

    @Override
    public final ItemStackIngredient getInputB() {
        return getExtraInput();
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(List.of(
                  getMainInput().ingredient().ingredient(),
                  getExtraInput().ingredient().ingredient()
            ));
        }
        return this.placementInfo;
    }

    @Override
    public ItemStack assemble(BiItemRecipeInput input) {
        if (!getInputA().hasNoMatchingInstances() && !getInputB().hasNoMatchingInstances()) {
            ItemStack mainInput = input.main();
            ItemStack extraInput = input.secondary();
            if (test(mainInput, extraInput)) {
                return getOutput(mainInput, extraInput).create();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean matches(BiItemRecipeInput input, Level level) {
        return test(input.main(), input.secondary());
    }

    @Override
    public final RecipeType<CombinerRecipe> getType() {
        return MekanismRecipeTypes.TYPE_COMBINING.value();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new CombiningRecipeDisplay(
              getMainInput().display(),
              getExtraInput().display(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(COMBINER)
        ));
    }

    /// Helper interface for constructing [CombinerRecipes][CombinerRecipe]
    ///
    /// @since 10.8.0
    @FunctionalInterface
    public interface Factory<RECIPE extends CombinerRecipe> {

        /// @param commonInfo Common recipe info.
        /// @param bookInfo   Recipe book info.
        /// @param mainInput  Main input.
        /// @param extraInput Secondary/extra input.
        /// @param output     Output item.
        RECIPE create(CommonInfo commonInfo, CombiningBookInfo bookInfo, ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStackTemplate output);
    }
}
