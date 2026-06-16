package mekanism.common.tile.multiblock;

import mekanism.api.IContentsListener;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.single.ISingleContainerHolder;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class TileEntityThermalEvaporationController extends TileEntityThermalEvaporationBlock {

    public TileEntityThermalEvaporationController(BlockPos pos, BlockState state) {
        super(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, pos, state);
        delaySupplier = NO_DELAY;
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level, EvaporationMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(level, multiblock);
        setActive(multiblock.isFormed());
        return needsPacket;
    }

    @Override
    public double simulateAdjacent(TransactionContext transaction) {
        return 0;//it's a screen, mostly
    }

    @Nullable
    @Override
    protected ISingleContainerHolder<IHeatCapacitor> getInitialHeatCapacitor(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return null;//it's a screen, mostly
    }

    @Override
    public boolean canBeMaster() {
        return true;
    }
}