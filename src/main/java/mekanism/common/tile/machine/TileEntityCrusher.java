package mekanism.common.tile.machine;

import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.book.CrushingBookCategory;
import mekanism.api.recipes.book.CrushingBookCategory.CrushingBookInfo;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityCrusher extends TileEntityElectricMachine<CrushingBookCategory, CrushingBookInfo> {

    public TileEntityCrusher(BlockPos pos, BlockState state) {
        super(MekanismBlocks.CRUSHER, pos, state, BASE_TICKS_REQUIRED);
    }

    @Override
    public IMekanismRecipeTypeProvider<SingleRecipeInput, CrushingBookCategory, CrushingBookInfo, ItemStackToItemStackRecipe<CrushingBookCategory, CrushingBookInfo>,
          SingleItem<ItemStackToItemStackRecipe<CrushingBookCategory, CrushingBookInfo>>> getRecipeType() {
        return MekanismRecipeType.CRUSHING;
    }

    @Override
    public IRecipeViewerRecipeType<ItemStackToItemStackRecipe<CrushingBookCategory, CrushingBookInfo>> recipeViewerType() {
        return RecipeViewerRecipeType.CRUSHING;
    }
}