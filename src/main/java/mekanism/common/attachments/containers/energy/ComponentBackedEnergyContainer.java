package mekanism.common.attachments.containers.energy;

import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.MekanismPreconditions;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.attachments.containers.ComponentBackedContainer;
import mekanism.common.attachments.containers.ContainerType;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class ComponentBackedEnergyContainer extends ComponentBackedContainer<Long, AttachedEnergy> implements IEnergyContainer {

    private final Predicate<@NotNull AutomationType> canExtract;
    private final Predicate<@NotNull AutomationType> canInsert;
    private final LongSupplier maxEnergy;
    private final LongSupplier rate;

    public ComponentBackedEnergyContainer(ItemAccess attachedAccess, int containerIndex, Predicate<@NotNull AutomationType> canExtract,
          Predicate<@NotNull AutomationType> canInsert, LongSupplier rate, LongSupplier maxEnergy) {
        super(attachedAccess, containerIndex);
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.maxEnergy = maxEnergy;
        this.rate = rate;
    }

    @Override
    protected boolean isEmpty(Long value) {
        return value <= 0L;
    }

    @Override
    protected ContainerType<?, AttachedEnergy, ?> containerType() {
        return ContainerType.ENERGY;
    }

    /**
     * @apiNote Try to minimize the number of calls to this method so that we don't have to look up the data component multiple times.
     */
    @Override
    public long energy() {
        return getContents(getAttached());
    }

    @Override
    public void setEnergy(@Range(from = 0, to = Long.MAX_VALUE) long energy, @Nullable TransactionContext transaction) {
        setContents(getAttached(), energy, transaction, true);
    }

    protected long clampEnergy(long energy) {
        //TODO - 26.1: Re-evaluate clamping
        return Math.min(energy, capacity());
    }

    @Override
    protected boolean setContents(AttachedEnergy attachedEnergy, Long energy, @Nullable TransactionContext transaction, boolean checkChanged) {
        return super.setContents(attachedEnergy, clampEnergy(energy), transaction, checkChanged);
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    protected long getInsertionRate(AutomationType automationType) {
        //Allow manual interaction to bypass rate limit for the item
        return automationType.isManual() ? Long.MAX_VALUE : rate.getAsLong();
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    protected long getExtractionRate(AutomationType automationType) {
        //Allow manual interaction to bypass rate limit for the item
        return automationType.isManual() ? Long.MAX_VALUE : rate.getAsLong();
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long insert(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType) {
        MekanismPreconditions.checkNonNegative(amount);
        if (amount == 0 || !isValidForInsertion(automationType)) {
            //"Fail quick" if nothing is being inserted, or we don't allow insertion for the given automation type
            return 0;
        }
        AttachedEnergy attachedEnergy = getAttached();
        long currentStored = getContents(attachedEnergy);
        //Validate that we aren't at max stack size before we try to see if we can insert the resource, as on average this will be a cheaper check
        long needed = capacity() - currentStored;
        //Limit how much we can add at once to the insertion rate the container sets
        needed = Math.min(needed, getInsertionRate(automationType));
        if (needed <= 0) {
            //Fail if we are a full slot, or we can never insert the resource or currently are unable to insert it
            return 0;
        }
        long toAdd = Math.min(amount, needed);
        // Note: We just set it as unchecked as we have already validated it
        if (setContents(attachedEnergy, currentStored + toAdd, transaction, false)) {
            return toAdd;
        }
        //If we couldn't update the backing item access, return that we didn't actually insert anything
        return 0;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long extract(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType) {
        MekanismPreconditions.checkNonNegative(amount);
        if (amount == 0 || !isValidForExtraction(automationType)) {
            //"Fail quick" nothing is being extracted, or if we can never extract from this slot
            return 0;
        }
        AttachedEnergy attachedEnergy = getAttached();
        long currentStored = getContents(attachedEnergy);
        if (currentStored == 0) {
            //"Fail quick" if we are empty
            return 0;
        }
        //If we are trying to extract more than we have, just change it so that we are extracting it all
        long toRemove = Math.min(amount, currentStored);
        //Limit how much we can remove at once to the extraction rate the container sets
        toRemove = Math.min(toRemove, getExtractionRate(automationType));
        //Shrink the stack by the amount removed
        if (toRemove > 0 && setContents(attachedEnergy, currentStored - toRemove, transaction, false)) {
            return toRemove;
        }
        //If we couldn't update the backing item access, return that we didn't actually extract anything
        return 0;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long capacity() {
        return maxEnergy.getAsLong();
    }

    @Override
    public boolean isValidForExtraction(AutomationType automationType) {
        return canExtract.test(automationType);
    }

    @Override
    public boolean isValidForInsertion(AutomationType automationType) {
        return canInsert.test(automationType);
    }
}