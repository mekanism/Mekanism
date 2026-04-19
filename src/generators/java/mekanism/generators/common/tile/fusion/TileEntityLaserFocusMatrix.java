package mekanism.generators.common.tile.fusion;

import mekanism.api.lasers.ILaserReceptor;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityLaserFocusMatrix extends TileEntityFusionReactorBlock implements ILaserReceptor {

    public TileEntityLaserFocusMatrix(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.LASER_FOCUS_MATRIX, pos, state);
    }

    @Override
    public void receiveLaserEnergy(long energy) {
        FusionReactorMultiblockData multiblock = getMultiblock();
        if (multiblock.isFormed()) {
            multiblock.addTemperatureFromEnergyInput(energy);
        }
    }

    @Override
    public InteractionResult onRightClick(Player player) {
        if (!isRemote() && player.isCreative()) {
            FusionReactorMultiblockData multiblock = getMultiblock();
            if (multiblock.isFormed()) {
                multiblock.setPlasmaTemp(1_000_000_000);
                return InteractionResult.SUCCESS_SERVER;
            }
        }
        //TODO - 26.1: Don't call super on the client side if it was successful?
        return super.onRightClick(player);
    }

    @Override
    public boolean canLasersDig() {
        return false;
    }
}