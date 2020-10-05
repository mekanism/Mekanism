package mekanism.common.capabilities;

import mekanism.api.IAlloyInteraction;
import mekanism.api.IConfigCardAccess;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.IConfigurable;
import mekanism.api.IEvaporationSolar;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.common.capabilities.basic.DefaultAlloyInteraction;
import mekanism.common.capabilities.basic.DefaultChemicalHandler.DefaultGasHandler;
import mekanism.common.capabilities.basic.DefaultChemicalHandler.DefaultInfusionHandler;
import mekanism.common.capabilities.basic.DefaultChemicalHandler.DefaultPigmentHandler;
import mekanism.common.capabilities.basic.DefaultChemicalHandler.DefaultSlurryHandler;
import mekanism.common.capabilities.basic.DefaultConfigCardAccess;
import mekanism.common.capabilities.basic.DefaultConfigurable;
import mekanism.common.capabilities.basic.DefaultEvaporationSolar;
import mekanism.common.capabilities.basic.DefaultHeatHandler;
import mekanism.common.capabilities.basic.DefaultLaserReceptor;
import mekanism.common.capabilities.basic.DefaultSpecialConfigData;
import mekanism.common.capabilities.basic.DefaultStrictEnergyHandler;
import mekanism.common.lib.radiation.capability.DefaultRadiationEntity;
import mekanism.common.lib.radiation.capability.DefaultRadiationShielding;
import mekanism.common.lib.radiation.capability.IRadiationEntity;
import mekanism.common.lib.radiation.capability.IRadiationShielding;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class Capabilities {

    private Capabilities() {
    }

    @CapabilityInject(IGasHandler.class)
    public static Capability<IGasHandler> GAS_HANDLER_CAPABILITY = null;

    @CapabilityInject(IInfusionHandler.class)
    public static Capability<IInfusionHandler> INFUSION_HANDLER_CAPABILITY = null;

    @CapabilityInject(IPigmentHandler.class)
    public static Capability<IPigmentHandler> PIGMENT_HANDLER_CAPABILITY = null;

    @CapabilityInject(ISlurryHandler.class)
    public static Capability<ISlurryHandler> SLURRY_HANDLER_CAPABILITY = null;

    @CapabilityInject(IHeatHandler.class)
    public static Capability<IHeatHandler> HEAT_HANDLER_CAPABILITY = null;

    @CapabilityInject(IStrictEnergyHandler.class)
    public static Capability<IStrictEnergyHandler> STRICT_ENERGY_CAPABILITY = null;

    @CapabilityInject(IConfigurable.class)
    public static Capability<IConfigurable> CONFIGURABLE_CAPABILITY = null;

    @CapabilityInject(IAlloyInteraction.class)
    public static Capability<IAlloyInteraction> ALLOY_INTERACTION_CAPABILITY = null;

    @CapabilityInject(IConfigCardAccess.class)
    public static Capability<IConfigCardAccess> CONFIG_CARD_CAPABILITY = null;

    @CapabilityInject(ISpecialConfigData.class)
    public static Capability<ISpecialConfigData> SPECIAL_CONFIG_DATA_CAPABILITY = null;

    @CapabilityInject(IEvaporationSolar.class)
    public static Capability<IEvaporationSolar> EVAPORATION_SOLAR_CAPABILITY = null;

    @CapabilityInject(ILaserReceptor.class)
    public static Capability<ILaserReceptor> LASER_RECEPTOR_CAPABILITY = null;

    @CapabilityInject(IRadiationShielding.class)
    public static Capability<IRadiationShielding> RADIATION_SHIELDING_CAPABILITY = null;

    @CapabilityInject(IRadiationEntity.class)
    public static Capability<IRadiationEntity> RADIATION_ENTITY_CAPABILITY = null;

    public static void registerCapabilities() {
        DefaultGasHandler.register();
        DefaultInfusionHandler.register();
        DefaultPigmentHandler.register();
        DefaultSlurryHandler.register();
        DefaultHeatHandler.register();
        DefaultStrictEnergyHandler.register();

        DefaultConfigurable.register();
        DefaultAlloyInteraction.register();
        DefaultConfigCardAccess.register();
        DefaultSpecialConfigData.register();
        DefaultEvaporationSolar.register();
        DefaultLaserReceptor.register();

        DefaultRadiationShielding.register();
        DefaultRadiationEntity.register();
    }
}