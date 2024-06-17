package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.math.ULong;
import mekanism.api.math.Unsigned;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BasicEnergyContainer implements IEnergyContainer {

    public static final Predicate<@NotNull AutomationType> alwaysTrue = ConstantPredicates.alwaysTrue();
    public static final Predicate<@NotNull AutomationType> alwaysFalse = ConstantPredicates.alwaysFalse();
    public static final Predicate<@NotNull AutomationType> internalOnly = automationType -> automationType == AutomationType.INTERNAL;
    public static final Predicate<@NotNull AutomationType> manualOnly = automationType -> automationType == AutomationType.MANUAL;
    public static final Predicate<@NotNull AutomationType> notExternal = automationType -> automationType != AutomationType.EXTERNAL;

    public static BasicEnergyContainer create(@Unsigned long maxEnergy, @Nullable IContentsListener listener) {
        return new BasicEnergyContainer(maxEnergy, alwaysTrue, alwaysTrue, listener);
    }

    public static BasicEnergyContainer input(@Unsigned long maxEnergy, @Nullable IContentsListener listener) {
        return new BasicEnergyContainer(maxEnergy, notExternal, alwaysTrue, listener);
    }

    public static BasicEnergyContainer output(@Unsigned long maxEnergy, @Nullable IContentsListener listener) {
        return new BasicEnergyContainer(maxEnergy, alwaysTrue, internalOnly, listener);
    }

    public static BasicEnergyContainer create(@Unsigned long maxEnergy, Predicate<@NotNull AutomationType> canExtract, Predicate<@NotNull AutomationType> canInsert,
          @Nullable IContentsListener listener) {
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new BasicEnergyContainer(maxEnergy, canExtract, canInsert, listener);
    }

    private @Unsigned long stored = 0L;
    protected final Predicate<@NotNull AutomationType> canExtract;
    protected final Predicate<@NotNull AutomationType> canInsert;
    private final @Unsigned long maxEnergy;
    @Nullable
    private final IContentsListener listener;

    protected BasicEnergyContainer(@Unsigned long maxEnergy, Predicate<@NotNull AutomationType> canExtract, Predicate<@NotNull AutomationType> canInsert,
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
    public @Unsigned long getEnergy() {
        return stored;
    }

    protected long clampEnergy(long energy) {
        return Math.min(energy, getMaxEnergy());
    }

    @Override
    public void setEnergy(@Unsigned long energy) {
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
     * @implNote By default, this returns {@link ULong#MAX_VALUE} to not actually limit the container's rate. By default, this is also ignored for direct setting
     * of the stack/stack size
     */
    protected @Unsigned long getInsertRate(@Nullable AutomationType automationType) {
        return ULong.MAX_VALUE;
    }

    /**
     * Helper method to allow easily setting a rate at which energy can be extracted from this {@link BasicEnergyContainer}.
     *
     * @param automationType The automation type to limit the rate by or null if we don't have access to an automation type.
     *
     * @return The rate this tank can insert/extract at.
     *
     * @implNote By default, this returns {@link ULong#MAX_VALUE} to not actually limit the container's rate. By default, this is also ignored for direct setting
     * of the stack/stack size
     */
    protected @Unsigned long getExtractRate(@Nullable AutomationType automationType) {
        return ULong.MAX_VALUE;
    }

    @Override
    public @Unsigned long insert(@Unsigned long amount, Action action, AutomationType automationType) {
        if (amount == 0L || !canInsert.test(automationType)) {
            return amount;
        }
        long needed = ULong.min(getInsertRate(automationType), getNeeded());
        if (needed == 0L) {
            //Fail if we are a full container or our rate is zero
            return amount;
        }
        long toAdd = ULong.min(amount, needed);
        if (toAdd != 0 && action.execute()) {
            //If we want to actually insert the energy, then update the current energy
            // Note: this also will mark that the contents changed
            stored += toAdd;
            onContentsChanged();
        }
        return amount - toAdd;
    }

    @Override
    public @Unsigned long extract(@Unsigned long amount, Action action, AutomationType automationType) {
        if (isEmpty() || amount == 0L || !canExtract.test(automationType)) {
            return 0L;
        }
        long ret = ULong.min(getExtractRate(automationType), getEnergy(), amount);
        if (ret != 0L && action.execute()) {
            //Note: this also will mark that the contents changed
            stored -= ret;
            onContentsChanged();
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten so that if we decide to change to returning a cached/copy of our value in {@link #getEnergy()}, we can optimize out the copying.
     */
    @Override
    public boolean isEmpty() {
        return stored == 0L;
    }

    @Override
    public @Unsigned long getMaxEnergy() {
        return maxEnergy;
    }

    /**
     * {@inheritDoc}
     *
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
        NBTUtils.setFloatingLongIfPresent(nbt, SerializationConstants.STORED, v -> this.setEnergy(v.longValue()));//TODO 1.22 - backcompat
        NBTUtils.setLongIfPresent(nbt, SerializationConstants.STORED, this::setEnergy);
    }
}