package mekanism.common.integration.energy.fluxnetworks;

import mekanism.common.capabilities.MultiTypeCapability;
import mekanism.common.integration.MekanismHooks;
import net.minecraft.resources.ResourceLocation;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

public class FNCapability {

    //Note: this must be in a separate class to avoid class loading issues
    //TODO - 1.20.2: Figure out what the actual things are called RL wise
    static final MultiTypeCapability<IFNEnergyStorage> ENERGY = new MultiTypeCapability<>(ResourceLocation.fromNamespaceAndPath(MekanismHooks.FLUX_NETWORKS_MOD_ID, "energy_handler"), IFNEnergyStorage.class);
}