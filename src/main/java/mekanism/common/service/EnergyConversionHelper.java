package mekanism.common.service;

import mekanism.api.energy.IEnergyConversion;
import mekanism.api.energy.IEnergyConversionHelper;
import mekanism.common.util.UnitDisplayUtils.EnergyUnit;

/**
 * @apiNote Do not instantiate this class directly as it will be done via the service loader. Instead, access instances of this via
 * {@link IEnergyConversionHelper#INSTANCE}
 */
public class EnergyConversionHelper implements IEnergyConversionHelper {

    @Override
    public IEnergyConversion jouleConversion() {
        return EnergyUnit.JOULES;
    }

    @Override
    public IEnergyConversion feConversion() {
        return EnergyUnit.FORGE_ENERGY;
    }
}