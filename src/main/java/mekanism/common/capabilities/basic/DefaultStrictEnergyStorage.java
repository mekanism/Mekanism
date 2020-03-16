package mekanism.common.capabilities.basic;

import mekanism.api.NBTConstants;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.capabilities.basic.DefaultStorageHelper.DefaultStorage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Created by ben on 30/04/16.
 */
@Deprecated
public class DefaultStrictEnergyStorage implements IStrictEnergyStorage, INBTSerializable<CompoundNBT> {

    private double energyStored = 0;
    private double maxEnergy;

    public DefaultStrictEnergyStorage() {
        this(0);
    }

    public DefaultStrictEnergyStorage(double capacity) {
        maxEnergy = capacity;
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IStrictEnergyStorage.class, new DefaultStorage<>(), DefaultStrictEnergyStorage::new);
    }

    @Override
    public double getEnergy() {
        return energyStored;
    }

    @Override
    public void setEnergy(double energy) {
        energyStored = energy;
    }

    @Override
    public double getMaxEnergy() {
        return maxEnergy;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.putDouble(NBTConstants.MAX_ENERGY, getMaxEnergy());
        tag.putDouble(NBTConstants.ENERGY_STORED, getEnergy());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        maxEnergy = nbt.getDouble(NBTConstants.MAX_ENERGY);
        setEnergy(nbt.getDouble(NBTConstants.ENERGY_STORED));
    }
}