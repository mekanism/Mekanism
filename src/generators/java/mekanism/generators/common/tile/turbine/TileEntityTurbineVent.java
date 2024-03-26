package mekanism.generators.common.tile.turbine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.IContentsListener;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.lib.multiblock.IMultiblockEjector;
import mekanism.common.util.FluidUtils;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityTurbineVent extends TileEntityTurbineCasing implements IMultiblockEjector {

    private final Map<Direction, BlockCapabilityCache<IFluidHandler, @Nullable Direction>> capabilityCaches = new EnumMap<>(Direction.class);
    private final List<BlockCapabilityCache<IFluidHandler, @Nullable Direction>> outputTargets = new ArrayList<>();

    public TileEntityTurbineVent(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.TURBINE_VENT, pos, state);
    }

    @NotNull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        return side -> {
            TurbineMultiblockData multiblock = getMultiblock();
            return multiblock.isFormed() ? multiblock.ventTanks : Collections.emptyList();
        };
    }

    @Override
    protected boolean onUpdateServer(TurbineMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(multiblock);
        if (multiblock.isFormed()) {
            FluidUtils.emit(outputTargets, multiblock.ventTank);
        }
        return needsPacket;
    }

    @Override
    public boolean persists(ContainerType<?, ?, ?> type) {
        //Do not handle fluid when it comes to syncing it/saving this tile to disk
        if (type == ContainerType.FLUID) {
            return false;
        }
        return super.persists(type);
    }

    @Override
    public void setEjectSides(Set<Direction> sides) {
        outputTargets.clear();
        for (Direction side : sides) {
            outputTargets.add(capabilityCaches.computeIfAbsent(side, s -> Capabilities.FLUID.createCache((ServerLevel) level, worldPosition.relative(s), s.getOpposite())));
        }
    }
}