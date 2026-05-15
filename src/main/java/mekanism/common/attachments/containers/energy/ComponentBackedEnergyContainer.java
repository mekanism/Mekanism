package mekanism.common.attachments.containers.energy;

import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.MekanismPreconditions;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.attachments.containers.ComponentBackedContainer;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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

    public ComponentBackedEnergyContainer(ItemStack attachedTo, int containerIndex, Predicate<@NotNull AutomationType> canExtract,
          Predicate<@NotNull AutomationType> canInsert, LongSupplier rate, LongSupplier maxEnergy) {
        super(attachedTo, containerIndex);
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
    public long getEnergy() {
        return getContents(getAttached());
    }

    @Override
    public void setEnergy(@Range(from = 0, to = Long.MAX_VALUE) long energy) {
        setContents(getAttached(), energy);
    }

    protected long clampEnergy(long energy) {
        //TODO - 26.1: Re-evaluate clamping
        return Math.min(energy, getCapacity());
    }

    @Override
    protected void setContents(AttachedEnergy attachedEnergy, Long energy) {
        super.setContents(attachedEnergy, clampEnergy(energy));
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    protected long getInsertionRate(@Nullable AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? Long.MAX_VALUE : rate.getAsLong();
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    protected long getExtractionRate(@Nullable AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? Long.MAX_VALUE : rate.getAsLong();
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
        long needed = getCapacity() - currentStored;
        //Limit how much we can add at once to the insertion rate the container sets
        needed = Math.min(needed, getInsertionRate(automationType));
        if (needed <= 0) {
            //Fail if we are a full slot, or we can never insert the resource or currently are unable to insert it
            return 0;
        }
        long toAdd = Math.min(amount, needed);
        updateSnapshots(transaction);
        // Note: We just set it as unchecked as we have already validated it
        setContents(attachedEnergy, currentStored + toAdd);
        return toAdd;
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
        if (toRemove > 0) {
            updateSnapshots(transaction);
            //Shrink the stack by the amount removed
            setContents(attachedEnergy, currentStored - toRemove);
        }
        return toRemove;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getCapacity() {
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

    @Override
    public void serialize(ValueOutput output) {
        long stored = getEnergy();
        if (stored > 0L) {
            output.putLong(SerializationConstants.STORED, stored);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        input.getLong(SerializationConstants.STORED).ifPresent(this::setEnergy);
    }
}