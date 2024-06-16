package mekanism.common.attachments.containers.energy;

import com.google.common.primitives.UnsignedLongs;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.Unsigned;
import mekanism.common.attachments.containers.ComponentBackedContainer;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ComponentBackedEnergyContainer extends ComponentBackedContainer<Long, AttachedEnergy> implements IEnergyContainer {

    private final Predicate<@NotNull AutomationType> canExtract;
    private final Predicate<@NotNull AutomationType> canInsert;
    private final @Unsigned LongSupplier maxEnergy;
    private final @Unsigned LongSupplier rate;

    public ComponentBackedEnergyContainer(ItemStack attachedTo, int containerIndex, Predicate<@NotNull AutomationType> canExtract,
          Predicate<@NotNull AutomationType> canInsert, @Unsigned LongSupplier rate, @Unsigned LongSupplier maxEnergy) {
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
        return value == 0L;
    }

    @Override
    protected ContainerType<?, AttachedEnergy, ?> containerType() {
        return ContainerType.ENERGY;
    }

    /**
     * {@inheritDoc}
     *
     * @apiNote Try to minimize the number of calls to this method so that we don't have to look up the data component multiple times.
     */
    @Override
    public @Unsigned long getEnergy() {
        return getContents(getAttached());
    }

    @Override
    public void setEnergy(@Unsigned long energy) {
        setContents(getAttached(), energy);
    }

    @Override
    protected void setContents(AttachedEnergy attachedEnergy, Long energy) {
        super.setContents(attachedEnergy, UnsignedLongs.min(energy, getMaxEnergy()));
    }

    protected long getInsertRate(@Nullable AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? UnsignedLongs.MAX_VALUE : rate.getAsLong();
    }

    protected long getExtractRate(@Nullable AutomationType automationType) {
        //Allow unknown or manual interaction to bypass rate limit for the item
        return automationType == null || automationType == AutomationType.MANUAL ? UnsignedLongs.MAX_VALUE : rate.getAsLong();
    }

    @Override
    public @Unsigned long insert(@Unsigned long amount, Action action, AutomationType automationType) {
        if (amount == 0L || !canInsert.test(automationType)) {
            return amount;
        }
        AttachedEnergy attachedEnergy = getAttached();
        long stored = getContents(attachedEnergy);
        long needed = UnsignedLongs.min(getInsertRate(automationType), getNeeded(stored));
        if (needed == 0L) {
            //Fail if we are a full container or our rate is zero
            return amount;
        }
        long toAdd = UnsignedLongs.min(amount, needed);
        if (toAdd != 0L && action.execute()) {
            //If we want to actually insert the energy, then update the current energy
            // Note: this also will mark that the contents changed
            setContents(attachedEnergy, stored + toAdd);
        }
        return amount - toAdd;
    }

    @Override
    public @Unsigned long extract(@Unsigned long amount, Action action, AutomationType automationType) {
        if (amount == 0) {
            return 0L;
        }
        AttachedEnergy attachedEnergy = getAttached();
        long stored = getContents(attachedEnergy);
        if (stored == 0L || !canExtract.test(automationType)) {
            return 0L;
        }
        long ret = UnsignedLongs.min(getExtractRate(automationType), stored, amount);
        if (ret != 0L && action.execute()) {
            //Note: this also will mark that the contents changed
            setContents(attachedEnergy, stored - ret);
        }
        return ret;
    }

    protected long getNeeded(long stored) {
        return getMaxEnergy() - stored;
    }

    @Override
    public @Unsigned long getMaxEnergy() {
        return maxEnergy.getAsLong();
    }

    @Override
    public CompoundTag serializeNBT(Provider provider) {
        CompoundTag nbt = new CompoundTag();
        long stored = getEnergy();
        if (stored != 0L) {
            nbt.putLong(SerializationConstants.STORED, stored);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(Provider provider, CompoundTag nbt) {
        NBTUtils.setFloatingLongIfPresent(nbt, SerializationConstants.STORED, v -> this.setEnergy(v.longValue()));//TODO 1.22 - backcompat
        NBTUtils.setLongIfPresent(nbt, SerializationConstants.STORED, this::setEnergy);
    }
}