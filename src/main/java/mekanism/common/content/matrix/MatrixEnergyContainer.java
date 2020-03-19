package mekanism.common.content.matrix;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Map;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

public class MatrixEnergyContainer implements IEnergyContainer {

    private Map<BlockPos, InductionProviderTier> providers = new Object2ObjectOpenHashMap<>();
    private Map<BlockPos, IEnergyContainer> cells = new Object2ObjectOpenHashMap<>();
    private Set<BlockPos> invalidPositions = new ObjectOpenHashSet<>();

    //TODO: Maybe replace queuedOutput and queuedInput with a BigDecimal or something
    private double queuedOutput;
    private double queuedInput;
    private double lastOutput;
    private double lastInput;

    private double cachedTotal;
    private double transferCap;
    private double storageCap;

    private final TileEntityInductionCasing tile;

    public MatrixEnergyContainer(TileEntityInductionCasing tile) {
        this.tile = tile;
    }

    public void addCell(Coord4D coord, TileEntityInductionCell cell) {
        //As we already have the two different variables just pass them instead of accessing world to get tile again
        MachineEnergyContainer<TileEntityInductionCell> energyContainer = cell.getEnergyContainer();
        cells.put(coord.getPos(), energyContainer);
        storageCap += energyContainer.getMaxEnergy();
        cachedTotal += energyContainer.getEnergy();
    }

    public void addProvider(Coord4D coord, TileEntityInductionProvider provider) {
        providers.put(coord.getPos(), provider.tier);
        transferCap += provider.tier.getOutput();
    }

    //TODO: I believe this is needed or at least will be after we eventually rewrite some of the multiblock system
    // currently I think it may just be rechecking the entire structure when something changes internally
    // We need to validate that does properly happen even if the cell is floating in the middle and not touching any walls
    // We may also want to make cells and providers extend TileEntityInternalMultiblock
    public void removeInternal(Coord4D coord) {
        BlockPos pos = coord.getPos();
        if (!invalidPositions.contains(pos)) {
            if (providers.containsKey(pos)) {
                //It is a provider
                transferCap -= providers.get(pos).getOutput();
            } else if (cells.containsKey(pos)) {
                //It is a cell
                //TODO: Handle this better, as I believe we *technically* could have this cause the cached total to become negative
                // It may work better if we just flush the buffer writing immediately, and then recalculate the cached totals/caps
                IEnergyContainer cellContainer = cells.get(pos);
                storageCap += cellContainer.getMaxEnergy();
                cachedTotal -= cellContainer.getEnergy();
            }
            invalidPositions.add(pos);
        }
    }

    private double getRemainingInput() {
        return transferCap - queuedInput;
    }

    private double getRemainingOutput() {
        return transferCap - queuedOutput;
    }

    public void tick() {
        if (!invalidPositions.isEmpty()) {
            for (BlockPos invalidPosition : invalidPositions) {
                cells.remove(invalidPosition);
                providers.remove(invalidPosition);
            }
            invalidPositions.clear();
        }
        //See comment in getEnergyPostQueue for explanation of how lastChange is calculated.
        double lastChange = queuedInput - queuedOutput;
        if (lastChange < 0) {
            //We are removing energy
            removeEnergy(-lastChange);
        } else if (lastChange > 0) {
            //we are adding energy
            addEnergy(lastChange);
        }
        cachedTotal += lastChange;

        lastInput = queuedInput;
        queuedInput = 0;
        lastOutput = queuedOutput;
        queuedOutput = 0;
    }

    private void addEnergy(double energy) {
        for (IEnergyContainer container : cells.values()) {
            //Note: inserting into the cell's energy container handles marking the cell for saving if it changes
            double remainder = container.insert(energy, Action.EXECUTE, AutomationType.INTERNAL);
            if (remainder < energy) {
                //Our cell accepted at least some energy
                if (remainder <= 0) {
                    //Check less than equal rather than just equal in case something went wrong
                    // and break if we don't have any energy left to add
                    break;
                }
                energy = remainder;
            }
        }
    }

    private void removeEnergy(double energy) {
        for (IEnergyContainer container : cells.values()) {
            //Note: extracting from the cell's energy container handles marking the cell for saving if it changes
            double extracted = container.extract(energy, Action.EXECUTE, AutomationType.INTERNAL);
            if (extracted > 0) {
                energy -= extracted;
                if (energy <= 0) {
                    //Check less than equal rather than just equal in case something went wrong
                    // and break if we don't need to remove any more energy
                    break;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return The energy post queue when this container next actually updates/saves to disk
     */
    @Override
    public double getEnergy() {
        return cachedTotal + queuedInput - queuedOutput;
    }

    @Override
    public void setEnergy(double energy) {
        //Throws a RuntimeException as specified is allowed when something unexpected happens
        // As setEnergy is more meant to be used as an internal method
        //TODO: Maybe we need to just set some arbitrary cached value, given I believe the only place this
        // might end up getting called is from the container tracker
        throw new RuntimeException("Unexpected call to setEnergy. The matrix energy container does not support directly setting the energy.");
    }

    @Override
    public double insert(double amount, Action action, AutomationType automationType) {
        if (amount <= 0 || tile.structure == null) {
            return amount;
        }
        double toAdd = Math.min(Math.min(amount, getRemainingInput()), getNeeded());
        if (toAdd <= 0) {
            //Exit if we don't actually have anything to add, either due to how much we need
            // or due to the our remaining rate limit
            return amount;
        }
        if (action.execute()) {
            //Increase how much we are inputting
            queuedInput += toAdd;
        }
        return amount - toAdd;
    }

    @Override
    public double extract(double amount, Action action, AutomationType automationType) {
        if (isEmpty() || amount <= 0 || tile.structure == null) {
            return 0;
        }
        //We limit it overall by the amount we can extract plus how much energy we have
        // as we want to be as accurate as possible with the values we return
        // It is possible that the energy we have stored is a lot less than the amount we
        // can output at once such as if the matrix is almost empty.
        amount = Math.min(Math.min(amount, getRemainingOutput()), getEnergy());
        if (amount > 0 && action.execute()) {
            //Increase how much we are outputting by the amount we accepted
            queuedOutput += amount;
        }
        return amount;
    }

    @Override
    public double getMaxEnergy() {
        return storageCap;
    }

    @Override
    public void onContentsChanged() {
        //Unused
    }

    @Override
    public CompoundNBT serializeNBT() {
        //TODO: Figure out the serialization/deserialization
        return new CompoundNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {

    }
}