package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BasicEnergyContainer implements IEnergyContainer {

    public static final Predicate<@NonNull AutomationType> alwaysTrue = automationType -> true;
    public static final Predicate<@NonNull AutomationType> alwaysFalse = automationType -> false;
    public static final Predicate<@NonNull AutomationType> internalOnly = automationType -> automationType == AutomationType.INTERNAL;
    public static final Predicate<@NonNull AutomationType> notExternal = automationType -> automationType != AutomationType.EXTERNAL;

    public static BasicEnergyContainer create(FloatingLong maxEnergy, @Nullable IMekanismStrictEnergyHandler energyHandler) {
        Objects.requireNonNull(maxEnergy, "Max energy cannot be null");
        return new BasicEnergyContainer(maxEnergy, alwaysTrue, alwaysTrue, energyHandler);
    }

    public static BasicEnergyContainer input(FloatingLong maxEnergy, @Nullable IMekanismStrictEnergyHandler energyHandler) {
        Objects.requireNonNull(maxEnergy, "Max energy cannot be null");
        return new BasicEnergyContainer(maxEnergy, notExternal, alwaysTrue, energyHandler);
    }

    public static BasicEnergyContainer output(FloatingLong maxEnergy, @Nullable IMekanismStrictEnergyHandler energyHandler) {
        Objects.requireNonNull(maxEnergy, "Max energy cannot be null");
        return new BasicEnergyContainer(maxEnergy, alwaysTrue, internalOnly, energyHandler);
    }

    public static BasicEnergyContainer create(FloatingLong maxEnergy, Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert,
          @Nullable IMekanismStrictEnergyHandler energyHandler) {
        Objects.requireNonNull(maxEnergy, "Max energy cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new BasicEnergyContainer(maxEnergy, canExtract, canInsert, energyHandler);
    }

    private FloatingLong stored = FloatingLong.ZERO;
    protected final Predicate<@NonNull AutomationType> canExtract;
    protected final Predicate<@NonNull AutomationType> canInsert;
    private final FloatingLong maxEnergy;
    @Nullable
    private final IMekanismStrictEnergyHandler energyHandler;

    protected BasicEnergyContainer(FloatingLong maxEnergy, Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert,
          @Nullable IMekanismStrictEnergyHandler energyHandler) {
        this.maxEnergy = maxEnergy.copy();
        this.canExtract = canExtract;
        this.canInsert = canInsert;
        this.energyHandler = energyHandler;
    }

    @Override
    public void onContentsChanged() {
        if (energyHandler != null) {
            energyHandler.onContentsChanged();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @apiNote We return a cached value from this that if modified won't actually end up having any information about the container get changed.
     */
    @Override
    public FloatingLong getEnergy() {
        //TODO: Debate doing what the JavaDoc says. See BasicInventorySlot#getStack for details
        return stored;
    }

    @Override
    public void setEnergy(FloatingLong energy) {
        if (!stored.equals(energy)) {
            stored = energy.copy();
            onContentsChanged();
        }
    }

    /**
     * Helper method to allow easily setting a rate at which this {@link BasicEnergyContainer} can insert/extract energy.
     *
     * @param automationType The automation type to limit the rate by or null if we don't have access to an automation type.
     *
     * @return The rate this tank can insert/extract at.
     *
     * @implNote By default this returns {@link FloatingLong#MAX_VALUE} so as to not actually limit the container's rate. By default this is also ignored for direct
     * setting of the stack/stack size
     */
    protected FloatingLong getRate(@Nullable AutomationType automationType) {
        //TODO: Decide if we want to split this into a rate for inserting and a rate for extracting.
        return FloatingLong.MAX_VALUE;
    }

    @Override
    public FloatingLong insert(FloatingLong amount, Action action, AutomationType automationType) {
        if (amount.isZero() || !canInsert.test(automationType)) {
            return amount;
        }
        FloatingLong needed = getRate(automationType).min(getNeeded());
        if (needed.isZero()) {
            //Fail if we are a full container or our rate is zero
            return amount;
        }
        FloatingLong toAdd = amount.min(needed);
        if (!toAdd.isZero() && action.execute()) {
            //If we want to actually insert the energy, then update the current energy
            // Note: this also will mark that the contents changed
            stored = stored.plusEqual(toAdd);
            onContentsChanged();
        }
        return amount.subtract(toAdd);
    }

    @Override
    public FloatingLong extract(FloatingLong amount, Action action, AutomationType automationType) {
        if (isEmpty() || amount.isZero() || !canExtract.test(automationType)) {
            return FloatingLong.ZERO;
        }
        FloatingLong ret = getRate(automationType).min(getEnergy()).min(amount).copy();
        if (!ret.isZero() && action.execute()) {
            //Note: this also will mark that the contents changed
            stored = stored.minusEqual(ret);
            onContentsChanged();
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our amount in {@link #getEnergy()}, and we can optimize out the copying.
     */
    @Override
    public boolean isEmpty() {
        return stored.isZero();
    }

    @Override
    public FloatingLong getMaxEnergy() {
        return maxEnergy;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Overwritten as we return a cached/copy of our stack in {@link #getEnergy()}, and we can optimize out the copying.
     */
    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (!isEmpty()) {
            nbt.put(NBTConstants.STORED, stored.serializeNBT());
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        NBTUtils.setFloatingLongIfPresent(nbt, NBTConstants.STORED, this::setEnergy);
    }
}