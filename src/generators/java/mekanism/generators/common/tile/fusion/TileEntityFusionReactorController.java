package mekanism.generators.common.tile.fusion;

import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.IContainerType;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityFusionReactorController extends TileEntityFusionReactorBlock {

    public TileEntityFusionReactorController(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.FUSION_REACTOR_CONTROLLER, pos, state);
        delaySupplier = NO_DELAY;
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level, FusionReactorMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(level, multiblock);
        setActive(multiblock.isFormed());
        return needsPacket;
    }

    @Override
    protected boolean canPlaySound() {
        FusionReactorMultiblockData multiblock = getMultiblock();
        return multiblock.isFormed() && multiblock.isBurning();
    }

    @Override
    public boolean canBeMaster() {
        return true;
    }

    @Override
    public boolean persists(IContainerType<?, ?> type) {
        if (type == ContainerType.CHEMICAL || type == ContainerType.FLUID || type == ContainerType.HEAT) {
            return false;
        }
        return super.persists(type);
    }
}