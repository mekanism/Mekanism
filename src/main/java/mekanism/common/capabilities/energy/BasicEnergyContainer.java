package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class BasicEnergyContainer implements IEnergyContainer {

    public static final Predicate<@NonNull AutomationType> alwaysTrue = automationType -> true;
    public static final Predicate<@NonNull AutomationType> alwaysFalse = automationType -> false;
    public static final Predicate<@NonNull AutomationType> internalOnly = automationType -> automationType == AutomationType.INTERNAL;
    public static final Predicate<@NonNull AutomationType> notExternal = automationType -> automationType != AutomationType.EXTERNAL;

    public static BasicEnergyContainer create(double maxEnergy, @Nullable IMekanismStrictEnergyHandler fluidHandler) {
        if (maxEnergy < 0) {
            throw new IllegalArgumentException("Max energy must be at least zero");
        }
        return new BasicEnergyContainer(maxEnergy, alwaysTrue, alwaysTrue, fluidHandler);
    }

    public static BasicEnergyContainer input(double maxEnergy, @Nullable IMekanismStrictEnergyHandler energyHandler) {
        if (maxEnergy < 0) {
            throw new IllegalArgumentException("Max energy must be at least zero");
        }
        return new BasicEnergyContainer(maxEnergy, notExternal, alwaysTrue, energyHandler);
    }

    public static BasicEnergyContainer output(double maxEnergy, @Nullable IMekanismStrictEnergyHandler energyHandler) {
        if (maxEnergy < 0) {
            throw new IllegalArgumentException("Max energy must be at least zero");
        }
        return new BasicEnergyContainer(maxEnergy, alwaysTrue, internalOnly, energyHandler);
    }

    public static BasicEnergyContainer create(double maxEnergy, Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert,
          @Nullable IMekanismStrictEnergyHandler energyHandler) {
        if (maxEnergy < 0) {
            throw new IllegalArgumentException("Max energy must be at least zero");
        }
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        return new BasicEnergyContainer(maxEnergy, canExtract, canInsert, energyHandler);
    }

    private double stored = 0;
    protected final Predicate<@NonNull AutomationType> canExtract;
    protected final Predicate<@NonNull AutomationType> canInsert;
    private final double maxEnergy;
    @Nullable
    private final IMekanismStrictEnergyHandler energyHandler;

    protected BasicEnergyContainer(double maxEnergy, Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert,
          @Nullable IMekanismStrictEnergyHandler energyHandler) {
        this.maxEnergy = maxEnergy;
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

    @Override
    public double getEnergy() {
        return stored;
    }

    @Override
    public void setEnergy(double energy) {
        if (energy < 0) {
            //Throws a RuntimeException as specified is allowed when something unexpected happens
            // As setEnergy is more meant to be used as an internal method
            throw new RuntimeException("Negative energy for container: " + energy);
        } else if (stored != energy) {
            stored = energy;
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
     * @implNote By default this returns {@link Double#MAX_VALUE} so as to not actually limit the container's rate.
     * @apiNote By default this is ignored for direct setting of the stack/stack size
     */
    protected double getRate(@Nullable AutomationType automationType) {
        //TODO: Decide if we want to split this into a rate for inserting and a rate for extracting.
        return Double.MAX_VALUE;
    }

    @Override
    public double insert(double amount, Action action, AutomationType automationType) {
        if (amount <= 0 || !canInsert.test(automationType)) {
            //"Fail quick" if the given amount is empty (zero or negative) or we can never insert from that automation type
            return amount;
        }
        double needed = Math.min(getRate(automationType), getNeeded());
        if (needed <= 0) {
            //Fail if we are a full container or our rate is zero
            return amount;
        }
        double toAdd = Math.min(amount, needed);
        if (action.execute()) {
            //If we want to actually insert the energy, then update the current energy
            // Note: this also will mark that the contents changed
            setEnergy(getEnergy() + toAdd);
        }
        return amount - toAdd;
    }

    @Override
    public double extract(double amount, Action action, AutomationType automationType) {
        if (isEmpty() || amount <= 0 || !canExtract.test(automationType)) {
            //"Fail quick" if we don't can never extract from this slot, have an item stored, or the amount being requested is less than zero
            return 0;
        }
        double ret = Math.min(Math.min(getRate(automationType), getEnergy()), amount);
        if (ret > 0 && action.execute()) {
            //Note: this also will mark that the contents changed
            setEnergy(getEnergy() - ret);
        }
        return ret;
    }

    @Override
    public double getMaxEnergy() {
        return maxEnergy;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        NBTUtils.setDoubleIfPresent(nbt, NBTConstants.STORED, this::setEnergy);
    }
}