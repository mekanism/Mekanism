package mekanism.common.tile.multiblock;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityThermalEvaporationBlock extends TileEntityMultiblock<EvaporationMultiblockData> {

    public TileEntityThermalEvaporationBlock(BlockPos pos, BlockState state) {
        this(MekanismBlocks.THERMAL_EVAPORATION_BLOCK, pos, state);
    }

    public TileEntityThermalEvaporationBlock(IBlockProvider provider, BlockPos pos, BlockState state) {
        super(provider, pos, state);
    }

    @Override
    public void onNeighborChange(Block block, BlockPos neighborPos) {
        super.onNeighborChange(block, neighborPos);
        if (!isRemote()) {
            EvaporationMultiblockData multiblock = getMultiblock();
            if (multiblock.isFormed()) {
                multiblock.updateSolarSpot(getLevel(), neighborPos);
            }
        }
    }

    @Override
    public EvaporationMultiblockData createMultiblock() {
        return new EvaporationMultiblockData(this);
    }

    @Override
    public MultiblockManager<EvaporationMultiblockData> getManager() {
        return Mekanism.evaporationManager;
    }

    @Override
    public boolean canBeMaster() {
        return false;
    }
}