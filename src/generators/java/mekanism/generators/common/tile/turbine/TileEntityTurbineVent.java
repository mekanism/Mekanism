package mekanism.generators.common.tile.turbine;

import java.util.Collections;
import mekanism.api.IContentsListener;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.FluidUtils;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityTurbineVent extends TileEntityTurbineCasing {

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
            FluidUtils.emit(multiblock.getDirectionsToEmit(getBlockPos()), multiblock.ventTank, this);
        }
        return needsPacket;
    }

    @Override
    public boolean persists(SubstanceType type) {
        //Do not handle fluid when it comes to syncing it/saving this tile to disk
        if (type == SubstanceType.FLUID) {
            return false;
        }
        return super.persists(type);
    }
}