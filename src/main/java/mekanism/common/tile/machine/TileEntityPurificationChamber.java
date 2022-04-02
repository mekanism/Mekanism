package mekanism.common.tile.machine;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemChemical;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityPurificationChamber extends TileEntityAdvancedElectricMachine {

    public TileEntityPurificationChamber(BlockPos pos, BlockState state) {
        super(MekanismBlocks.PURIFICATION_CHAMBER, pos, state, BASE_TICKS_REQUIRED);
    }

    @Nonnull
    @Override
    public IMekanismRecipeTypeProvider<ItemStackGasToItemStackRecipe, ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> getRecipeType() {
        return MekanismRecipeType.PURIFYING;
    }

    @Override
    protected boolean useStatisticalMechanics() {
        return true;
    }
}