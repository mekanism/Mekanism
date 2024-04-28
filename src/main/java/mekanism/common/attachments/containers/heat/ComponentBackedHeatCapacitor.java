package mekanism.common.attachments.containers.heat;

import java.util.function.Supplier;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.attachments.containers.ComponentBackedContainer;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentType;
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
    protected Supplier<? extends DataComponentType<AttachedHeat>> dataComponentType() {
        return MekanismDataComponents.ATTACHED_HEAT;
    }

    @Override
    protected HeatCapacitorData copy(HeatCapacitorData toCopy) {
        //HeatCapacitorData is already immutable, so we don't need to copy it
        return toCopy;
    }

    @Override
    protected boolean isEmpty(HeatCapacitorData value) {
        //TODO - 1.20.5: Re-evaluate
        return value.equals(defaultData);
    }

    protected HeatCapacitorData getData() {
        //TODO - 1.20.5: Similar to getBasicInventorySlot do we want to reduce calls to this? Probably (We mostly do so, but we probably want to add a note here)
        AttachedHeat attachedHeat = getAttached();
        return attachedHeat == null ? defaultData : getContents(attachedHeat);
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
        if (attachedHeat != null) {
            HeatCapacitorData stored = getContents(attachedHeat);
            setContents(attachedHeat, new HeatCapacitorData(heat, stored.capacity()));
        }
        //TODO - 1.20.5: Else initialize to whatever the default size is meant to be?
    }

    @Override//TODO - 1.20.5: Re-evaluate this override
    protected void setContents(AttachedHeat attached, HeatCapacitorData value) {
        HeatCapacitorData stored = getContents(attached);
        if (!stored.equals(value)) {
            attachedTo.set(dataComponentType(), attached.with(containerIndex, value));
            onContentsChanged();
        }
    }

    @Override
    public void handleHeat(double transfer) {
        if (transfer != 0 && Math.abs(transfer) > HeatAPI.EPSILON) {
            AttachedHeat attachedHeat = getAttached();
            if (attachedHeat != null) {
                HeatCapacitorData stored = getContents(attachedHeat);
                setContents(attachedHeat, new HeatCapacitorData(stored.heat() + transfer, stored.capacity()));
            }
        }
    }

    @Override
    public CompoundTag serializeNBT(Provider provider) {
        //TODO - 1.20.5: This is a copy of BasicHeatCapacitor#serializeNBT. We might need to also grab the specific overrides of
        // that method as special component backed inventory slots, that then access and put that other data as a different component?
        CompoundTag nbt = new CompoundTag();
        HeatCapacitorData data = getData();
        nbt.putDouble(NBTConstants.STORED, data.heat());
        nbt.putDouble(NBTConstants.HEAT_CAPACITY, data.capacity());
        return nbt;
    }

    @Override
    public void deserializeNBT(Provider provider, CompoundTag nbt) {
        double capacity;
        if (nbt.contains(NBTConstants.HEAT_CAPACITY, Tag.TAG_DOUBLE)) {
            capacity = nbt.getDouble(NBTConstants.HEAT_CAPACITY);
        } else {
            capacity = defaultData.capacity();
        }
        setContents(new HeatCapacitorData(nbt.getDouble(NBTConstants.STORED), capacity));
    }
}