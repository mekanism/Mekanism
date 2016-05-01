package mekanism.api;

import mekanism.api.energy.ICableOutputter;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * Created by ben on 30/04/16.
 */
public class Capabilities
{
    @CapabilityInject(IStrictEnergyStorage.class)
    public static Capability<IStrictEnergyStorage> ENERGY_STORAGE_CAPABILITY = null;

    @CapabilityInject(IStrictEnergyAcceptor.class)
    public static Capability<IStrictEnergyAcceptor> ENERGY_ACCEPTOR_CAPABILITY = null;

    @CapabilityInject(ICableOutputter.class)
    public static Capability<ICableOutputter> CABLE_OUTPUTTER_CAPABILITY = null;
}
