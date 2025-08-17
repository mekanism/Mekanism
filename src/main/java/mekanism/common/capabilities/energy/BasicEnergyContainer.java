package mekanism.common.capabilities.energy;

import com.google.common.base.Preconditions;
import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class BasicEnergyContainer implements IEnergyContainer {

    public static final Predicate<@NotNull AutomationType> internalOnly = automationType -> automationType == AutomationType.INTERNAL;
    public static final Predicate<@NotNull AutomationType> manualOnly = automationType -> automationType == AutomationType.MANUAL;
    public static final Predicate<@NotNull AutomationType> notExternal = automationType -> automationType != AutomationType.EXTERNAL;

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

    @Override
    public void onContentsChanged() {
        if (listener != null) {
            listener.onContentsChanged();
        }
    }

    @Override
    public long getEnergy() {
        return stored;
    }

    protected long clampEnergy(long energy) {
        return Math.min(energy, getMaxEnergy());
    }

    @Override
    public void setEnergy(long energy) {
        Preconditions.checkArgument(energy >= 0, "Energy cannot be negative");
        energy = clampEnergy(energy);
        if (stored != energy) {
            stored = energy;
            onContentsChanged();
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
    protected long getInsertRate(@Nullable AutomationType automationType) {
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
    protected long getExtractRate(@Nullable AutomationType automationType) {
        return Long.MAX_VALUE;
    }

    @Override
    public long insert(long amount, Action action, AutomationType automationType) {
        if (amount <= 0L || !canInsert.test(automationType)) {
            return amount;
        }
        long needed = Math.min(getInsertRate(automationType), getNeeded());
        if (needed == 0L) {
            //Fail if we are a full container or our rate is zero
            return amount;
        }
        long toAdd = Math.min(amount, needed);
        if (action.execute()) {
            //If we want to actually insert the energy, then update the current energy
            // Note: this also will mark that the contents changed
            stored += toAdd;
            onContentsChanged();
        }
        return amount - toAdd;
    }

    @Override
    public long extract(long amount, Action action, AutomationType automationType) {
        if (isEmpty() || amount <= 0L || !canExtract.test(automationType)) {
            return 0L;
        }
        long ret = Math.min(Math.min(getExtractRate(automationType), getEnergy()), amount);
        if (ret > 0L && action.execute()) {
            //Note: this also will mark that the contents changed
            stored -= ret;
            onContentsChanged();
        }
        return ret;
    }

    /**
     * @implNote Overwritten so that if we decide to change to returning a cached/copy of our value in {@link #getEnergy()}, we can optimize out the copying.
     */
    @Override
    public boolean isEmpty() {
        return stored == 0L;
    }

    @Override
    public long getMaxEnergy() {
        return maxEnergy;
    }

    /**
     * @implNote Overwritten so that if we decide to change to returning a cached/copy of our value in {@link #getEnergy()}, we can optimize out the copying.
     */
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        if (!isEmpty()) {
            nbt.putLong(SerializationConstants.STORED, stored);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        NBTUtils.setLegacyEnergyIfPresent(nbt, SerializationConstants.STORED, this::setEnergy);
    }
}