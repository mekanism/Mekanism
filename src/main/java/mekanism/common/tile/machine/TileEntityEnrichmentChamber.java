package mekanism.common.tile.machine;

import javax.annotation.Nonnull;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleItem;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityEnrichmentChamber extends TileEntityElectricMachine {

    public TileEntityEnrichmentChamber(BlockPos pos, BlockState state) {
        super(MekanismBlocks.ENRICHMENT_CHAMBER, pos, state, 200);
    }

    @Nonnull
    @Override
    public IMekanismRecipeTypeProvider<ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> getRecipeType() {
        return MekanismRecipeType.ENRICHING;
    }
}