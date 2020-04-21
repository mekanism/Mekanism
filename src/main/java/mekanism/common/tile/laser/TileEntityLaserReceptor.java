package mekanism.common.tile.laser;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.inventory.AutomationType;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import net.minecraft.util.Direction;

public abstract class TileEntityLaserReceptor extends TileEntityBasicLaser implements ILaserReceptor {

    public TileEntityLaserReceptor(IBlockProvider blockProvider) {
        super(blockProvider);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.LASER_RECEPTOR_CAPABILITY, this));
    }

    @Override
    public void receiveLaserEnergy(@Nonnull FloatingLong energy, Direction side) {
        energyContainer.insert(energy, Action.EXECUTE, AutomationType.INTERNAL);
    }

    @Override
    public boolean canLasersDig() {
        return false;
    }
}