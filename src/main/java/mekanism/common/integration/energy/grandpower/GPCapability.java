package mekanism.common.integration.energy.grandpower;

import dev.technici4n.grandpower.api.ILongEnergyStorage;
import mekanism.common.capabilities.MultiTypeCapability;

public class GPCapability {

    //Note: this must be in a separate class to avoid class loading issues
    static final MultiTypeCapability<ILongEnergyStorage> ENERGY = new MultiTypeCapability<>(ILongEnergyStorage.BLOCK, ILongEnergyStorage.ITEM, ILongEnergyStorage.ENTITY);
}