package mekanism.common.tile.machine;

import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.book.CompressingBookCategory;
import mekanism.api.recipes.book.CompressingBookCategory.CompressingBookInfo;
import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemChemical;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityOsmiumCompressor extends TileEntityAdvancedElectricMachine<CompressingBookCategory, CompressingBookInfo> {

    public TileEntityOsmiumCompressor(BlockPos pos, BlockState state) {
        super(MekanismBlocks.OSMIUM_COMPRESSOR, pos, state, BASE_TICKS_REQUIRED);
    }

    @Override
    public IMekanismRecipeTypeProvider<SingleItemChemicalRecipeInput, CompressingBookCategory, CompressingBookInfo, ItemStackChemicalToItemStackRecipe<CompressingBookCategory, CompressingBookInfo>,
          ItemChemical<ItemStackChemicalToItemStackRecipe<CompressingBookCategory, CompressingBookInfo>>> getRecipeType() {
        return MekanismRecipeType.COMPRESSING;
    }

    @Override
    public IRecipeViewerRecipeType<ItemStackChemicalToItemStackRecipe<CompressingBookCategory, CompressingBookInfo>> recipeViewerType() {
        return RecipeViewerRecipeType.COMPRESSING;
    }
}