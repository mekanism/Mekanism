package mekanism.common.capabilities.energy;

import com.google.common.primitives.Ints;
import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismPreconditions;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.transaction.ITransactionHelper;
import mekanism.api.transaction.RateLimitTracker;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class BasicEnergyContainer extends SnapshotJournal<Long> implements IEnergyContainer {

    public static final Predicate<AutomationType> internalOnly = AutomationType::isInternal;
    public static final Predicate<AutomationType> manualOnly = AutomationType::isManual;
    public static final Predicate<AutomationType> notExternal = automationType -> !automationType.isExternal();

    public static BasicEnergyContainer create(long maxEnergy, @Nullable IContentsListener listener) {
        return create(maxEnergy, ConstantPredicates.alwaysTrue(), ConstantPredicates.alwaysTrue(), listener);
    }

    public static BasicEnergyContainer input(long maxEnergy, @Nullable IContentsListener listener) {
        return create(maxEnergy, notExternal, ConstantPredicates.alwaysTrue(), listener);
    }

    public static BasicEnergyContainer output(long maxEnergy, @Nullable IContentsListener listener) {
        return create(maxEnergy, ConstantPredicates.alwaysTrue(), internalOnly, listener);
    }

    public static BasicEnergyContainer create(long maxEnergy, Predicate<AutomationType> canExtract, Predicate<AutomationType> canInsert,
          @Nullable IContentsListener listener) {
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new BasicEnergyContainer(maxEnergy, canExtract, canInsert, null, null, listener);
    }

    private long stored = 0L;
    private final Predicate<AutomationType> canExtract;
    private final Predicate<AutomationType> canInsert;
    private final RateLimitTracker insertionRateLimiter;
    private final RateLimitTracker extractionRateLimiter;
    private final long maxEnergy;
    @Nullable
    private final IContentsListener listener;

    protected BasicEnergyContainer(long maxEnergy, Predicate<AutomationType> canExtract, Predicate<AutomationType> canInsert,
          @Nullable RateLimitTracker insertionRateLimiter, @Nullable RateLimitTracker extractionRateLimiter, @Nullable IContentsListener listener) {
        this.maxEnergy = maxEnergy;
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.insertionRateLimiter = ITransactionHelper.INSTANCE.orInfinite(insertionRateLimiter);
        this.extractionRateLimiter = ITransactionHelper.INSTANCE.orInfinite(extractionRateLimiter);
        this.listener = listener;
    }

    public void onContentsChanged(long originalState) {
        if (listener != null) {
            listener.onContentsChanged();
        }
    }

    @Override
    public final long getAmountAsLong() {
        return stored;
    }

    @Override
    public void setEnergy(@Range(from = 0, to = Long.MAX_VALUE) long energy, @Nullable TransactionContext transaction) {
        MekanismPreconditions.checkNonNegative(energy);
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

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        MekanismPreconditions.checkNonNegative(amount);
        if (amount == 0 || !isValidForInsertion(automationType)) {
            //"Fail quick" if nothing is being inserted, or we don't allow insertion for the given automation type
            return 0;
        }
        //Validate that we aren't at max stack size before we try to see if we can insert the resource, as on average this will be a cheaper check
        int needed = Ints.saturatedCast(getCapacityAsLong() - stored);
        int insertionRate = insertionRateLimiter.getRemainingLimit(automationType);
        //Limit how much we can add at once to the insertion rate the container sets
        needed = Math.min(needed, insertionRate);
        if (needed <= 0) {
            //Fail if we are a full slot, or we can never insert the resource or currently are unable to insert it
            return 0;
        }
        int toAdd = Math.min(amount, needed);
        //Note: We know toAdd is greater than zero, so we can just always call setEnergy
        setEnergy(stored + toAdd, transaction);
        insertionRateLimiter.consumeLimit(toAdd, automationType, transaction);
        return toAdd;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int extract(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        MekanismPreconditions.checkNonNegative(amount);
        if (isEmpty() || amount == 0 || !isValidForExtraction(automationType)) {
            //"Fail quick" if we are empty, nothing is being extracted, or if we can never extract from this slot
            return 0;
        }
        //If we are trying to extract more than we have, just change it so that we are extracting it all
        int toRemove = Math.min(amount, Ints.saturatedCast(stored));
        int extractionRate = extractionRateLimiter.getRemainingLimit(automationType);
        //Limit how much we can remove at once to the extraction rate the container sets
        toRemove = Math.min(toRemove, extractionRate);
        if (toRemove > 0) {
            setEnergy(stored - toRemove, transaction);
            extractionRateLimiter.consumeLimit(toRemove, automationType, transaction);
        }
        return toRemove;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getCapacityAsLong() {
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
        return stored;
    }

    @Override
    protected void revertToSnapshot(Long snapshot) {
        //Bypass contents change check
        stored = snapshot;
    }

    @Override
    protected void onRootCommit(Long originalState) {
        super.onRootCommit(originalState);
        if (stored != originalState) {
            //Fire content change listeners during root commit if the final state is different from the original one
            onContentsChanged(originalState);
        }
    }
}