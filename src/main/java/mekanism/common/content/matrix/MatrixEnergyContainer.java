package mekanism.common.content.matrix;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MatrixEnergyContainer implements IEnergyContainer {
    //TODO: Figure out if this is still accepting power while the matrix is unformed, or maybe the rate is not being obeyed
    // I think when the casing is broken when it first reforms, it does not have a transfer cap setup properly

    private Map<BlockPos, InductionProviderTier> providers = new Object2ObjectOpenHashMap<>();
    private Map<BlockPos, IEnergyContainer> cells = new Object2ObjectOpenHashMap<>();
    private Set<BlockPos> invalidPositions = new ObjectOpenHashSet<>();

    //TODO: Eventually we could look into extending FloatingLong to have a "BigInt" styled implementation that is used by the class
    // at the very least for keeping track of the cached values and rates
    private FloatingLong queuedOutput = FloatingLong.getNewZero();
    private FloatingLong queuedInput = FloatingLong.getNewZero();
    private FloatingLong lastOutput = FloatingLong.ZERO;
    private FloatingLong lastInput = FloatingLong.ZERO;

    private FloatingLong cachedTotal = FloatingLong.getNewZero();
    private FloatingLong transferCap = FloatingLong.getNewZero();
    private FloatingLong storageCap = FloatingLong.getNewZero();

    private final TileEntityInductionCasing tile;

    public MatrixEnergyContainer(TileEntityInductionCasing tile) {
        this.tile = tile;
    }

    public void addCell(Coord4D coord, TileEntityInductionCell cell) {
        //As we already have the two different variables just pass them instead of accessing world to get tile again
        MachineEnergyContainer<TileEntityInductionCell> energyContainer = cell.getEnergyContainer();
        cells.put(coord.getPos(), energyContainer);
        storageCap.plusEqual(energyContainer.getMaxEnergy());
        cachedTotal.plusEqual(energyContainer.getEnergy());
    }

    public void addProvider(Coord4D coord, TileEntityInductionProvider provider) {
        providers.put(coord.getPos(), provider.tier);
        transferCap.plusEqual(provider.tier.getOutput());
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
                transferCap.minusEqual(providers.get(pos).getOutput());
            } else if (cells.containsKey(pos)) {
                //It is a cell
                //TODO: Handle this better, as I believe we *technically* could have this cause the cached total to become negative
                // It may work better if we just flush the buffer writing immediately, and then recalculate the cached totals/caps
                IEnergyContainer cellContainer = cells.get(pos);
                storageCap.plusEqual(cellContainer.getMaxEnergy());
                cachedTotal.minusEqual(cellContainer.getEnergy());
            }
            invalidPositions.add(pos);
        }
    }

    public void invalidate() {
        //Force save
        tick();
        //And reset everything
        cells.clear();
        providers.clear();
        queuedOutput = FloatingLong.getNewZero();
        queuedInput = FloatingLong.getNewZero();
        lastOutput = FloatingLong.ZERO;
        lastInput = FloatingLong.ZERO;
        cachedTotal = FloatingLong.getNewZero();
        transferCap = FloatingLong.getNewZero();
        storageCap = FloatingLong.getNewZero();
    }

    public void tick() {
        if (!invalidPositions.isEmpty()) {
            for (BlockPos invalidPosition : invalidPositions) {
                cells.remove(invalidPosition);
                providers.remove(invalidPosition);
            }
            invalidPositions.clear();
        }
        int compare = queuedInput.compareTo(queuedOutput);
        if (compare < 0) {
            //queuedInput is smaller - we are removing energy
            FloatingLong lastChange = queuedOutput.subtract(queuedInput);
            removeEnergy(lastChange);
            cachedTotal.plusEqual(lastChange);
        } else if (compare > 0) {
            //queuedInput is larger - we are adding energy
            FloatingLong lastChange = queuedInput.subtract(queuedOutput);
            addEnergy(lastChange);
            cachedTotal.plusEqual(lastChange);
        }
        lastInput = queuedInput;
        lastOutput = queuedOutput;
        queuedInput = FloatingLong.getNewZero();
        queuedOutput = FloatingLong.getNewZero();
    }

    private void addEnergy(FloatingLong energy) {
        for (IEnergyContainer container : cells.values()) {
            //Note: inserting into the cell's energy container handles marking the cell for saving if it changes
            FloatingLong remainder = container.insert(energy, Action.EXECUTE, AutomationType.INTERNAL);
            if (remainder.smallerThan(energy)) {
                //Our cell accepted at least some energy
                if (remainder.isEmpty()) {
                    //Check less than equal rather than just equal in case something went wrong
                    // and break if we don't have any energy left to add
                    break;
                }
                energy = remainder;
            }
        }
    }

    private void removeEnergy(FloatingLong energy) {
        for (IEnergyContainer container : cells.values()) {
            //Note: extracting from the cell's energy container handles marking the cell for saving if it changes
            FloatingLong extracted = container.extract(energy, Action.EXECUTE, AutomationType.INTERNAL);
            if (!extracted.isEmpty()) {
                energy.minusEqual(extracted);
                if (energy.isEmpty()) {
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
    public FloatingLong getEnergy() {
        return cachedTotal.add(queuedInput).subtract(queuedOutput);
    }

    @Override
    public void setEnergy(FloatingLong energy) {
        //Throws a RuntimeException as specified is allowed when something unexpected happens
        // As setEnergy is more meant to be used as an internal method
        //TODO: Maybe we need to just set some arbitrary cached value, given I believe the only place this
        // might end up getting called is from the container tracker
        throw new RuntimeException("Unexpected call to setEnergy. The matrix energy container does not support directly setting the energy.");
    }

    @Override
    public FloatingLong insert(FloatingLong amount, Action action, AutomationType automationType) {
        if (amount.isEmpty() || tile.structure == null) {
            return amount;
        }
        FloatingLong toAdd = amount.min(getRemainingInput()).min(getNeeded());
        if (toAdd.isEmpty()) {
            //Exit if we don't actually have anything to add, either due to how much we need
            // or due to the our remaining rate limit
            return amount;
        }
        if (action.execute()) {
            //Increase how much we are inputting
            queuedInput.plusEqual(toAdd);
        }
        return amount.subtract(toAdd);
    }

    @Override
    public FloatingLong extract(FloatingLong amount, Action action, AutomationType automationType) {
        if (isEmpty() || amount.isEmpty() || tile.structure == null) {
            return FloatingLong.ZERO;
        }
        //We limit it overall by the amount we can extract plus how much energy we have
        // as we want to be as accurate as possible with the values we return
        // It is possible that the energy we have stored is a lot less than the amount we
        // can output at once such as if the matrix is almost empty.
        amount = amount.min(getRemainingOutput()).min(getEnergy());
        if (!amount.isEmpty() && action.execute()) {
            //Increase how much we are outputting by the amount we accepted
            queuedOutput.plusEqual(amount);
        }
        return amount;
    }

    @Override
    public FloatingLong getMaxEnergy() {
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

    private FloatingLong getRemainingInput() {
        return transferCap.subtract(queuedInput);
    }

    private FloatingLong getRemainingOutput() {
        return transferCap.subtract(queuedOutput);
    }

    public FloatingLong getMaxTransfer() {
        return transferCap;
    }

    public FloatingLong getLastInput() {
        return lastInput;
    }

    public FloatingLong getLastOutput() {
        return lastOutput;
    }

    public int getCells() {
        return cells.size();
    }

    public int getProviders() {
        return providers.size();
    }
}