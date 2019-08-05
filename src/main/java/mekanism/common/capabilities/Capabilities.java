package mekanism.common.capabilities;

import mekanism.api.IAlloyInteraction;
import mekanism.api.IConfigCardAccess;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.IConfigurable;
import mekanism.api.IEvaporationSolar;
import mekanism.api.IHeatTransfer;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyOutputter;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.transmitters.IBlockableConnection;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.base.ITileNetwork;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * Created by ben on 30/04/16.
 */
public class Capabilities {

    @CapabilityInject(IStrictEnergyStorage.class)
    public static Capability<IStrictEnergyStorage> ENERGY_STORAGE_CAPABILITY = null;

    @CapabilityInject(IStrictEnergyAcceptor.class)
    public static Capability<IStrictEnergyAcceptor> ENERGY_ACCEPTOR_CAPABILITY = null;

    @CapabilityInject(IStrictEnergyOutputter.class)
    public static Capability<IStrictEnergyOutputter> ENERGY_OUTPUTTER_CAPABILITY = null;

    @CapabilityInject(IConfigurable.class)
    public static Capability<IConfigurable> CONFIGURABLE_CAPABILITY = null;

    @CapabilityInject(IGasHandler.class)
    public static Capability<IGasHandler> GAS_HANDLER_CAPABILITY = null;

    @CapabilityInject(IHeatTransfer.class)
    public static Capability<IHeatTransfer> HEAT_TRANSFER_CAPABILITY = null;

    @CapabilityInject(IBlockableConnection.class)
    public static Capability<IBlockableConnection> BLOCKABLE_CONNECTION_CAPABILITY = null;

    @CapabilityInject(IGridTransmitter.class)
    public static Capability<IGridTransmitter> GRID_TRANSMITTER_CAPABILITY = null;

    @CapabilityInject(IAlloyInteraction.class)
    public static Capability<IAlloyInteraction> ALLOY_INTERACTION_CAPABILITY = null;

    //Kept in for compatibility with any mods that may try to reference it.
    // However, nothing in Mekanism uses this anymore
    @Deprecated
    @CapabilityInject(ITubeConnection.class)
    public static Capability<ITubeConnection> TUBE_CONNECTION_CAPABILITY = null;

    @CapabilityInject(IConfigCardAccess.class)
    public static Capability<IConfigCardAccess> CONFIG_CARD_CAPABILITY = null;

    @CapabilityInject(ISpecialConfigData.class)
    public static Capability<ISpecialConfigData> SPECIAL_CONFIG_DATA_CAPABILITY = null;

    @CapabilityInject(IEvaporationSolar.class)
    public static Capability<IEvaporationSolar> EVAPORATION_SOLAR_CAPABILITY = null;

    @CapabilityInject(ILaserReceptor.class)
    public static Capability<ILaserReceptor> LASER_RECEPTOR_CAPABILITY = null;

    @CapabilityInject(ITileNetwork.class)
    public static Capability<ITileNetwork> TILE_NETWORK_CAPABILITY = null;

    @CapabilityInject(ILogisticalTransporter.class)
    public static Capability<ILogisticalTransporter> LOGISTICAL_TRANSPORTER_CAPABILITY = null;

    public static void registerCapabilities() {
        DefaultStrictEnergyStorage.register();
        DefaultStrictEnergyAcceptor.register();
        DefaultCableOutputter.register();

        DefaultGridTransmitter.register();
        DefaultLogisticalTransporter.register();
        DefaultBlockableConnection.register();

        DefaultGasHandler.register();
        DefaultTubeConnection.register();

        DefaultConfigurable.register();
        DefaultTileNetwork.register();
        DefaultAlloyInteraction.register();
        DefaultHeatTransfer.register();
        DefaultConfigCardAccess.register();
        DefaultSpecialConfigData.register();
        DefaultEvaporationSolar.register();
        DefaultLaserReceptor.register();
    }
}