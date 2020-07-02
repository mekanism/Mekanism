package mekanism.generators.common.tile;

import java.util.EnumSet;
import javax.annotation.Nonnull;
import mekanism.api.RelativeSide;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;

public abstract class TileEntityGenerator extends TileEntityMekanism {

    /**
     * Output per tick this generator can transfer.
     */
    public FloatingLong output;
    private BasicEnergyContainer energyContainer;

    /**
     * Generator -- a block that produces energy. It has a certain amount of fuel it can store as well as an output rate.
     */
    public TileEntityGenerator(IBlockProvider blockProvider, @Nonnull FloatingLong out) {
        super(blockProvider);
        output = out;
    }

    protected RelativeSide getEnergySide() {
        return RelativeSide.FRONT;
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = BasicEnergyContainer.output(MachineEnergyContainer.validateBlock(this).getStorage(), this), getEnergySide());
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        if (MekanismUtils.canFunction(this)) {
            CableUtils.emit(EnumSet.of(getEnergySide().getDirection(getDirection())), energyContainer, this, getMaxOutput());
        }
    }

    @Override
    public int getActiveLightValue() {
        return 8;
    }

    public FloatingLong getMaxOutput() {
        return output;
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    public BasicEnergyContainer getEnergyContainer() {
        return energyContainer;
    }
}