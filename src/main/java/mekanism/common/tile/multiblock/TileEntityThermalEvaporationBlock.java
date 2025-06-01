package mekanism.common.tile.multiblock;

import mekanism.api.IContentsListener;
import mekanism.common.Mekanism;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityThermalEvaporationBlock extends TileEntityMultiblock<EvaporationMultiblockData> {

    public TileEntityThermalEvaporationBlock(BlockPos pos, BlockState state) {
        this(MekanismBlocks.THERMAL_EVAPORATION_BLOCK, pos, state);
    }

    public TileEntityThermalEvaporationBlock(Holder<Block> provider, BlockPos pos, BlockState state) {
        super(provider, pos, state);
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

    @Override
    public boolean persists(ContainerType<?, ?, ?> type) {
        //But that we do not handle fluid when it comes to syncing it/saving this tile to disk
        if (type == ContainerType.FLUID || type == ContainerType.HEAT) {
            return false;
        }
        return super.persists(type);
    }
    
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return side -> getMultiblock().getHeatCapacitors(side);
    }

    @Override
    protected boolean onUpdateServer(EvaporationMultiblockData multiblock) {
        boolean packet = super.onUpdateServer(multiblock);
        if (multiblock.isFormed()) {
            simulateAdjacent();
        }
        return packet;
    }
}