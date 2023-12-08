package mekanism.common.tile.laser;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class TileEntityLaserReceptor extends TileEntityBasicLaser implements ILaserReceptor {

    public TileEntityLaserReceptor(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    public void receiveLaserEnergy(@NotNull FloatingLong energy) {
        energyContainer.insert(energy, Action.EXECUTE, AutomationType.INTERNAL);
    }

    @Override
    public boolean canLasersDig() {
        return false;
    }
}