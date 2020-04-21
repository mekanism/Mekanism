package mekanism.generators.common.tile.reactor;

import javax.annotation.Nonnull;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.util.Direction;

public class TileEntityReactorLaserFocusMatrix extends TileEntityReactorBlock implements ILaserReceptor {

    public TileEntityReactorLaserFocusMatrix() {
        super(GeneratorsBlocks.LASER_FOCUS_MATRIX);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.LASER_RECEPTOR_CAPABILITY, this));
    }

    @Override
    public boolean isFrame() {
        return false;
    }

    @Override
    public void receiveLaserEnergy(@Nonnull FloatingLong energy, Direction side) {
        if (getReactor() != null) {
            getReactor().addTemperatureFromEnergyInput(energy);
        }
    }

    @Override
    public boolean canLasersDig() {
        return false;
    }
}