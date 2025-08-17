package mekanism.common.attachments.containers.heat;

import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.attachments.containers.ComponentBackedContainer;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public class ComponentBackedHeatCapacitor extends ComponentBackedContainer<HeatCapacitorData, AttachedHeat> implements IHeatCapacitor {

    private final double inverseConductionCoefficient;
    private final double inverseInsulationCoefficient;
    private final HeatCapacitorData defaultData;

    public ComponentBackedHeatCapacitor(ItemStack attachedTo, int slotIndex, double inverseConductionCoefficient, double inverseInsulationCoefficient) {
        this(attachedTo, slotIndex, inverseConductionCoefficient, inverseInsulationCoefficient, HeatAPI.DEFAULT_HEAT_CAPACITY);
    }

    public ComponentBackedHeatCapacitor(ItemStack attachedTo, int slotIndex, double inverseConductionCoefficient, double inverseInsulationCoefficient,
          double defaultHeatCapacity) {
        super(attachedTo, slotIndex);
        this.inverseConductionCoefficient = inverseConductionCoefficient;
        this.inverseInsulationCoefficient = inverseInsulationCoefficient;
        this.defaultData = new HeatCapacitorData(defaultHeatCapacity);
    }

    @Override
    protected HeatCapacitorData copy(HeatCapacitorData toCopy) {
        //HeatCapacitorData is already immutable, so we don't need to copy it
        return toCopy;
    }

    @Override
    protected boolean isEmpty(HeatCapacitorData value) {
        return value.equals(defaultData);
    }

    @Override
    protected ContainerType<?, AttachedHeat, ?> containerType() {
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

    @Override//TODO - 1.21: Re-evaluate this override
    protected boolean shouldUpdate(AttachedHeat attached, HeatCapacitorData value) {
        return !getContents(attached).equals(value);
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
    public CompoundTag serializeNBT(Provider provider) {
        CompoundTag nbt = new CompoundTag();
        HeatCapacitorData data = getData();
        if (data.heat().isPresent()) {
            nbt.putDouble(SerializationConstants.STORED, data.heat().getAsDouble());
        }
        nbt.putDouble(SerializationConstants.HEAT_CAPACITY, data.capacity());
        return nbt;
    }

    @Override
    public void deserializeNBT(Provider provider, CompoundTag nbt) {
        double capacity;
        HeatCapacitorData data;
        if (nbt.contains(SerializationConstants.HEAT_CAPACITY, Tag.TAG_DOUBLE)) {
            capacity = nbt.getDouble(SerializationConstants.HEAT_CAPACITY);
        } else {
            capacity = defaultData.capacity();
        }
        if (nbt.contains(SerializationConstants.STORED, Tag.TAG_DOUBLE)) {
            data = new HeatCapacitorData(nbt.getDouble(SerializationConstants.STORED), capacity);
        } else {
            data = new HeatCapacitorData(capacity);
        }
        setContents(getAttached(), data);
    }
}