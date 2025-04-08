package mekanism.common.attachments.containers.energy;

import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.attachments.containers.ComponentBackedContainer;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
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
    protected Long copy(Long toCopy) {
        return toCopy;
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
    public void setEnergy(long energy) {
        setContents(getAttached(), energy);
    }

    protected long clampEnergy(long energy) {
        return Math.min(energy, getMaxEnergy());
    }

    @Override
    protected void setContents(AttachedEnergy attachedEnergy, Long energy) {
        super.setContents(attachedEnergy, clampEnergy(energy));
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    protected long getInsertRate(@Nullable AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? Long.MAX_VALUE : rate.getAsLong();
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    protected long getExtractRate(@Nullable AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? Long.MAX_VALUE : rate.getAsLong();
    }

    @Override
    public long insert(long amount, Action action, AutomationType automationType) {
        if (amount <= 0L || !canInsert.test(automationType)) {
            return amount;
        }
        AttachedEnergy attachedEnergy = getAttached();
        long stored = getContents(attachedEnergy);
        long needed = Math.min(getInsertRate(automationType), getNeeded(stored));
        if (needed == 0L) {
            //Fail if we are a full container or our rate is zero
            return amount;
        }
        long toAdd = Math.min(amount, needed);
        if (action.execute()) {
            //If we want to actually insert the energy, then update the current energy
            // Note: this also will mark that the contents changed
            setContents(attachedEnergy, stored + toAdd);
        }
        return amount - toAdd;
    }

    @Override
    public long extract(long amount, Action action, AutomationType automationType) {
        if (amount <= 0) {
            return 0L;
        }
        AttachedEnergy attachedEnergy = getAttached();
        long stored = getContents(attachedEnergy);
        if (stored == 0L || !canExtract.test(automationType)) {
            return 0L;
        }
        long ret = Math.min(Math.min(getExtractRate(automationType), stored), amount);
        if (ret > 0L && action.execute()) {
            //Note: this also will mark that the contents changed
            setContents(attachedEnergy, stored - ret);
        }
        return ret;
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    protected long getNeeded(@Range(from = 0, to = Long.MAX_VALUE) long stored) {
        return getMaxEnergy() - stored;
    }

    @Override
    public long getMaxEnergy() {
        return maxEnergy.getAsLong();
    }

    @Override
    public CompoundTag serializeNBT(Provider provider) {
        CompoundTag nbt = new CompoundTag();
        long stored = getEnergy();
        if (stored > 0L) {
            nbt.putLong(SerializationConstants.STORED, stored);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(Provider provider, CompoundTag nbt) {
        NBTUtils.setLegacyEnergyIfPresent(nbt, SerializationConstants.STORED, this::setEnergy);
    }
}