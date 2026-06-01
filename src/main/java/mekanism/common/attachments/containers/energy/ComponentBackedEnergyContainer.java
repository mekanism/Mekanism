package mekanism.common.attachments.containers.energy;

import com.google.common.primitives.Ints;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.MekanismPreconditions;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.attachments.containers.SimpleComponentBackedContainer;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.attachments.containers.type.EnergyContainerType;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/// @implNote This container does not take the backing item access into account. None of the methods for interacting with this resource container scale the inputs based
/// on the backing item access' size.
@NothingNullByDefault
public class ComponentBackedEnergyContainer extends SimpleComponentBackedContainer<Long> implements IEnergyContainer {

    private final Predicate<AutomationType> canExtract;
    private final Predicate<AutomationType> canInsert;
    private final LongSupplier maxEnergy;
    private final IntSupplier rate;

    public ComponentBackedEnergyContainer(ItemAccess attachedAccess, Predicate<AutomationType> canExtract, Predicate<AutomationType> canInsert, IntSupplier rate,
          LongSupplier maxEnergy) {
        super(attachedAccess);
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
    protected EnergyContainerType containerType() {
        return ContainerType.ENERGY;
    }

    /**
     * @apiNote Try to minimize the number of calls to this method so that we don't have to look up the data component multiple times.
     */
    @Override
    public long getAmountAsLong() {
        return getAttached();
    }

    @Override
    public void setEnergy(@Range(from = 0, to = Long.MAX_VALUE) long energy, @Nullable TransactionContext transaction) {
        if (getAmountAsLong() != energy) {
            setContents(energy, transaction);
        }
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    protected int getInsertionRate(AutomationType automationType) {
        //Allow manual interaction to bypass rate limit for the item
        return automationType.isManual() ? Integer.MAX_VALUE : rate.getAsInt();
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    protected int getExtractionRate(AutomationType automationType) {
        //Allow manual interaction to bypass rate limit for the item
        return automationType.isManual() ? Integer.MAX_VALUE : rate.getAsInt();
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        MekanismPreconditions.checkNonNegative(amount);
        if (amount == 0 || !isValidForInsertion(automationType)) {
            //"Fail quick" if nothing is being inserted, or we don't allow insertion for the given automation type
            return 0;
        }
        long currentStored = getAmountAsLong();
        //Validate that we aren't at max stack size before we try to see if we can insert the resource, as on average this will be a cheaper check
        int needed = Ints.saturatedCast(getCapacityAsLong() - currentStored);
        //Limit how much we can add at once to the insertion rate the container sets
        needed = Math.min(needed, getInsertionRate(automationType));
        if (needed <= 0) {
            //Fail if we are a full slot, or we can never insert the resource or currently are unable to insert it
            return 0;
        }
        int toAdd = Math.min(amount, needed);
        // Note: We just set it as unchecked as we have already validated it
        if (setContents(currentStored + toAdd, transaction)) {
            return toAdd;
        }
        //If we couldn't update the backing item access, return that we didn't actually insert anything
        return 0;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int extract(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        MekanismPreconditions.checkNonNegative(amount);
        if (amount == 0 || !isValidForExtraction(automationType)) {
            //"Fail quick" nothing is being extracted, or if we can never extract from this slot
            return 0;
        }
        long currentStored = getAmountAsLong();
        if (currentStored == 0) {
            //"Fail quick" if we are empty
            return 0;
        }
        //If we are trying to extract more than we have, just change it so that we are extracting it all
        int toRemove = Math.min(amount, Ints.saturatedCast(currentStored));
        //Limit how much we can remove at once to the extraction rate the container sets
        toRemove = Math.min(toRemove, getExtractionRate(automationType));
        //Shrink the stack by the amount removed
        if (toRemove > 0 && setContents(currentStored - toRemove, transaction)) {
            return toRemove;
        }
        //If we couldn't update the backing item access, return that we didn't actually extract anything
        return 0;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getCapacityAsLong() {
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