package mekanism.common.attachments.containers.energy;

import java.util.Objects;
import java.util.function.LongSupplier;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.ResistiveHeaterEnergyContainer;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault//TODO - 1.21: Figure out how to implement this properly??
public class ComponentBackedResistiveEnergyContainer extends ComponentBackedEnergyContainer {

    public static ComponentBackedResistiveEnergyContainer create(ContainerType<?, ?, ?> ignored, ItemStack attachedTo, int containerIndex) {
        AttributeEnergy attributeEnergy = Objects.requireNonNull(MekanismBlockTypes.RESISTIVE_HEATER.get(AttributeEnergy.class));
        return new ComponentBackedResistiveEnergyContainer(attachedTo, containerIndex, () -> (long) (attributeEnergy.getStorage() * 0.005),
              attributeEnergy::getStorage, attributeEnergy.getUsage());
    }

    private long currentMaxEnergy;
    private long energyPerTick;

    private ComponentBackedResistiveEnergyContainer(ItemStack attachedTo, int containerIndex, LongSupplier rate, LongSupplier capacity,
          long baseEnergyPerTick) {
        super(attachedTo, containerIndex, BasicEnergyContainer.manualOnly, BasicEnergyContainer.alwaysTrue, rate, capacity);
        this.currentMaxEnergy = super.getMaxEnergy();
        this.energyPerTick = baseEnergyPerTick;
    }

    @Override
    public long getMaxEnergy() {
        return currentMaxEnergy;
    }

    private void updateEnergyUsage(long energyUsage) {
        energyPerTick = energyUsage;
        this.currentMaxEnergy = energyUsage * ResistiveHeaterEnergyContainer.USAGE_MULTIPLIER;
        //Clamp the energy
        setEnergy(getEnergy());
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = super.serializeNBT(provider);
        nbt.putLong(SerializationConstants.ENERGY_USAGE, energyPerTick);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        NBTUtils.setFloatingLongIfPresent(nbt, SerializationConstants.ENERGY_USAGE, energyUsage -> updateEnergyUsage(energyUsage.longValue()));//todo 1.22 - backcompat
        NBTUtils.setLongIfPresent(nbt, SerializationConstants.ENERGY_USAGE, this::updateEnergyUsage);
        super.deserializeNBT(provider, nbt);
    }
}