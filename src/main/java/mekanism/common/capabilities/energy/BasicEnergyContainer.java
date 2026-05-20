package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismPreconditions;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class BasicEnergyContainer extends SnapshotJournal<Long> implements IEnergyContainer {

    public static final Predicate<@NotNull AutomationType> internalOnly = AutomationType::isInternal;
    public static final Predicate<@NotNull AutomationType> manualOnly = AutomationType::isManual;
    public static final Predicate<@NotNull AutomationType> notExternal = automationType -> !automationType.isExternal();

    public static BasicEnergyContainer create(long maxEnergy, @Nullable IContentsListener listener) {
        return new BasicEnergyContainer(maxEnergy, ConstantPredicates.alwaysTrue(), ConstantPredicates.alwaysTrue(), listener);
    }

    public static BasicEnergyContainer input(long maxEnergy, @Nullable IContentsListener listener) {
        return new BasicEnergyContainer(maxEnergy, notExternal, ConstantPredicates.alwaysTrue(), listener);
    }

    public static BasicEnergyContainer output(long maxEnergy, @Nullable IContentsListener listener) {
        return new BasicEnergyContainer(maxEnergy, ConstantPredicates.alwaysTrue(), internalOnly, listener);
    }

    public static BasicEnergyContainer create(long maxEnergy, Predicate<@NotNull AutomationType> canExtract, Predicate<@NotNull AutomationType> canInsert,
          @Nullable IContentsListener listener) {
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new BasicEnergyContainer(maxEnergy, canExtract, canInsert, listener);
    }

    private long stored = 0L;
    protected final Predicate<@NotNull AutomationType> canExtract;
    protected final Predicate<@NotNull AutomationType> canInsert;
    private final long maxEnergy;
    @Nullable
    private final IContentsListener listener;

    protected BasicEnergyContainer(long maxEnergy, Predicate<@NotNull AutomationType> canExtract, Predicate<@NotNull AutomationType> canInsert,
          @Nullable IContentsListener listener) {
        this.maxEnergy = maxEnergy;
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.listener = listener;
    }

    public void onContentsChanged(long originalState) {
        if (listener != null) {
            listener.onContentsChanged();
        }
    }

    @Override
    public long energy() {
        return stored;
    }

    protected long clampEnergy(long energy) {
        return Math.min(energy, capacity());
    }

    @Override
    public void setEnergy(@Range(from = 0, to = Long.MAX_VALUE) long energy, @Nullable TransactionContext transaction) {
        MekanismPreconditions.checkNonNegative(energy);
        //TODO - 26.1: Re-evaluate this clamping and maybe get rid of it or move it?
        energy = clampEnergy(energy);
        if (stored != energy) {
            if (transaction == null) {
                long originalState = stored;
                stored = energy;
                onContentsChanged(originalState);
            } else {
                updateSnapshots(transaction);
                stored = energy;
            }
        }
    }

    /**
     * Helper method to allow easily setting a rate at which energy can be inserted into this {@link BasicEnergyContainer}.
     *
     * @param automationType The automation type to limit the rate by or null if we don't have access to an automation type.
     *
     * @return The rate this tank can insert/extract at.
     *
     * @implNote By default, this returns {@link Long#MAX_VALUE} to not actually limit the container's rate. By default, this is also ignored for direct setting of the
     * stack/stack size
     */
    @Range(from = 0, to = Long.MAX_VALUE)
    protected long getInsertionRate(AutomationType automationType) {
        return Long.MAX_VALUE;
    }

    /**
     * Helper method to allow easily setting a rate at which energy can be extracted from this {@link BasicEnergyContainer}.
     *
     * @param automationType The automation type to limit the rate by or null if we don't have access to an automation type.
     *
     * @return The rate this tank can insert/extract at.
     *
     * @implNote By default, this returns {@link Long#MAX_VALUE} to not actually limit the container's rate. By default, this is also ignored for direct setting of the
     * stack/stack size
     */
    @Range(from = 0, to = Long.MAX_VALUE)
    protected long getExtractionRate(AutomationType automationType) {
        return Long.MAX_VALUE;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long insert(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType) {
        MekanismPreconditions.checkNonNegative(amount);
        if (amount == 0 || !isValidForInsertion(automationType)) {
            //"Fail quick" if nothing is being inserted, or we don't allow insertion for the given automation type
            return 0;
        }
        long currentStored = energy();
        //Validate that we aren't at max stack size before we try to see if we can insert the resource, as on average this will be a cheaper check
        long needed = capacity() - currentStored;
        //Limit how much we can add at once to the insertion rate the container sets
        needed = Math.min(needed, getInsertionRate(automationType));
        if (needed <= 0) {
            //Fail if we are a full slot, or we can never insert the resource or currently are unable to insert it
            return 0;
        }
        long toAdd = Math.min(amount, needed);
        setEnergy(currentStored + toAdd, transaction);
        return toAdd;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long extract(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType) {
        MekanismPreconditions.checkNonNegative(amount);
        if (isEmpty() || amount == 0 || !isValidForExtraction(automationType)) {
            //"Fail quick" if we are empty, nothing is being extracted, or if we can never extract from this slot
            return 0;
        }
        long currentStored = energy();
        //If we are trying to extract more than we have, just change it so that we are extracting it all
        long toRemove = Math.min(amount, currentStored);
        //Limit how much we can remove at once to the extraction rate the container sets
        toRemove = Math.min(toRemove, getExtractionRate(automationType));
        if (toRemove > 0) {
            //Shrink the stack by the amount removed
            setEnergy(currentStored - toRemove, transaction);
        }
        return toRemove;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long capacity() {
        return maxEnergy;
    }

    @Override
    public final boolean isValidForExtraction(AutomationType automationType) {
        return canExtract.test(automationType);
    }

    @Override
    public final boolean isValidForInsertion(AutomationType automationType) {
        return canInsert.test(automationType);
    }

    @Override
    protected Long createSnapshot() {
        return energy();
    }

    @Override
    protected void revertToSnapshot(Long snapshot) {
        //Bypass contents change check
        stored = snapshot;
    }

    @Override
    protected void onRootCommit(Long originalState) {
        super.onRootCommit(originalState);
        if (energy() != originalState) {
            //Fire content change listeners during root commit if the final state is different from the original one
            onContentsChanged(originalState);
        }
    }
}