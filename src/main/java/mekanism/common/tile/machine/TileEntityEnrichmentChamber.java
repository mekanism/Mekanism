package mekanism.common.tile.machine;

import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.book.EnrichingBookCategory;
import mekanism.api.recipes.book.EnrichingBookCategory.EnrichingBookInfo;
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

public class TileEntityEnrichmentChamber extends TileEntityElectricMachine<EnrichingBookCategory, EnrichingBookInfo> {

    public TileEntityEnrichmentChamber(BlockPos pos, BlockState state) {
        super(MekanismBlocks.ENRICHMENT_CHAMBER, pos, state, BASE_TICKS_REQUIRED);
    }

    @Override
    public IMekanismRecipeTypeProvider<SingleRecipeInput, EnrichingBookCategory, EnrichingBookInfo, ItemStackToItemStackRecipe<EnrichingBookCategory, EnrichingBookInfo>,
          SingleItem<ItemStackToItemStackRecipe<EnrichingBookCategory, EnrichingBookInfo>>> getRecipeType() {
        return MekanismRecipeType.ENRICHING;
    }

    @Override
    public IRecipeViewerRecipeType<ItemStackToItemStackRecipe<EnrichingBookCategory, EnrichingBookInfo>> recipeViewerType() {
        return RecipeViewerRecipeType.ENRICHING;
    }
}