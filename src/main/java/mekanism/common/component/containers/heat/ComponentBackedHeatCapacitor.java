package mekanism.common.component.containers.heat;

import java.util.Objects;
import java.util.function.UnaryOperator;
import mekanism.api.SerializationConstants;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.HeatCapacitorWrapper;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.component.containers.SimpleComponentBackedContainer;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.HeatContainerType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class ComponentBackedHeatCapacitor extends SimpleComponentBackedContainer<HeatCapacitorData> implements IHeatCapacitor {

    private final double inverseConductionCoefficient;
    private final double inverseInsulationCoefficient;
    private final HeatCapacitorData defaultData;

    public ComponentBackedHeatCapacitor(ItemAccess attachedAccess, double inverseConductionCoefficient, double inverseInsulationCoefficient) {
        super(attachedAccess);
        this.inverseConductionCoefficient = inverseConductionCoefficient;
        this.inverseInsulationCoefficient = inverseInsulationCoefficient;
        //TODO - 26.1 (heat): Re-evaluate throwing like this? Can we somehow handle it more gracefully?
        // Theoretically we could keep requiring passing in the default capacity
        this.defaultData = Objects.requireNonNull(attachedAccess.getResource().getItem().components().get(containerType().getComponentType()),
              "Attempted to create a component backed heat capacitor for an item that doesn't support heat data");
    }

    @Override
    protected HeatContainerType containerType() {
        return ContainerType.HEAT;
    }

    @Nullable
    protected HeatCapacitorData getCurrent() {
        return containerType().get(attachedAccess.getResource());
    }

    protected HeatCapacitorData getCurrentOrDefault() {
        return Objects.requireNonNullElse(getCurrent(), defaultData);
    }

    @Override
    public double getTemperature() {
        return getCurrentOrDefault().temperature();
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
        return getCurrentOrDefault().capacity();
    }

    @Override
    public double getHeat() {
        return getCurrentOrDefault().heatOrAmbient();
    }

    //Note: While callers create capturing lambda's, as these methods are not really being used in item form anyway, it shouldn't have that big a performance impact
    private void updateContents(UnaryOperator<HeatCapacitorData> transformer, @Nullable TransactionContext transaction) {
        HeatCapacitorData current = getCurrent();
        HeatCapacitorData existing = Objects.requireNonNullElse(current, defaultData);
        //Note: withHeat handles clamping to zero
        HeatCapacitorData newData = transformer.apply(existing);
        if (current != newData) {
            //Note: we can just check instance equality, because if the heat value is the same as it was, then the same object is returned from withHeat and withCapacity
            setContents(newData, transaction);
        }
    }

    @Override
    public void setHeat(double heat, @Nullable TransactionContext transaction) {
        updateContents(existing -> existing.withHeat(heat), transaction);
    }

    @Override
    public void setHeatCapacity(double newCapacity, @Nullable TransactionContext transaction) {
        updateContents(existing -> existing.withCapacity(newCapacity), transaction);
    }

    @Override
    public void setHeatAndCapacity(double heat, double heatCapacity, @Nullable TransactionContext transaction) {
        updateContents(existing -> existing.withHeat(heat).withCapacity(heatCapacity), transaction);
    }

    @Override
    public void handleHeat(double transfer, TransactionContext transaction) {
        if (Math.abs(transfer) > HeatAPI.EPSILON) {
            updateContents(existing -> existing.withHeat(existing.heatOrAmbient() + transfer), transaction);
        }
    }

    @Override
    public boolean isAmbientTemperature() {
        return getCurrentOrDefault().heat().isEmpty();
    }

    @Override
    public void copyContents(IHeatCapacitor other, @Nullable TransactionContext transaction) {
        if (other instanceof HeatCapacitorWrapper wrapper) {
            other = wrapper.getInternal();
        }
        HeatCapacitorData otherData;
        if (other instanceof ComponentBackedHeatCapacitor otherCapacitor) {
            otherData = otherCapacitor.getCurrentOrDefault();
        } else {
            otherData = new HeatCapacitorData(other.getHeat(), other.getHeatCapacity());
        }
        if (!otherData.equals(getCurrent())) {
            setContents(otherData, transaction);
        }
    }

    @Override
    public void serialize(ValueOutput output) {
        HeatCapacitorData data = getCurrent();
        if (data != null) {
            if (data.heat().isPresent()) {
                output.putDouble(SerializationConstants.STORED, data.heat().getAsDouble());
            }
            output.putDouble(SerializationConstants.HEAT_CAPACITY, data.capacity());
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        HeatCapacitorData data;
        double capacity = input.getDoubleOr(SerializationConstants.HEAT_CAPACITY, defaultData.capacity());
        double stored = input.getDoubleOr(SerializationConstants.STORED, -1);
        if (stored == -1) {
            data = new HeatCapacitorData(capacity);
        } else {
            data = new HeatCapacitorData(stored, capacity);
        }
        if (!data.equals(getCurrent())) {
            setContents(data, null);
        }
    }
}