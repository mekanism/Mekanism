package mekanism.common.capabilities;

import mekanism.api.energy.IStrictEnergyAcceptor;

import net.minecraft.util.EnumFacing;

/**
 * Created by ben on 30/04/16.
 */
public class StrictEnergyAcceptor extends StrictEnergyStorage implements IStrictEnergyAcceptor
{
    public StrictEnergyAcceptor(double capacity)
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
}
