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
        this.defaultData = new HeatCapacitorData(0.0, defaultHeatCapacity);
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
     * {@inheritDoc}
     *
     * @apiNote Try to minimize the number of calls to this method so that we don't have to look up the data component multiple times.
     */
    protected HeatCapacitorData getData() {
        return getContents(getAttached());
    }

    @Override
    public double getTemperature() {
        HeatCapacitorData data = getData();
        return data.heat() / data.capacity();
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
        return getData().heat();
    }

    @Override
    public void setHeat(double heat) {
        AttachedHeat attachedHeat = getAttached();
        if (!attachedHeat.isEmpty()) {
            HeatCapacitorData stored = getContents(attachedHeat);
            setContents(attachedHeat, new HeatCapacitorData(heat, stored.capacity()));
        }
        //TODO - 1.21: Else initialize to whatever the default size is meant to be?
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
                setContents(attachedHeat, new HeatCapacitorData(stored.heat() + transfer, stored.capacity()));
            }
        }
    }

    @Override
    public CompoundTag serializeNBT(Provider provider) {
        CompoundTag nbt = new CompoundTag();
        HeatCapacitorData data = getData();
        nbt.putDouble(SerializationConstants.STORED, data.heat());
        nbt.putDouble(SerializationConstants.HEAT_CAPACITY, data.capacity());
        return nbt;
    }

    @Override
    public void deserializeNBT(Provider provider, CompoundTag nbt) {
        double capacity;
        if (nbt.contains(SerializationConstants.HEAT_CAPACITY, Tag.TAG_DOUBLE)) {
            capacity = nbt.getDouble(SerializationConstants.HEAT_CAPACITY);
        } else {
            capacity = defaultData.capacity();
        }
        setContents(getAttached(), new HeatCapacitorData(nbt.getDouble(SerializationConstants.STORED), capacity));
    }
}