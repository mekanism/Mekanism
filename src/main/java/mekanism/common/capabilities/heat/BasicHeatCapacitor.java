package mekanism.common.capabilities.heat;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.HeatPacket;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BasicHeatCapacitor implements IHeatCapacitor {

    @Nullable
    private final IMekanismHeatHandler heatHandler;

    private FloatingLong heatCapacity;

    private final FloatingLong inverseConductionCoefficient;
    private final FloatingLong inverseInsulationCoefficient;

    private boolean absorbHeat;
    private boolean emitHeat;

    protected FloatingLong storedHeat = FloatingLong.ZERO;
    @Nullable
    protected HeatPacket heatToHandle;

    public static BasicHeatCapacitor create(FloatingLong heatCapacity, @Nullable IMekanismHeatHandler heatHandler) {
        return create(heatCapacity, HeatAPI.DEFAULT_INVERSE_CONDUCTION, HeatAPI.DEFAULT_INVERSE_INSULATION, heatHandler);
    }

    // double helper
    public static BasicHeatCapacitor create(double heatCapacity, double inverseConductionCoefficient, double inverseInsulationCoefficient,
          @Nullable IMekanismHeatHandler heatHandler) {
        return create(FloatingLong.create(heatCapacity), FloatingLong.create(inverseConductionCoefficient), FloatingLong.create(inverseInsulationCoefficient), heatHandler);
    }

    public static BasicHeatCapacitor create(FloatingLong heatCapacity, FloatingLong inverseConductionCoefficient, FloatingLong inverseInsulationCoefficient,
          @Nullable IMekanismHeatHandler heatHandler) {
        return create(heatCapacity, inverseConductionCoefficient, inverseInsulationCoefficient, true, true, heatHandler);
    }

    public static BasicHeatCapacitor create(FloatingLong heatCapacity, FloatingLong inverseConductionCoefficient, FloatingLong inverseInsulationCoefficient,
          boolean absorbHeat, boolean emitHeat, @Nullable IMekanismHeatHandler heatHandler) {
        Objects.requireNonNull(heatCapacity, "Heat capacity cannot be null");
        if (heatCapacity.smallerThan(FloatingLong.ONE)) {
            throw new IllegalArgumentException("Heat capacity must be at least one");
        }
        Objects.requireNonNull(inverseConductionCoefficient, "Inverse conduction coefficient cannot be null");
        if (inverseConductionCoefficient.isZero()) {
            throw new IllegalArgumentException("Inverse conduction coefficient must be greater than zero");
        }
        Objects.requireNonNull(inverseInsulationCoefficient, "Inverse insulation coefficient cannot be null");
        return new BasicHeatCapacitor(heatCapacity, inverseConductionCoefficient, inverseInsulationCoefficient, absorbHeat, emitHeat, heatHandler);
    }

    protected BasicHeatCapacitor(FloatingLong heatCapacity, FloatingLong inverseConductionCoefficient, FloatingLong inverseInsulationCoefficient, boolean absorbHeat,
          boolean emitHeat, @Nullable IMekanismHeatHandler heatHandler) {
        this.heatCapacity = heatCapacity;
        this.inverseConductionCoefficient = inverseConductionCoefficient;
        this.inverseInsulationCoefficient = inverseInsulationCoefficient;
        this.absorbHeat = absorbHeat;
        this.emitHeat = emitHeat;
        this.heatHandler = heatHandler;

        Thread.dumpStack();
        System.out.println("Pre: " + storedHeat + " " + heatCapacity + " " + HeatAPI.AMBIENT_TEMP);
        // update the stored heat based on initial capacity
        storedHeat = heatCapacity.multiply(HeatAPI.AMBIENT_TEMP);
        System.out.println("Default heat: " + storedHeat + " " + heatCapacity + " " + HeatAPI.AMBIENT_TEMP);
        System.out.println(FloatingLong.ONE);
    }

    @Override
    public FloatingLong getTemperature() {
        return getHeat().divide(getHeatCapacity());
    }

    @Override
    public FloatingLong getInverseConduction() {
        return inverseConductionCoefficient;
    }

    @Override
    public FloatingLong getInverseInsulation() {
        return inverseInsulationCoefficient;
    }

    @Override
    public FloatingLong getHeatCapacity() {
        return heatCapacity;
    }

    @Override
    public void onContentsChanged() {
        if (heatHandler != null) {
            heatHandler.onContentsChanged();
        }
    }

    @Override
    public void handleHeat(HeatPacket transfer) {
        if (heatToHandle == null) {
            heatToHandle = transfer;
        } else {
            heatToHandle.merge(transfer);
        }
    }

    public void update() {
        if (heatToHandle != null) {
            if (heatToHandle.getType().absorb() && absorbHeat) {
                storedHeat = storedHeat.plusEqual(heatToHandle.getAmount());
            } else if (heatToHandle.getType().emit() && emitHeat) {
                storedHeat = storedHeat.minusEqual(heatToHandle.getAmount());
            }
            //notify listeners
            onContentsChanged();
        }
        // reset our heat
        heatToHandle = null;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        NBTUtils.setFloatingLongIfPresent(nbt, NBTConstants.STORED, heat -> storedHeat = heat);
        NBTUtils.setFloatingLongIfPresent(nbt, NBTConstants.HEAT_CAPACITY, capacity -> heatCapacity = capacity);
        System.out.println("READ CAPACITY " + heatCapacity);
        System.out.println("READ HEAT " + getHeat());
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString(NBTConstants.STORED, getHeat().toString());
        nbt.putString(NBTConstants.HEAT_CAPACITY, getHeatCapacity().toString());
        System.out.println("WRITE CAPACITY " + getHeatCapacity());
        System.out.println("WRITE HEAT " + getHeat());
        return nbt;
    }

    @Override
    public FloatingLong getHeat() {
        return storedHeat;
    }

    @Override
    public void setHeat(FloatingLong heat) {
        if (!storedHeat.equals(heat)) {
            storedHeat = heat;
            onContentsChanged();
        }
    }

    public void setHeatCapacity(FloatingLong newCapacity, boolean updateHeat) {
        if (updateHeat) {
            System.out.println("UPDATE " + heatCapacity + " " + newCapacity);
            setHeat(getHeat().add(newCapacity.subtract(getHeatCapacity()).multiply(HeatAPI.AMBIENT_TEMP)));
        }
        heatCapacity = newCapacity.copyAsConst();
    }
}
