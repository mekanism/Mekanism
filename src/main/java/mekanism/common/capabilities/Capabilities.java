package mekanism.common.capabilities;

import mekanism.api.IAlloyInteraction;
import mekanism.api.IConfigCardAccess;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.IConfigurable;
import mekanism.api.IEvaporationSolar;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.transmitters.IBlockableConnection;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.capabilities.basic.DefaultAlloyInteraction;
import mekanism.common.capabilities.basic.DefaultBlockableConnection;
import mekanism.common.capabilities.basic.DefaultConfigCardAccess;
import mekanism.common.capabilities.basic.DefaultConfigurable;
import mekanism.common.capabilities.basic.DefaultEvaporationSolar;
import mekanism.common.capabilities.basic.DefaultGasHandler;
import mekanism.common.capabilities.basic.DefaultGridTransmitter;
import mekanism.common.capabilities.basic.DefaultHeatHandler;
import mekanism.common.capabilities.basic.DefaultInfusionHandler;
import mekanism.common.capabilities.basic.DefaultLaserReceptor;
import mekanism.common.capabilities.basic.DefaultLogisticalTransporter;
import mekanism.common.capabilities.basic.DefaultSpecialConfigData;
import mekanism.common.capabilities.basic.DefaultStrictEnergyHandler;
import mekanism.common.radiation.capability.DefaultRadiationEntity;
import mekanism.common.radiation.capability.DefaultRadiationShielding;
import mekanism.common.radiation.capability.IRadiationEntity;
import mekanism.common.radiation.capability.IRadiationShielding;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * Created by ben on 30/04/16.
 */
public class Capabilities {

    @CapabilityInject(IStrictEnergyHandler.class)
    public static Capability<IStrictEnergyHandler> STRICT_ENERGY_CAPABILITY = null;

    @CapabilityInject(IConfigurable.class)
    public static Capability<IConfigurable> CONFIGURABLE_CAPABILITY = null;

    @CapabilityInject(IGasHandler.class)
    public static Capability<IGasHandler> GAS_HANDLER_CAPABILITY = null;

    @CapabilityInject(IInfusionHandler.class)
    public static Capability<IInfusionHandler> INFUSION_HANDLER_CAPABILITY = null;

    @CapabilityInject(IRadiationShielding.class)
    public static Capability<IRadiationShielding> RADIATION_SHIELDING_CAPABILITY = null;

    @CapabilityInject(IHeatHandler.class)
    public static Capability<IHeatHandler> HEAT_HANDLER_CAPABILITY = null;

    @CapabilityInject(IBlockableConnection.class)
    public static Capability<IBlockableConnection> BLOCKABLE_CONNECTION_CAPABILITY = null;

    //TODO: Re-evaluate having this be IGridTransmitter<?, ?, ?> instead of just IGridTransmitter for the capability
    @CapabilityInject(IGridTransmitter.class)
    public static Capability<IGridTransmitter<?, ?, ?>> GRID_TRANSMITTER_CAPABILITY = null;

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

    @CapabilityInject(ILogisticalTransporter.class)
    public static Capability<ILogisticalTransporter> LOGISTICAL_TRANSPORTER_CAPABILITY = null;

    @CapabilityInject(IRadiationEntity.class)
    public static Capability<IRadiationEntity> RADIATION_ENTITY_CAPABILITY = null;

    public static void registerCapabilities() {
        DefaultStrictEnergyHandler.register();

        DefaultGridTransmitter.register();
        DefaultLogisticalTransporter.register();
        DefaultBlockableConnection.register();

        DefaultGasHandler.register();
        DefaultInfusionHandler.register();
        DefaultHeatHandler.register();

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