package mekanism.common.content.matrix;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Map;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.ULong;
import mekanism.api.math.Unsigned;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tile.multiblock.TileEntityInductionCell;
import mekanism.common.tile.multiblock.TileEntityInductionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

@NothingNullByDefault
public class MatrixEnergyContainer implements IEnergyContainer {

    private final Map<BlockPos, InductionProviderTier> providers = new Object2ObjectOpenHashMap<>();
    private final Map<BlockPos, IEnergyContainer> cells = new Object2ObjectOpenHashMap<>();
    private final Set<BlockPos> invalidPositions = new ObjectOpenHashSet<>();

    //TODO: Eventually we could look into extending FloatingLong to have a "BigInt" styled implementation that is used by the class
    // at the very least for keeping track of the cached values and rates
    private @Unsigned long queuedOutput = 0L;
    private @Unsigned long queuedInput = 0L;
    private @Unsigned long lastOutput = 0L;
    private @Unsigned long lastInput = 0L;

    private @Unsigned long cachedTotal = 0L;
    private @Unsigned long transferCap = 0L;
    private @Unsigned long storageCap = 0L;

    private final MatrixMultiblockData multiblock;

    public MatrixEnergyContainer(MatrixMultiblockData multiblock) {
        this.multiblock = multiblock;
    }

    public void addCell(BlockPos pos, TileEntityInductionCell cell) {
        //As we already have the two different variables just pass them instead of accessing world to get tile again
        MachineEnergyContainer<TileEntityInductionCell> energyContainer = cell.getEnergyContainer();
        cells.put(pos, energyContainer);
        storageCap = storageCap + (energyContainer.getMaxEnergy());
        cachedTotal = cachedTotal + (energyContainer.getEnergy());
    }

    public void addProvider(BlockPos pos, TileEntityInductionProvider provider) {
        providers.put(pos, provider.tier);
        transferCap = transferCap + (provider.tier.getOutput());
    }

    //TODO: I believe this is needed or at least will be after we eventually rewrite some of the multiblock system
    // currently I think it may just be rechecking the entire structure when something changes internally
    // We need to validate that does properly happen even if the cell is floating in the middle and not touching any walls
    // We may also want to make cells and providers extend TileEntityInternalMultiblock
    public void removeInternal(BlockPos pos) {
        if (invalidPositions.add(pos)) {
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
        }
    }

    public void invalidate() {
        //Force save
        tick();
        //And reset everything
        cells.clear();
        providers.clear();
        queuedOutput = 0L;
        queuedInput = 0L;
        lastOutput = 0L;
        lastInput = 0L;
        cachedTotal = 0L;
        transferCap = 0L;
        storageCap = 0L;
    }

    public void tick() {
        if (!invalidPositions.isEmpty()) {
            for (BlockPos invalidPosition : invalidPositions) {
                cells.remove(invalidPosition);
                providers.remove(invalidPosition);
            }
            invalidPositions.clear();
        }
        int compare = Long.compareUnsigned(queuedInput, queuedOutput);
        if (compare < 0) {
            //queuedInput is smaller - we are removing energy
            removeEnergy(queuedOutput - queuedInput);
        } else if (compare > 0) {
            //queuedInput is larger - we are adding energy
            addEnergy(queuedInput - queuedOutput);
        }
        lastInput = queuedInput;
        lastOutput = queuedOutput;
        queuedInput = 0L;
        queuedOutput = 0L;
    }

    private void addEnergy(@Unsigned long energy) {
        cachedTotal += energy;
        for (IEnergyContainer container : cells.values()) {
            //Note: inserting into the cell's energy container handles marking the cell for saving if it changes
            @Unsigned long remainder = container.insert(energy, Action.EXECUTE, AutomationType.INTERNAL);
            if (ULong.lt(remainder, energy)) {
                //Our cell accepted at least some energy
                if (remainder == 0L) {
                    //Check less than equal rather than just equal in case something went wrong
                    // and break if we don't have any energy left to add
                    break;
                }
                energy = remainder;
            }
        }
    }

    private void removeEnergy(@Unsigned long energy) {
        cachedTotal -= energy;
        for (IEnergyContainer container : cells.values()) {
            //Note: extracting from the cell's energy container handles marking the cell for saving if it changes
            @Unsigned long extracted = container.extract(energy, Action.EXECUTE, AutomationType.INTERNAL);
            if (extracted != 0L) {
                energy -= extracted;
                if (energy == 0L) {
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
    public @Unsigned long getEnergy() {
        return cachedTotal + queuedInput - queuedOutput;
    }

    @Override
    public void setEnergy(@Unsigned long energy) {
        //Throws a RuntimeException as specified is allowed when something unexpected happens
        // As setEnergy is more meant to be used as an internal method
        throw new RuntimeException("Unexpected call to setEnergy. The matrix energy container does not support directly setting the energy.");
    }

    @Override
    public @Unsigned long insert(@Unsigned long amount, Action action, AutomationType automationType) {
        if (amount == 0L || !multiblock.isFormed()) {
            return amount;
        }
        @Unsigned long toAdd = ULong.min(amount, getRemainingInput(), getNeeded());
        if (toAdd == 0L) {
            //Exit if we don't actually have anything to add, either due to how much we need
            // or due to the remaining rate limit
            return amount;
        }
        if (action.execute()) {
            //Increase how much we are inputting
            queuedInput += toAdd;
        }
        return amount - toAdd;
    }

    @Override
    public @Unsigned long extract(@Unsigned long amount, Action action, AutomationType automationType) {
        if (isEmpty() || amount == 0L || !multiblock.isFormed()) {
            return 0L;
        }
        //We limit it overall by the amount we can extract plus how much energy we have
        // as we want to be as accurate as possible with the values we return
        // It is possible that the energy we have stored is a lot less than the amount we
        // can output at once such as if the matrix is almost empty.
        amount = ULong.min(amount, getRemainingOutput(), getEnergy());
        if (amount != 0L && action.execute()) {
            //Increase how much we are outputting by the amount we accepted
            queuedOutput = queuedOutput.plusEqual(amount);
        }
        return amount;
    }

    @Override
    public @Unsigned long getMaxEnergy() {
        return storageCap;
    }

    @Override
    public void onContentsChanged() {
        //Unused
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        //Note: We don't actually have any specific serialization
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {

    }

    private @Unsigned long getRemainingInput() {
        return transferCap.subtract(queuedInput);
    }

    private @Unsigned long getRemainingOutput() {
        return transferCap.subtract(queuedOutput);
    }

    public @Unsigned long getMaxTransfer() {
        return transferCap;
    }

    public @Unsigned long getLastInput() {
        return lastInput;
    }

    public @Unsigned long getLastOutput() {
        return lastOutput;
    }

    public int getCells() {
        return cells.size();
    }

    public int getProviders() {
        return providers.size();
    }
}