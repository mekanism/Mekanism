package mekanism.common.capabilities.heat;

import java.util.function.DoubleSupplier;
import mekanism.api.IContentsListener;
import mekanism.api.MekanismPreconditions;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.component.containers.heat.HeatCapacitorData;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class BasicHeatCapacitor extends SnapshotJournal<HeatCapacitorData> implements IHeatCapacitor {

    @Nullable
    private final IContentsListener listener;

    @Nullable
    private final DoubleSupplier ambientTempSupplier;
    private final double inverseConductionCoefficient;
    private final double inverseInsulationCoefficient;

    // set to ambient * heat capacity by default
    private double storedHeat = -1;
    private double heatCapacity;

    public static BasicHeatCapacitor create(double heatCapacity, @Nullable DoubleSupplier ambientTempSupplier, @Nullable IContentsListener listener) {
        return create(heatCapacity, HeatAPI.DEFAULT_INVERSE_CONDUCTION, HeatAPI.DEFAULT_INVERSE_INSULATION, ambientTempSupplier, listener);
    }

    public static BasicHeatCapacitor create(double heatCapacity, double inverseConductionCoefficient, double inverseInsulationCoefficient,
          @Nullable DoubleSupplier ambientTempSupplier, @Nullable IContentsListener listener) {
        MekanismPreconditions.checkHeatCapacity(heatCapacity);
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

    protected double getAmbientTemperature() {
        return ambientTempSupplier == null ? HeatAPI.AMBIENT_TEMP : ambientTempSupplier.getAsDouble();
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
        return heatCapacity;
    }

    public void onContentsChanged(HeatCapacitorData originalState) {
        if (listener != null) {
            listener.onContentsChanged();
        }
    }

    @Override
    public void handleHeat(double transfer, TransactionContext transaction) {
        if (Math.abs(transfer) > HeatAPI.EPSILON) {
            updateSnapshots(transaction);
            storedHeat = Math.max(0D, getHeat() + transfer);
        }
    }

    @Override
    public boolean isAmbientTemperature() {
        return Mth.equal(getTemperature(), getAmbientTemperature());
    }

    private boolean isHeatInitialized() {
        return storedHeat != -1;
    }

    @Override
    public final double getHeat() {
        if (!isHeatInitialized()) {
            //If the stored heat hasn't been initialized yet, update the stored heat based on initial capacity
            storedHeat = Math.max(0D, getHeatCapacity() * getAmbientTemperature());
        }
        return storedHeat;
    }

    @Override
    public void setHeat(double heat, @Nullable TransactionContext transaction) {
        MekanismPreconditions.checkNonNegative(heat);
        if (transaction == null) {
            HeatCapacitorData originalState = createSnapshot();
            storedHeat = heat;
            //TODO - 26.2: do we need a way to avoid calling onContentsChange when loading from disk? I don't think we used to have one but it might be useful to have
            onContentsChanged(originalState);
        } else {
            updateSnapshots(transaction);
            storedHeat = heat;
        }
    }

    public void updateHeatAndCapacity(double newCapacity, @Nullable TransactionContext transaction) {
        if (isHeatInitialized()) {
            //If the heat has been initialized, calculate the value the heat should have, and also update the capacity
            try (Transaction subTransaction = Transaction.open(transaction)) {
                //Ensure there is a snapshot from before we call getHeat
                updateSnapshots(subTransaction);
                double capacityChange = newCapacity - getHeatCapacity();
                setHeatAndCapacity(Math.max(0D, getHeat() + capacityChange * getAmbientTemperature()), newCapacity, subTransaction);
                subTransaction.commit();
            }
        } else {
            //If heat hasn't been initialized yet, just update the capacity
            setHeatCapacity(newCapacity, transaction);
        }
    }

    @Override
    public void setHeatCapacity(double newCapacity, @Nullable TransactionContext transaction) {
        MekanismPreconditions.checkHeatCapacity(newCapacity);
        if (transaction == null) {
            HeatCapacitorData originalState = createSnapshot();
            heatCapacity = newCapacity;
            onContentsChanged(originalState);
        } else {
            //If we are in a transactional context, update the snapshot of the heat capacity before updating the value
            updateSnapshots(transaction);
            heatCapacity = newCapacity;
        }
    }

    @Override
    protected HeatCapacitorData createSnapshot() {
        if (isHeatInitialized()) {
            return new HeatCapacitorData(getHeat(), heatCapacity);
        }
        return new HeatCapacitorData(heatCapacity);
    }

    @Override
    protected void revertToSnapshot(HeatCapacitorData snapshot) {
        //Bypass contents change check
        storedHeat = snapshot.heat().orElse(-1);
        heatCapacity = snapshot.capacity();
    }

    @Override
    protected void onRootCommit(HeatCapacitorData originalState) {
        super.onRootCommit(originalState);
        //TODO - 26.2 (heat): Should this use Mth#equal? I suspect no? Then tiny changes would potentially never get saved,
        // as it compares against last value rather than last saved value and we are checking the Mth#equal in the setHeat method
        if (storedHeat != originalState.heat().orElse(-1) || !Mth.equal(heatCapacity, originalState.capacity())) {
            //Fire content change listeners during root commit if the final state is different from the original one
            onContentsChanged(originalState);
        }
    }
}