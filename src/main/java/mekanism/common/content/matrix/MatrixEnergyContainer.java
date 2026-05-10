package mekanism.common.content.matrix;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Map;
import java.util.Set;
import mekanism.api.AutomationType;
import mekanism.api.MekanismPreconditions;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.MathUtils;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tile.multiblock.TileEntityInductionCell;
import mekanism.common.tile.multiblock.TileEntityInductionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class MatrixEnergyContainer implements IEnergyContainer {

    private final Map<BlockPos, InductionProviderTier> providers = new Object2ObjectOpenHashMap<>();
    private final Map<BlockPos, IEnergyContainer> cells = new Object2ObjectOpenHashMap<>();
    private final Set<BlockPos> invalidPositions = new ObjectOpenHashSet<>();
    private final QueuedEnergySnapshot queuedInput = new QueuedEnergySnapshot();
    private final QueuedEnergySnapshot queuedOutput = new QueuedEnergySnapshot();

    private long lastInput = 0L;
    private long lastOutput = 0L;

    private long cachedTotal = 0L;
    private long transferCap = 0L;
    private long storageCap = 0L;

    private final MatrixMultiblockData multiblock;

    public MatrixEnergyContainer(MatrixMultiblockData multiblock) {
        this.multiblock = multiblock;
    }

    public void addCell(BlockPos pos, TileEntityInductionCell cell) {
        //As we already have the two different variables just pass them instead of accessing world to get tile again
        MachineEnergyContainer<TileEntityInductionCell> energyContainer = cell.getEnergyContainer();
        cells.put(pos, energyContainer);
        storageCap = MathUtils.addClamped(storageCap, energyContainer.getMaxEnergy());
        cachedTotal = MathUtils.addClamped(cachedTotal, energyContainer.getEnergy());
    }

    public void addProvider(BlockPos pos, TileEntityInductionProvider provider) {
        providers.put(pos, provider.tier);
        transferCap = MathUtils.addClamped(transferCap, provider.tier.getOutput());
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
        queuedOutput.queued = 0L;
        queuedInput.queued = 0L;
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
        if (queuedInput.queued != queuedOutput.queued){
            try (Transaction transaction = Transaction.openRoot()) {
                if (queuedInput.queued < queuedOutput.queued) {
                    //queuedInput is smaller - we are removing energy
                    removeEnergy(-getQueuedChange(), transaction);
                } else {//if (queuedInput.queued > queuedOutput.queued)
                    //queuedInput is larger - we are adding energy
                    addEnergy(getQueuedChange(), transaction);
                }
                transaction.commit();
            }
        }
        lastInput = queuedInput.queued;
        lastOutput = queuedOutput.queued;
        queuedInput.queued = 0L;
        queuedOutput.queued = 0L;
    }

    private void addEnergy(long energy, TransactionContext transaction) {
        cachedTotal += energy;
        for (IEnergyContainer container : cells.values()) {
            //Note: inserting into the cell's energy container handles marking the cell for saving if it changes
            long inserted = container.insert(energy, transaction, AutomationType.INTERNAL);
            if (inserted > 0) {
                //Our cell accepted at least some energy
                energy -= inserted;
                if (energy == 0L) {
                    //Break if we don't have any energy left to add
                    break;
                }
            }
        }
    }

    private void removeEnergy(long energy, TransactionContext transaction) {
        cachedTotal -= energy;
        for (IEnergyContainer container : cells.values()) {
            //Note: extracting from the cell's energy container handles marking the cell for saving if it changes
            long extracted = container.extract(energy, transaction, AutomationType.INTERNAL);
            if (extracted > 0L) {
                energy -= extracted;
                if (energy == 0L) {
                    //Break if we don't need to remove any more energy
                    break;
                }
            }
        }
    }

    private long getQueuedChange() {
        return queuedInput.queued - queuedOutput.queued;
    }

    /**
     * @return The energy post queue when this container next actually updates/saves to disk
     */
    @Override
    public long getEnergy() {
        return cachedTotal + getQueuedChange();
    }

    @Override
    public void setEnergy(long energy) {
        //Throws a RuntimeException as specified is allowed when something unexpected happens
        // As setEnergy is more meant to be used as an internal method
        throw new RuntimeException("Unexpected call to setEnergy. The matrix energy container does not support directly setting the energy.");
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long insert(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType) {
        MekanismPreconditions.checkNonNegative(amount);
        if (amount == 0L || !multiblock.isFormed()) {
            return 0;
        }
        long toAdd = Math.min(Math.min(amount, getRemainingInput()), getNeeded());
        if (toAdd != 0L) {
            queuedInput.updateSnapshots(transaction);
            //Increase how much we are inputting
            queuedInput.queued += toAdd;

        }
        return toAdd;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long extract(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType) {
        MekanismPreconditions.checkNonNegative(amount);
        if (isEmpty() || amount == 0L || !multiblock.isFormed()) {
            return 0L;
        }
        //We limit it overall by the amount we can extract plus how much energy we have
        // as we want to be as accurate as possible with the values we return
        // It is possible that the energy we have stored is a lot less than the amount we
        // can output at once such as if the matrix is almost empty.
        amount = Math.min(Math.min(amount, getRemainingOutput()), getEnergy());
        if (amount > 0L) {
            //Increase how much we are outputting by the amount we accepted
            queuedOutput.updateSnapshots(transaction);
            //Increase how much we are inputting
            queuedOutput.queued += amount;
        }
        return amount;
    }

    @Override
    public long getMaxEnergy() {
        return storageCap;
    }

    @Override
    public boolean isValidForExtraction(AutomationType automationType) {
        return true;
    }

    @Override
    public boolean isValidForInsertion(AutomationType automationType) {
        return true;
    }

    @Override
    public void onContentsChanged() {
        //Unused
    }

    @Override
    public void serialize(ValueOutput output) {
        //Note: We don't actually have any specific serialization
    }

    @Override
    public void deserialize(ValueInput input) {
    }

    private long getRemainingInput() {
        return transferCap - queuedInput.queued;
    }

    private long getRemainingOutput() {
        return transferCap - queuedOutput.queued;
    }

    public long getMaxTransfer() {
        return transferCap;
    }

    public long getLastInput() {
        return lastInput;
    }

    public long getLastOutput() {
        return lastOutput;
    }

    public int getCells() {
        return cells.size();
    }

    public int getProviders() {
        return providers.size();
    }

    private static class QueuedEnergySnapshot extends SnapshotJournal<Long> {

        private long queued = 0L;

        @Override
        protected Long createSnapshot() {
            return queued;
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            queued = snapshot;
        }
    }
}