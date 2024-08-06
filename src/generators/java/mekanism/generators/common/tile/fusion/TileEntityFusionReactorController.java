package mekanism.generators.common.tile.fusion;

import mekanism.common.attachments.containers.ContainerType;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityFusionReactorController extends TileEntityFusionReactorBlock {

    public TileEntityFusionReactorController(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.FUSION_REACTOR_CONTROLLER, pos, state);
        delaySupplier = NO_DELAY;
    }

    @Override
    protected boolean onUpdateServer(FusionReactorMultiblockData multiblock) {
        boolean needsPacket = super.onUpdateServer(multiblock);
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
    public boolean persists(ContainerType<?, ?, ?> type) {
        if (type == ContainerType.CHEMICAL || type == ContainerType.FLUID || type == ContainerType.HEAT) {
            return false;
        }
        return super.persists(type);
    }
}