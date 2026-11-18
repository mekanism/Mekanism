package mekanism.common.tile.laser;

import mekanism.api.IContentsListener;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.single.BasicSingleHolder;
import mekanism.common.capabilities.holder.single.ISingleContainerHolder;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityLaser extends TileEntityBasicLaser {

    public TileEntityLaser(BlockPos pos, BlockState state) {
        super(MekanismBlocks.LASER, pos, state);
    }

    @Override
    protected ISingleContainerHolder<IEnergyContainer> getInitialEnergyContainer(IContentsListener listener) {
        energyContainer = MachineEnergyContainer.input(this, listener);
        return new BasicSingleHolder<>(energyContainer, facingSupplier, BACK_ONLY);
    }

    @Override
    protected int toFire() {
        return ((MachineEnergyContainer<?>) energyContainer).getEnergyPerTick();
    }

    @Override
    public float getInitialVolume() {
        return 0.3f;
    }
}