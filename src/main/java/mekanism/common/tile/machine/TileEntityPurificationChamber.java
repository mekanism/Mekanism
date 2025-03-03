package mekanism.common.tile.machine;

import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
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
import org.jetbrains.annotations.NotNull;

public class TileEntityPurificationChamber extends TileEntityAdvancedElectricMachine {

    public TileEntityPurificationChamber(BlockPos pos, BlockState state) {
        super(MekanismBlocks.PURIFICATION_CHAMBER, pos, state, BASE_TICKS_REQUIRED);
    }

    @NotNull
    @Override
    public IMekanismRecipeTypeProvider<SingleItemChemicalRecipeInput, ItemStackChemicalToItemStackRecipe, ItemChemical<ItemStackChemicalToItemStackRecipe>> getRecipeType() {
        return MekanismRecipeType.PURIFYING;
    }

    @Override
    public IRecipeViewerRecipeType<ItemStackChemicalToItemStackRecipe> recipeViewerType() {
        return RecipeViewerRecipeType.PURIFYING;
    }

    @Override
    protected boolean useStatisticalMechanics() {
        return MekanismConfig.usage.randomizedConsumption.get();
    }
}