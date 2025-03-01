package mekanism.generators.common.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.energy.BlockEnergyCapabilityCache;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CableUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TileEntityGenerator extends TileEntityMekanism {

    private static final RelativeSide[] ENERGY_SIDES = {RelativeSide.FRONT};

    @Nullable
    private List<BlockEnergyCapabilityCache> outputCaches;
    /**
     * Output per tick this generator can transfer.
     */
    private long maxOutput;
    private BasicEnergyContainer energyContainer;

    /**
     * Generator -- a block that produces energy. It has a certain amount of fuel it can store as well as an output rate.
     */
    public TileEntityGenerator(Holder<Block> blockProvider, BlockPos pos, BlockState state, @NotNull LongSupplier maxOutput) {
        super(blockProvider, pos, state);
        updateMaxOutputRaw(maxOutput.getAsLong());
    }

    protected RelativeSide[] getEnergySides() {
        return ENERGY_SIDES;
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(facingSupplier);
        builder.addContainer(energyContainer = BasicEnergyContainer.output(MachineEnergyContainer.validateBlock(this).getStorage(), listener), getEnergySides());
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        if (canFunction()) {
            //TODO: Maybe even make some generators have a side config/ejector component and move this to the ejector component?
            if (outputCaches == null) {
                Direction direction = getDirection();
                RelativeSide[] energySides = getEnergySides();
                outputCaches = new ArrayList<>(energySides.length);
                for (RelativeSide energySide : energySides) {
                    Direction side = energySide.getDirection(direction);
                    outputCaches.add(BlockEnergyCapabilityCache.create((ServerLevel) level, worldPosition.relative(side), side.getOpposite()));
                }
            }
            CableUtils.emit(outputCaches, energyContainer, getMaxOutput());
        }
        return sendUpdatePacket;
    }

    @Override
    protected void invalidateDirectionCaches(Direction newDirection) {
        super.invalidateDirectionCaches(newDirection);
        outputCaches = null;
    }

    @ComputerMethod
    public long getMaxOutput() {
        return maxOutput;
    }

    protected void updateMaxOutputRaw(long maxOutput) {
        this.maxOutput = MathUtils.multiplyClamped(maxOutput, 2);
    }

    protected ISyncableData syncableMaxOutput() {
        return SyncableLong.create(this::getMaxOutput, value -> maxOutput = value);
    }

    public BasicEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    @ComputerMethod(methodDescription = "Get the amount of energy produced by this generator in the last tick.")
    abstract long getProductionRate();
}
