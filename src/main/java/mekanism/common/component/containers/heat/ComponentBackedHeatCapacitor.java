package mekanism.common.component.containers.heat;

import java.util.function.UnaryOperator;
import mekanism.api.MekanismPreconditions;
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

    public ComponentBackedHeatCapacitor(ItemAccess attachedAccess, double inverseConductionCoefficient, double inverseInsulationCoefficient) {
        super(attachedAccess);
        this.inverseConductionCoefficient = inverseConductionCoefficient;
        this.inverseInsulationCoefficient = inverseInsulationCoefficient;
    }

    @Override
    protected HeatContainerType containerType() {
        return ContainerType.HEAT;
    }

    @Nullable
    protected HeatCapacitorData getCurrent() {
        return containerType().get(attachedAccess.getResource());
    }

    @Override
    public double getTemperature() {
        HeatCapacitorData current = getCurrent();
        if (current == null) {
            //Fail to update contents due to there being no backing data on the attached access
            return HeatAPI.AMBIENT_TEMP;
        }
        return current.temperature();
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
        HeatCapacitorData current = getCurrent();
        if (current == null) {
            //Fail to update contents due to there being no backing data on the attached access
            return HeatAPI.DEFAULT_HEAT_CAPACITY;
        }
        return current.capacity();
    }

    @Override
    public double getHeat() {
        HeatCapacitorData current = getCurrent();
        if (current == null) {
            //Fail to update contents due to there being no backing data on the attached access
            return HeatAPI.AMBIENT_TEMP * HeatAPI.DEFAULT_HEAT_CAPACITY;
        }
        return current.heatOrAmbient();
    }

    //Note: While callers create capturing lambda's, as these methods are not really being used in item form anyway, it shouldn't have that big a performance impact
    private void updateContents(UnaryOperator<HeatCapacitorData> transformer, @Nullable TransactionContext transaction) {
        HeatCapacitorData current = getCurrent();
        if (current == null) {
            //Fail to update contents due to there being no backing data on the attached access
            return;
        }
        //Note: withHeat handles clamping to zero
        HeatCapacitorData newData = transformer.apply(current);
        if (current != newData) {
            //Note: we can just check instance equality, because if the heat value is the same as it was, then the same object is returned from withHeat and withCapacity
            setContents(newData, transaction);
        }
    }

    @Override
    public void setHeat(double heat, @Nullable TransactionContext transaction) {
        MekanismPreconditions.checkNonNegative(heat);
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
        HeatCapacitorData current = getCurrent();
        if (current == null) {
            //Fail to update contents due to there being no backing data on the attached access
            return true;
        }
        return current.heat().isEmpty();
    }

    @Override
    public void copyContents(IHeatCapacitor other, @Nullable TransactionContext transaction) {
        if (other instanceof HeatCapacitorWrapper wrapper) {
            other = wrapper.getInternal();
        }
        HeatCapacitorData otherData;
        if (other instanceof ComponentBackedHeatCapacitor otherCapacitor) {
            otherData = otherCapacitor.getCurrent();
            if (otherData == null) {
                //Fail to update contents due to there being no backing data on the attached access
                return;
            }
        } else {
            otherData = new HeatCapacitorData(other.getHeat(), other.getHeatCapacity());
        }
        if (!otherData.equals(getCurrent())) {
            setContents(otherData, transaction);
        }
    }

    @Override
    public void serialize(ValueOutput output) {
        output.storeNullable(SerializationConstants.STATE, HeatCapacitorData.CODEC, getCurrent());
    }

    @Override
    public void deserialize(ValueInput input) {
        input.read(SerializationConstants.STATE, HeatCapacitorData.CODEC).ifPresent(state -> {
            if (!state.equals(getCurrent())) {
                setContents(state, null);
            }
        });
    }
}