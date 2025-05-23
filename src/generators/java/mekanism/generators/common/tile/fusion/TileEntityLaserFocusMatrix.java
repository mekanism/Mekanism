package mekanism.generators.common.tile.fusion;

import mekanism.api.IConfigurable;
import mekanism.api.WrenchResult;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
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
    public WrenchResult onConfigure(ConfigureContext context) { //onRightClick
        if (!context.is(ConfigureAction.ACTIVATE)) {
            return WrenchResult.PASS;
        }
        if (!isRemote() && context.player().isCreative()) {
            FusionReactorMultiblockData multiblock = getMultiblock();
            if (multiblock.isFormed()) {
                multiblock.setPlasmaTemp(1_000_000_000);
                return WrenchResult.PROBED;
            }
        }
        return WrenchResult.PASS;
    }

    @Override
    public boolean canLasersDig() {
        return false;
    }
}