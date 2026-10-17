package mekanism.common.tile.machine;

import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.book.PurifyingBookCategory;
import mekanism.api.recipes.book.PurifyingBookCategory.PurifyingBookInfo;
import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemChemical;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityPurificationChamber extends TileEntityAdvancedElectricMachine<PurifyingBookCategory, PurifyingBookInfo> {

    public TileEntityPurificationChamber(BlockPos pos, BlockState state) {
        super(MekanismBlocks.PURIFICATION_CHAMBER, pos, state, BASE_TICKS_REQUIRED);
    }

    @Override
    public IMekanismRecipeTypeProvider<SingleItemChemicalRecipeInput, PurifyingBookCategory, PurifyingBookInfo, ItemStackChemicalToItemStackRecipe<PurifyingBookCategory, PurifyingBookInfo>,
          ItemChemical<ItemStackChemicalToItemStackRecipe<PurifyingBookCategory, PurifyingBookInfo>>> getRecipeType() {
        return MekanismRecipeType.PURIFYING;
    }

    @Override
    public IRecipeViewerRecipeType<ItemStackChemicalToItemStackRecipe<PurifyingBookCategory, PurifyingBookInfo>> recipeViewerType() {
        return RecipeViewerRecipeType.PURIFYING;
    }

    @Override
    protected boolean useStatisticalMechanics() {
        return MekanismConfig.usage.randomizedConsumption.get();
    }
}