package mekanism.common.tile.multiblock;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class TileEntityThermalEvaporationBlock extends TileEntityMultiblock<EvaporationMultiblockData> {

    public TileEntityThermalEvaporationBlock() {
        this(MekanismBlocks.THERMAL_EVAPORATION_BLOCK);
    }

    public TileEntityThermalEvaporationBlock(IBlockProvider provider) {
        super(provider);
    }

    @Override
    public void onNeighborChange(Block block, BlockPos neighborPos) {
        super.onNeighborChange(block, neighborPos);
        if (!isRemote()) {
            if (getMultiblock().isFormed()) {
                if (getMultiblock().isSolarSpot(neighborPos)) {
                    getMultiblock().updateSolars(getWorld());
                }
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
}