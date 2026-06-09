package mekanism.generators.common.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.BasicEnergyHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnergyUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import org.jspecify.annotations.Nullable;

public abstract class TileEntityGenerator extends TileEntityMekanism {

    private static final Set<RelativeSide> ENERGY_SIDES = Set.of(RelativeSide.FRONT);

    @Nullable
    private List<BlockCapabilityCache<EnergyHandler, @Nullable Direction>> outputCaches;
    private BasicEnergyContainer energyContainer;

    /**
     * Generator -- a block that produces energy. It has a certain amount of fuel it can store as well as an output rate.
     */
    public TileEntityGenerator(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    protected Set<RelativeSide> getEnergySides() {
        return ENERGY_SIDES;
    }

    @Override
    protected @Nullable IEnergyContainerHolder getInitialEnergyContainer(IContentsListener listener) {
        energyContainer = BasicEnergyContainer.output(MachineEnergyContainer.validateBlock(this).getStorage(), listener);
        return new BasicEnergyHolder(energyContainer, facingSupplier, getEnergySides());
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level) {
        boolean sendUpdatePacket = super.onUpdateServer(level);
        if (canFunction()) {
            //TODO: Maybe even make some generators have a side config/ejector component and move this to the ejector component?
            if (outputCaches == null) {
                Direction direction = getDirection();
                Set<RelativeSide> energySides = getEnergySides();
                outputCaches = new ArrayList<>(energySides.size());
                for (RelativeSide energySide : energySides) {
                    Direction side = energySide.getDirection(direction);
                    outputCaches.add(Capabilities.ENERGY.createCache(level, worldPosition.relative(side), side.getOpposite()));
                }
            }
            EnergyUtils.emit(outputCaches, energyContainer, null);
        }
        return sendUpdatePacket;
    }

    @Override
    protected void invalidateDirectionCaches(Direction newDirection) {
        super.invalidateDirectionCaches(newDirection);
        outputCaches = null;
    }

    public BasicEnergyContainer energyContainer() {
        return energyContainer;
    }

    @ComputerMethod(methodDescription = "Get the amount of energy produced by this generator in the last tick.")
    abstract int getProductionRate();
}
