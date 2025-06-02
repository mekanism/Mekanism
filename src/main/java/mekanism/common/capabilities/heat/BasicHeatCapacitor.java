package mekanism.common.capabilities.heat;

import java.util.function.DoubleSupplier;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class BasicHeatCapacitor implements IHeatCapacitor {

    @Nullable
    private final IContentsListener listener;

    private double heatCapacity;

    @Nullable
    private final DoubleSupplier ambientTempSupplier;
    private final double inverseConductionCoefficient;
    private final double inverseInsulationCoefficient;

    // set to ambient * heat capacity by default
    private double storedHeat = -1;

    public static BasicHeatCapacitor create(double heatCapacity, @Nullable DoubleSupplier ambientTempSupplier, @Nullable IContentsListener listener) {
        return create(heatCapacity, HeatAPI.DEFAULT_INVERSE_CONDUCTION, HeatAPI.DEFAULT_INVERSE_INSULATION, ambientTempSupplier, listener);
    }

    public static BasicHeatCapacitor create(double heatCapacity, double inverseConductionCoefficient, double inverseInsulationCoefficient,
          @Nullable DoubleSupplier ambientTempSupplier, @Nullable IContentsListener listener) {
        if (heatCapacity < 1) {
            throw new IllegalArgumentException("Heat capacity must be at least one");
        }
        if (inverseConductionCoefficient < 1) {
            throw new IllegalArgumentException("Inverse conduction coefficient must be at least one");
        }
        return new BasicHeatCapacitor(heatCapacity, inverseConductionCoefficient, inverseInsulationCoefficient, ambientTempSupplier, listener);
    }

    protected BasicHeatCapacitor(double heatCapacity, double inverseConductionCoefficient, double inverseInsulationCoefficient,
          @Nullable DoubleSupplier ambientTempSupplier, @Nullable IContentsListener listener) {
        this.heatCapacity = heatCapacity;
        this.inverseConductionCoefficient = inverseConductionCoefficient;
        this.inverseInsulationCoefficient = inverseInsulationCoefficient;
        this.ambientTempSupplier = ambientTempSupplier;
        this.listener = listener;
    }

    private void initStoredHeat() {
        if (storedHeat == -1) {
            //If the stored heat hasn't been initialized yet, update the stored heat based on initial capacity
            storedHeat = Math.max(0D, heatCapacity * getAmbientTemperature());
        }
    }

    protected double getAmbientTemperature() {
        return ambientTempSupplier == null ? HeatAPI.AMBIENT_TEMP : ambientTempSupplier.getAsDouble();
    }

    @Override
    public double getTemperature() {
        return getHeat() / getHeatCapacity();
    }

    @Override
    public double getInverseConduction() {
        return inverseConductionCoefficient;
    }

    @Override
    public double getInverseInsulation() {
        return inverseInsulationCoefficient;
    }

    @Override
    public double getHeatCapacity() {
        return heatCapacity;
    }

    public void onContentsChanged(double originalState) {
        if (listener != null) {
            listener.onContentsChanged();
        }
    }

    @Override
    public void handleHeat(double transfer) {
        initStoredHeat();
        if (transfer != 0 && Math.abs(transfer) > HeatAPI.EPSILON) {
            double originalState = getHeat();
            storedHeat = Math.max(0D, storedHeat + transfer);
            onContentsChanged(originalState);
        }
    }

    @Override
    public boolean isAmbientTemperature() {
        return Mth.equal(getTemperature(), getAmbientTemperature());
    }

    @Override
    public void deserialize(ValueInput input) {
        storedHeat = Math.max(0D, input.getDoubleOr(SerializationConstants.STORED, storedHeat));
        setHeatCapacity(input.getDoubleOr(SerializationConstants.HEAT_CAPACITY, heatCapacity), false);
    }

    @Override
    public void serialize(ValueOutput output) {
        IHeatCapacitor.super.serialize(output);
        output.putDouble(SerializationConstants.HEAT_CAPACITY, getHeatCapacity());
    }

    @Override
    public double getHeat() {
        initStoredHeat();
        return storedHeat;
    }

    @Override
    public void setHeat(double heat) {
        heat = Math.max(0D, heat);
        double originalState = getHeat();
        if (!Mth.equal(heat, originalState)) {
            storedHeat = heat;
            onContentsChanged(originalState);
        }
    }

    @Override
    public void copyContents(IHeatCapacitor other, @Nullable TransactionContext transaction) {
        IHeatCapacitor.super.copyContents(other, transaction);
        //TODO - 26.1: Should heat capacity be copied before or after?
        setHeatCapacity(other.getHeatCapacity(), false);
    }

    public void setHeatCapacity(double newCapacity, boolean updateHeat) {
        if (updateHeat && storedHeat != -1) {
            setHeat(getHeat() + (newCapacity - getHeatCapacity()) * getAmbientTemperature());
        }
        heatCapacity = newCapacity;
    }

    public void setHeatCapacityFromPacket(double newCapacity) {
        heatCapacity = newCapacity;
    }
}
