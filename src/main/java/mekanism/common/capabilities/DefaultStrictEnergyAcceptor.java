package mekanism.common.capabilities;

import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.common.capabilities.DefaultStorageHelper.DefaultStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Created by ben on 30/04/16.
 */
public class DefaultStrictEnergyAcceptor extends DefaultStrictEnergyStorage implements IStrictEnergyAcceptor, INBTSerializable<NBTTagCompound>
{
    public DefaultStrictEnergyAcceptor()
    {
        this(0);
    }

    public DefaultStrictEnergyAcceptor(double capacity)
    {
        super(capacity);
    }

    @Override
    public double transferEnergyToAcceptor(EnumFacing side, double amount)
    {
        double used = Math.min(amount, Math.max(0, getMaxEnergy() - getEnergy()));
        setEnergy(getEnergy() + used);
        return used;
    }

    @Override
    public boolean canReceiveEnergy(EnumFacing side)
    {
        return true;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        return super.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        super.deserializeNBT(nbt);
    }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IStrictEnergyAcceptor.class, new DefaultStorage<>(), DefaultStrictEnergyAcceptor.class);
    }
}
