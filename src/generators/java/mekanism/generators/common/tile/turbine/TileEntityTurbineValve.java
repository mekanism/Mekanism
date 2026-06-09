package mekanism.generators.common.tile.turbine;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.IContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import org.jspecify.annotations.Nullable;

public class TileEntityTurbineValve extends TileEntityTurbineCasing {

    private final Map<Direction, BlockCapabilityCache<EnergyHandler, @Nullable Direction>> energyCapabilityCaches = new EnumMap<>(Direction.class);

    public TileEntityTurbineValve(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.TURBINE_VALVE, pos, state);
    }

    @Override
    public IContainerHolder<IChemicalTank> getInitialChemicalTanks(IContentsListener listener) {
        return _ -> getMultiblock().getChemicalTanks();
    }

    @Override
    protected @Nullable IEnergyContainerHolder getInitialEnergyContainer(IContentsListener listener) {
        return _ -> getMultiblock().getEnergyContainer();
    }

    @Override
    public boolean persists(IContainerType<?, ?> type) {
        //Do not handle gas when it comes to syncing it/saving this tile to disk
        if (type == ContainerType.CHEMICAL || type == ContainerType.ENERGY) {
            return false;
        }
        return super.persists(type);
    }

    public void addEnergyTargetCapability(List<BlockCapabilityCache<EnergyHandler, @Nullable Direction>> outputTargets, Direction side) {
        BlockCapabilityCache<EnergyHandler, @Nullable Direction> cache = energyCapabilityCaches.get(side);
        if (cache == null) {
            cache = Capabilities.ENERGY.createCache((ServerLevel) level, worldPosition.relative(side), side.getOpposite());
            energyCapabilityCaches.put(side, cache);
        }
        outputTargets.add(cache);
    }

    @Override
    public int getRedstoneLevel() {
        return getMultiblock().getCurrentRedstoneLevel();
    }
}