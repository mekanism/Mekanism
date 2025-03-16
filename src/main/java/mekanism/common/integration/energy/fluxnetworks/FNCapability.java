package mekanism.common.integration.energy.fluxnetworks;

import mekanism.common.capabilities.MultiTypeCapability;
import sonar.fluxnetworks.api.FluxCapabilities;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

public class FNCapability {

    //Note: this must be in a separate class to avoid class loading issues
    static final MultiTypeCapability<IFNEnergyStorage> ENERGY = new MultiTypeCapability<>(
          FluxCapabilities.BLOCK,
          FluxCapabilities.ITEM,
          FluxCapabilities.ENTITY
    );
}