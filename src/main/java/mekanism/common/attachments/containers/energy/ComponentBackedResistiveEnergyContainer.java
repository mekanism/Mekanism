package mekanism.common.attachments.containers.energy;

import java.util.Objects;
import mekanism.api.NBTConstants;
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

@NothingNullByDefault//TODO - 1.20.5: Figure out how to implement this properly??
public class ComponentBackedResistiveEnergyContainer extends ComponentBackedEnergyContainer {

    public static ComponentBackedResistiveEnergyContainer create(ContainerType<?, ?, ?> ignored, ItemStack attachedTo, int containerIndex) {
        AttributeEnergy attributeEnergy = Objects.requireNonNull(MekanismBlockTypes.RESISTIVE_HEATER.get(AttributeEnergy.class));
        return new ComponentBackedResistiveEnergyContainer(attachedTo, containerIndex, () -> attributeEnergy.getStorage().multiply(0.005),
              attributeEnergy::getStorage, attributeEnergy.getUsage());
    }

    private FloatingLong currentMaxEnergy;
    private FloatingLong energyPerTick;

    private ComponentBackedResistiveEnergyContainer(ItemStack attachedTo, int containerIndex, FloatingLongSupplier rate, FloatingLongSupplier capacity,
          FloatingLong baseEnergyPerTick) {
        super(attachedTo, containerIndex, BasicEnergyContainer.manualOnly, BasicEnergyContainer.alwaysTrue, rate, capacity);
        this.currentMaxEnergy = super.getMaxEnergy();
        this.energyPerTick = baseEnergyPerTick.copyAsConst();
    }

    @Override
    public FloatingLong getMaxEnergy() {
        return currentMaxEnergy;
    }

    private void updateEnergyUsage(FloatingLong energyUsage) {
        energyPerTick = energyUsage;
        this.currentMaxEnergy = energyUsage.multiply(ResistiveHeaterEnergyContainer.USAGE_MULTIPLIER).copyAsConst();
        //Clamp the energy
        setEnergy(getEnergy());
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = super.serializeNBT(provider);
        nbt.putString(NBTConstants.ENERGY_USAGE, energyPerTick.toString());
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        NBTUtils.setFloatingLongIfPresent(nbt, NBTConstants.ENERGY_USAGE, this::updateEnergyUsage);
        super.deserializeNBT(provider, nbt);
    }
}