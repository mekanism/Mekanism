package mekanism.common.capabilities.heat;

import java.util.function.DoubleSupplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
    private double heatToHandle;

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
            storedHeat = heatCapacity * getAmbientTemperature();
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

    @Override
    public void onContentsChanged() {
        if (listener != null) {
            listener.onContentsChanged();
        }
    }

    @Override
    public void handleHeat(double transfer) {
        heatToHandle += transfer;
    }

    public void update() {
        if (heatToHandle != 0) {
            initStoredHeat();
            storedHeat += heatToHandle;
            //notify listeners
            onContentsChanged();
            // reset our handling heat
            heatToHandle = 0;
        }
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        NBTUtils.setDoubleIfPresent(nbt, NBTConstants.STORED, heat -> storedHeat = heat);
        NBTUtils.setDoubleIfPresent(nbt, NBTConstants.HEAT_CAPACITY, capacity -> setHeatCapacity(capacity, false));
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putDouble(NBTConstants.STORED, getHeat());
        nbt.putDouble(NBTConstants.HEAT_CAPACITY, getHeatCapacity());
        return nbt;
    }

    @Override
    public double getHeat() {
        initStoredHeat();
        return storedHeat;
    }

    @Override
    public void setHeat(double heat) {
        if (getHeat() != heat) {
            storedHeat = heat;
            onContentsChanged();
        }
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
