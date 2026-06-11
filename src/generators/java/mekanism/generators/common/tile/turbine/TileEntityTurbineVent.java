package mekanism.generators.common.tile.turbine;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.IContentsListener;
import mekanism.api.fluid.IFluidTank;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.IContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

public class TileEntityTurbineVent extends TileEntityTurbineCasing {

    private final Map<Direction, BlockCapabilityCache<ResourceHandler<FluidResource>, @Nullable Direction>> capabilityCaches = new EnumMap<>(Direction.class);

    public TileEntityTurbineVent(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.TURBINE_VENT, pos, state);
    }

    @Override
    protected IContainerHolder<IFluidTank> getInitialFluidTanks(IContentsListener listener) {
        return _ -> getMultiblock().getFluidTanks();
    }

    @Override
    public boolean persists(IContainerType<?, ?> type) {
        //Do not handle fluid when it comes to syncing it/saving this tile to disk
        if (type == ContainerType.FLUID) {
            return false;
        }
        return super.persists(type);
    }

    public void addFluidTargetCapability(List<BlockCapabilityCache<ResourceHandler<FluidResource>, @Nullable Direction>> outputTargets, Direction side) {
        BlockCapabilityCache<ResourceHandler<FluidResource>, @Nullable Direction> cache = capabilityCaches.get(side);
        if (cache == null) {
            cache = Capabilities.FLUID.createCache((ServerLevel) level, worldPosition.relative(side), side.getOpposite());
            capabilityCaches.put(side, cache);
        }
        outputTargets.add(cache);
    }
}