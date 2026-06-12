package mekanism.api.heat;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/// Helper class to simplify wrapping a heat capacitor while allowing providing overrides for specific methods.
///
/// @since 10.8.0
public abstract class HeatCapacitorWrapper implements IHeatCapacitor {

    private final IHeatCapacitor internal;

    protected HeatCapacitorWrapper(IHeatCapacitor internal) {
        this.internal = internal;
    }

    /// This method gets the innermost resource container for use in [copyContents][IHeatCapacitor#copyContents(IHeatCapacitor, TransactionContext)] when instance
    /// checks are required.
    public IHeatCapacitor getInternal() {
        IHeatCapacitor internal = this.internal;
        if (internal instanceof HeatCapacitorWrapper wrapper) {
            //For cases like valve fluid wrappers that are wrapping a merged tank
            // We want to return the actual source container
            return wrapper.getInternal();
        }
        return internal;
    }

    @Override
    public double getTemperature() {
        return internal.getTemperature();
    }

    @Override
    public double getInverseConduction() {
        return internal.getInverseConduction();
    }

    @Override
    public double getInverseInsulation() {
        return internal.getInverseInsulation();
    }

    @Override
    public double getHeatCapacity() {
        return internal.getHeatCapacity();
    }

    @Override
    public double getHeat() {
        return internal.getHeat();
    }

    @Override
    public void setHeat(double heat) {
        internal.setHeat(heat);
    }

    @Override
    public void handleHeat(double transfer) {
        internal.handleHeat(transfer);
    }

    @Override
    public boolean isAmbientTemperature() {
        return internal.isAmbientTemperature();
    }

    @Override
    public void serialize(ValueOutput output) {
        internal.serialize(output);
    }

    @Override
    public void deserialize(ValueInput input) {
        internal.deserialize(input);
    }

    @Override
    public void copyContents(IHeatCapacitor other, @Nullable TransactionContext transaction) {
        internal.copyContents(other, transaction);
    }
}