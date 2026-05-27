package mekanism.common.attachments.containers.heat;

import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.attachments.containers.ComponentBackedContainer;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.attachments.containers.type.IContainerType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.access.ItemAccess;

@NothingNullByDefault
public class ComponentBackedHeatCapacitor extends ComponentBackedContainer<HeatCapacitorData, AttachedHeat> implements IHeatCapacitor {

    private final double inverseConductionCoefficient;
    private final double inverseInsulationCoefficient;
    private final HeatCapacitorData defaultData;

    public ComponentBackedHeatCapacitor(ItemAccess attachedAccess, int slotIndex, double inverseConductionCoefficient, double inverseInsulationCoefficient,
          double defaultHeatCapacity) {
        super(attachedAccess, slotIndex);
        this.inverseConductionCoefficient = inverseConductionCoefficient;
        this.inverseInsulationCoefficient = inverseInsulationCoefficient;
        this.defaultData = new HeatCapacitorData(defaultHeatCapacity);
    }

    protected boolean setContents(AttachedHeat attached, HeatCapacitorData value) {
        return setContents(attached, value, null, true);
    }

    @Override
    protected boolean isEmpty(HeatCapacitorData value) {
        return value.equals(defaultData);
    }

    @Override
    protected IContainerType<?, AttachedHeat> containerType() {
        return ContainerType.HEAT;
    }

    @Override
    protected HeatCapacitorData getContents(AttachedHeat attached) {
        if (containerIndex < 0 || containerIndex >= attached.size()) {
            return defaultData;
        }
        return attached.get(containerIndex);
    }

    /**
     * @apiNote Try to minimize the number of calls to this method so that we don't have to look up the data component multiple times.
     */
    protected HeatCapacitorData getData() {
        return getContents(getAttached());
    }

    @Override
    public double getTemperature() {
        return getData().temperature();
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
        return getData().capacity();
    }

    @Override
    public double getHeat() {
        return getData().heatOrAmbient();
    }

    @Override
    public void setHeat(double heat) {
        AttachedHeat attachedHeat = getAttached();
        setContents(attachedHeat, getContents(attachedHeat).withHeat(heat));
    }

    @Override
    public void handleHeat(double transfer) {
        if (transfer != 0 && Math.abs(transfer) > HeatAPI.EPSILON) {
            AttachedHeat attachedHeat = getAttached();
            if (!attachedHeat.isEmpty()) {
                HeatCapacitorData stored = getContents(attachedHeat);
                setContents(attachedHeat, stored.withHeat(stored.heatOrAmbient() + transfer));
            }
        }
    }

    @Override
    public boolean isAmbientTemperature() {
        return getData().heat().isEmpty();
    }

    @Override
    public void copyContents(IHeatCapacitor other) {
        AttachedHeat attachedHeat = getAttached();
        setContents(attachedHeat, new HeatCapacitorData(other.getHeat(), other.getHeatCapacity()));
    }

    @Override
    public void serialize(ValueOutput output) {
        HeatCapacitorData data = getData();
        if (data.heat().isPresent()) {
            output.putDouble(SerializationConstants.STORED, data.heat().getAsDouble());
        }
        output.putDouble(SerializationConstants.HEAT_CAPACITY, data.capacity());
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
        setContents(getAttached(), data);
    }
}