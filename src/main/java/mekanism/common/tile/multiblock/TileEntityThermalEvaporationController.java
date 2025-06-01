package mekanism.common.tile.multiblock;

import mekanism.api.IContentsListener;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TileEntityThermalEvaporationController extends TileEntityThermalEvaporationBlock {

    public TileEntityThermalEvaporationController(BlockPos pos, BlockState state) {
        super(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, pos, state);
        delaySupplier = NO_DELAY;
    }

    @Override
    protected boolean onUpdateServer(EvaporationMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(multiblock);
        setActive(multiblock.isFormed());
        return needsPacket;
    }

    @Override
    public double simulateAdjacent() {
        return 0;//it's a screen, mostly
    }

    @Override
    @Nullable
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return null;//it's a screen, mostly
    }

    @Override
    public boolean canBeMaster() {
        return true;
    }
}