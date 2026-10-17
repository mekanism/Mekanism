package mekanism.common.recipe.impl;

import java.util.List;
import mekanism.api.recipes.basic.BasicItemStackToFluidOptionalItemRecipe;
import mekanism.api.recipes.book.LiquificationBookCategory;
import mekanism.api.recipes.book.LiquificationBookCategory.LiquificationBookInfo;
import mekanism.api.recipes.display.SimpleMachineRecipeDisplay;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismRecipeSerializersInternal;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import org.jspecify.annotations.Nullable;

public class NutritionalLiquifierRecipe extends BasicItemStackToFluidOptionalItemRecipe<LiquificationBookCategory, LiquificationBookInfo> {

    public NutritionalLiquifierRecipe(Recipe.CommonInfo commonInfo, LiquificationBookInfo bookInfo, ItemStackIngredient input, FluidStackTemplate output,
          @Nullable ItemStackTemplate emptyContainer) {
        this(commonInfo, bookInfo, input, new FluidOptionalItemOutput(output, emptyContainer));
    }

    public NutritionalLiquifierRecipe(Recipe.CommonInfo commonInfo, LiquificationBookInfo bookInfo, ItemStackIngredient input, FluidOptionalItemOutput output) {
        super(commonInfo, bookInfo, input, output);
        //TODO - V11: Make the recipe system support a concept similar to vanilla's "special recipe". The backend already exists
        // but we don't currently have a way for it to get registered and added to the list.
    }

    @Override
    public RecipeType<BasicItemStackToFluidOptionalItemRecipe<LiquificationBookCategory, LiquificationBookInfo>> getType() {
        //TODO: Can we improve this so that it is actually implemented?
        throw new UnsupportedOperationException();
    }

    @Override
    public RecipeSerializer<NutritionalLiquifierRecipe> getSerializer() {
        return MekanismRecipeSerializersInternal.LIQUIFIER.get();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new SimpleMachineRecipeDisplay(
              getInput().display(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(MekanismBlocks.NUTRITIONAL_LIQUIFIER.getItemHolder())
        ));
    }
}