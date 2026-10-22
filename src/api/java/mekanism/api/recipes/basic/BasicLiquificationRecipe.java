package mekanism.api.recipes.basic;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.book.LiquificationBookCategory;
import mekanism.api.recipes.book.LiquificationBookCategory.LiquificationBookInfo;
import mekanism.api.recipes.display.recipe.LiquificationRecipeDisplay;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// @since 10.8.0
public class BasicLiquificationRecipe extends BasicItemStackToFluidOptionalItemRecipe<LiquificationBookCategory, LiquificationBookInfo> {

    private static final Holder<Item> NUTRITIONAL_LIQUIFIER = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "nutritional_liquifier"));

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    /// @param input      Input.
    /// @param output     Output.
    public BasicLiquificationRecipe(Recipe.CommonInfo commonInfo, LiquificationBookInfo bookInfo, ItemStackIngredient input, FluidOptionalItemOutput output) {
        super(commonInfo, bookInfo, input, output);
    }

    @Override
    public RecipeType<ItemStackToFluidOptionalItemRecipe<LiquificationBookCategory, LiquificationBookInfo>> getType() {
        return MekanismRecipeTypes.TYPE_LIQUIFICATION.value();
    }

    @Override
    public RecipeSerializer<BasicLiquificationRecipe> getSerializer() {
        return MekanismRecipeSerializers.LIQUIFICATION.get();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new LiquificationRecipeDisplay(
              getInput().display(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(NUTRITIONAL_LIQUIFIER)
        ));
    }
}