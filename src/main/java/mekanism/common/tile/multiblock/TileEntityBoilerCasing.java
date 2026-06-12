package mekanism.common.tile.multiblock;

import mekanism.api.IContentsListener;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.single.ISingleContainerHolder;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.IContainerType;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.lib.multiblock.MekanismMultiblocks;
import mekanism.common.lib.multiblock.MultiblockType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityBoilerCasing extends TileEntityMultiblock<BoilerMultiblockData> {

    public TileEntityBoilerCasing(BlockPos pos, BlockState state) {
        this(MekanismBlocks.BOILER_CASING, pos, state);
    }

    public TileEntityBoilerCasing(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    public BoilerMultiblockData createMultiblock() {
        return new BoilerMultiblockData(this);
    }

    @Override
    public MultiblockType<BoilerMultiblockData> getMultiblockType() {
        return MekanismMultiblocks.BOILER;
    }

    @Override
    protected ISingleContainerHolder<IHeatCapacitor> getInitialHeatCapacitor(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return _ -> getMultiblock().getHeatCapacitor();
    }

    @Override
    public boolean persists(IContainerType<?, ?> type) {
        //Do not handle heat when it comes to syncing it/saving this tile to disk
        if (type == ContainerType.HEAT) {
            return false;
        }
        return super.persists(type);
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level, BoilerMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(level, multiblock);
        if (multiblock.isFormed()) {
            simulateAdjacent();
        }
        return needsPacket;
    }
}