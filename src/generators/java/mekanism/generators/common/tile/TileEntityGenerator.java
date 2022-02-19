package mekanism.generators.common.tile;

import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.RelativeSide;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.Direction;

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
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD_CAPABILITY, this));
    }

    protected RelativeSide[] getEnergySides() {
        return new RelativeSide[]{RelativeSide.FRONT};
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = BasicEnergyContainer.output(MachineEnergyContainer.validateBlock(this).getStorage(), this), getEnergySides());
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (MekanismUtils.canFunction(this)) {
            //TODO: Cache the directions or maybe even make some generators have a side config/ejector component and move this to the ejector component?
            Set<Direction> emitDirections = EnumSet.noneOf(Direction.class);
            Direction direction = getDirection();
            for (RelativeSide energySide : getEnergySides()) {
                emitDirections.add(energySide.getDirection(direction));
            }
            CableUtils.emit(emitDirections, energyContainer, this, getMaxOutput());
        }
    }

    @ComputerMethod
    public FloatingLong getMaxOutput() {
        return output;
    }

    public BasicEnergyContainer getEnergyContainer() {
        return energyContainer;
    }
}