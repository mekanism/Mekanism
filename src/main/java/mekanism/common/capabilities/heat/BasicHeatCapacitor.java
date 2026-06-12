package mekanism.common.capabilities.heat;

import java.util.function.DoubleSupplier;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.lib.transaction.SimpleDoubleJournal;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class BasicHeatCapacitor extends SnapshotJournal<Double> implements IHeatCapacitor {

    @Nullable
    private final IContentsListener listener;

    private final SimpleDoubleJournal heatCapacity;

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
        this.heatCapacity = new SimpleDoubleJournal(heatCapacity);
        this.inverseConductionCoefficient = inverseConductionCoefficient;
        this.inverseInsulationCoefficient = inverseInsulationCoefficient;
        this.ambientTempSupplier = ambientTempSupplier;
        this.listener = listener;
    }

    private void initStoredHeat() {
        if (storedHeat == -1) {
            //If the stored heat hasn't been initialized yet, update the stored heat based on initial capacity
            storedHeat = Math.max(0D, getHeatCapacity() * getAmbientTemperature());
        }
    }

    protected double getAmbientTemperature() {
        return ambientTempSupplier == null ? HeatAPI.AMBIENT_TEMP : ambientTempSupplier.getAsDouble();
    }

    @Override
    public double getTemperature() {
        //TODO - 26.1 (heat): Do we want to define this as the default in IHeatCapacitor? Also should we validate the capacity is non-zero
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
    public final double getHeatCapacity() {
        return heatCapacity.value;
    }

    public void onContentsChanged(double originalState) {
        if (listener != null) {
            listener.onContentsChanged();
        }
    }

    @Override
    public void handleHeat(double transfer, TransactionContext transaction) {
        if (Math.abs(transfer) > HeatAPI.EPSILON) {
            initStoredHeat();
            updateSnapshots(transaction);
            storedHeat = Math.max(0D, storedHeat + transfer);
        }
    }

    @Override
    public boolean isAmbientTemperature() {
        return Mth.equal(getTemperature(), getAmbientTemperature());
    }

    @Override
    public void deserialize(ValueInput input) {
        //TODO - 26.1: Re-evaluate this. It is equivalent to super, except skips the contents change call
        storedHeat = Math.max(0D, input.getDoubleOr(SerializationConstants.STORED, storedHeat));
        heatCapacity.value = input.getDoubleOr(SerializationConstants.HEAT_CAPACITY, getHeatCapacity());
    }

    @Override
    public double getHeat() {
        initStoredHeat();
        return storedHeat;
    }

    @Override
    public void setHeat(double heat, @Nullable TransactionContext transaction) {
        //TODO - 26.1 (heat): Do we want to strictly deny values less than zero instead of just clamping to zero?
        heat = Math.max(0D, heat);
        double originalState = getHeat();
        if (!Mth.equal(heat, originalState)) {
            if (transaction == null) {
                storedHeat = heat;
                onContentsChanged(originalState);
            } else {
                updateSnapshots(transaction);
                storedHeat = heat;
            }
        }
    }

    public void updateHeatAndCapacity(double newCapacity, @Nullable TransactionContext transaction) {
        if (storedHeat == -1) {
            //If heat hasn't been initialized yet, just update the capacity
            setHeatCapacity(newCapacity, transaction);
        } else {
            //Otherwise calculate the value the heat should have, and also update the capacity
            setHeatAndCapacity(storedHeat + (newCapacity - getHeatCapacity()) * getAmbientTemperature(), newCapacity, transaction);
        }
    }

    @Override
    public void setHeatCapacity(double newCapacity, @Nullable TransactionContext transaction) {
        //TODO - 26.1 (heat): Sanitize heat capacity?
        if (transaction != null) {
            //If we are in a transactional context, update the snapshot of the heat capacity before updating the value
            heatCapacity.updateSnapshots(transaction);
        }
        heatCapacity.value = newCapacity;
        //TODO - 26.1 (heat): Should we be firing onContentsChanged?
    }

    @Override
    protected Double createSnapshot() {
        //TODO - 26.1: Should we force init the heat here? Or in updateSnapshots? Or just trust it is done correctly
        return storedHeat;
    }

    @Override
    protected void revertToSnapshot(Double snapshot) {
        //Bypass contents change check
        storedHeat = snapshot;
    }

    @Override
    protected void onRootCommit(Double originalState) {
        super.onRootCommit(originalState);
        //TODO - 26.1 (heat): Should this use Mth#equal? I suspect no? Then tiny changes would potentially never get saved,
        // as it compares against last value rather than last saved value and we are checking the Mth#equal in the setHeat method
        if (storedHeat != originalState) {
            //Fire content change listeners during root commit if the final state is different from the original one
            onContentsChanged(originalState);
        }
    }
}
