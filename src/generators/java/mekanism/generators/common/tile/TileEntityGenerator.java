package mekanism.generators.common.tile;

import java.util.EnumSet;
import java.util.Set;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class TileEntityGenerator extends TileEntityMekanism {

    /**
     * Output per tick this generator can transfer.
     */
    private FloatingLong maxOutput;
    private BasicEnergyContainer energyContainer;

    /**
     * Generator -- a block that produces energy. It has a certain amount of fuel it can store as well as an output rate.
     */
    public TileEntityGenerator(IBlockProvider blockProvider, BlockPos pos, BlockState state, @NotNull FloatingLongSupplier maxOutput) {
        super(blockProvider, pos, state);
        updateMaxOutputRaw(maxOutput.get());
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD, this));
    }

    protected RelativeSide[] getEnergySides() {
        return new RelativeSide[]{RelativeSide.FRONT};
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = BasicEnergyContainer.output(MachineEnergyContainer.validateBlock(this).getStorage(), listener), getEnergySides());
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
        return maxOutput;
    }

    protected void updateMaxOutputRaw(FloatingLong maxOutput) {
        this.maxOutput = maxOutput.multiply(2);
    }

    protected ISyncableData syncableMaxOutput() {
        return SyncableFloatingLong.create(this::getMaxOutput, value -> maxOutput = value);
    }

    public BasicEnergyContainer getEnergyContainer() {
        return energyContainer;
    }
}