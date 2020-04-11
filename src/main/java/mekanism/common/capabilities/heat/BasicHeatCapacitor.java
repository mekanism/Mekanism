package mekanism.common.capabilities.heat;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BasicHeatCapacitor implements IHeatCapacitor {

    @Nullable
    private final IMekanismHeatHandler heatHandler;

    private double heatCapacity;

    private final double inverseConductionCoefficient;
    private final double inverseInsulationCoefficient;

    // set to ambient * heat capacity by default
    protected double storedHeat;
    protected double heatToHandle;

    public static BasicHeatCapacitor create(double heatCapacity, @Nullable IMekanismHeatHandler heatHandler) {
        return create(heatCapacity, HeatAPI.DEFAULT_INVERSE_CONDUCTION, HeatAPI.DEFAULT_INVERSE_INSULATION, heatHandler);
    }

    public static BasicHeatCapacitor create(double heatCapacity, double inverseConductionCoefficient, double inverseInsulationCoefficient,
          @Nullable IMekanismHeatHandler heatHandler) {
        if (heatCapacity < 1) {
            throw new IllegalArgumentException("Heat capacity must be at least one");
        }
        if (inverseConductionCoefficient < 1) {
            throw new IllegalArgumentException("Inverse conduction coefficient must be at least one");
        }
        return new BasicHeatCapacitor(heatCapacity, inverseConductionCoefficient, inverseInsulationCoefficient, heatHandler);
    }

    protected BasicHeatCapacitor(double heatCapacity, double inverseConductionCoefficient, double inverseInsulationCoefficient,
          @Nullable IMekanismHeatHandler heatHandler) {
        this.heatCapacity = heatCapacity;
        this.inverseConductionCoefficient = inverseConductionCoefficient;
        this.inverseInsulationCoefficient = inverseInsulationCoefficient;
        this.heatHandler = heatHandler;

        // update the stored heat based on initial capacity
        storedHeat = heatCapacity * HeatAPI.AMBIENT_TEMP;
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
        if (heatHandler != null) {
            heatHandler.onContentsChanged();
        }
    }

    @Override
    public void handleHeat(double transfer) {
        heatToHandle += transfer;
    }

    public void update() {
        if (heatToHandle != 0) {
            storedHeat += heatToHandle;
            //notify listeners
            onContentsChanged();
        }
        // reset our handling heat
        heatToHandle = 0;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        NBTUtils.setDoubleIfPresent(nbt, NBTConstants.STORED, heat -> storedHeat = heat);
        NBTUtils.setDoubleIfPresent(nbt, NBTConstants.HEAT_CAPACITY, capacity -> heatCapacity = capacity);
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
        return storedHeat;
    }

    @Override
    public void setHeat(double heat) {
        if (storedHeat != heat) {
            storedHeat = heat;
            onContentsChanged();
        }
    }

    public void setHeatCapacity(double newCapacity, boolean updateHeat) {
        if (updateHeat) {
            setHeat(getHeat() + (newCapacity - getHeatCapacity()) * HeatAPI.AMBIENT_TEMP);
        }
        heatCapacity = newCapacity;
    }
}
