package mekanism.common.capabilities;

import mekanism.api.energy.IStrictEnergyStorage;

/**
 * Created by ben on 30/04/16.
 */
public class StrictEnergyStorage implements IStrictEnergyStorage
{
    double energyStored = 0;
    double maxEnergy;

    public StrictEnergyStorage(double capacity)
    {
        maxEnergy = capacity;
    }

    @Override
    public double getEnergy()
    {
        return energyStored;
    }

    @Override
    public void setEnergy(double energy)
    {
        energyStored = energy;
    }

    @Override
    public double getMaxEnergy()
    {
        return maxEnergy;
    }
}
