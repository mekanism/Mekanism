package mekanism.common.capabilities.energy.item;

import java.util.Objects;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.ResistiveHeaterEnergyContainer;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

@NothingNullByDefault
public class ResistiveHeaterItemEnergyContainer extends RateLimitEnergyContainer {

    public static ResistiveHeaterItemEnergyContainer create(AttributeEnergy attributeEnergy) {
        Objects.requireNonNull(attributeEnergy);
        return new ResistiveHeaterItemEnergyContainer(() -> attributeEnergy.getStorage().multiply(0.005), attributeEnergy::getStorage, attributeEnergy.getUsage());
    }

    private FloatingLong currentMaxEnergy;
    private FloatingLong energyPerTick;

    private ResistiveHeaterItemEnergyContainer(FloatingLongSupplier rate, FloatingLongSupplier capacity, FloatingLong baseEnergyPerTick) {
        super(rate, capacity, BasicEnergyContainer.manualOnly, BasicEnergyContainer.alwaysTrue, null);
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
    public boolean isCompatible(IEnergyContainer other) {
        return super.isCompatible(other) && energyPerTick.equals(((ResistiveHeaterItemEnergyContainer) other).energyPerTick);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        NBTUtils.setFloatingLongIfPresent(nbt, NBTConstants.ENERGY_USAGE, this::updateEnergyUsage);
        super.deserializeNBT(provider, nbt);
    }
}