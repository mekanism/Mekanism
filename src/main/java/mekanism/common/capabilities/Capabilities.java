package mekanism.common.capabilities;

import mekanism.api.IAlloyInteraction;
import mekanism.api.IConfigurable;
import mekanism.api.IHeatTransfer;
import mekanism.api.energy.ICableOutputter;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mekanism.api.transmitters.IBlockableConnection;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.base.ITileNetwork;
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

    @CapabilityInject(ILogisticalTransporter.class)
    public static Capability<ILogisticalTransporter> LOGISTICAL_TRANSPORTER_CAPABILITY = null;

    @CapabilityInject(IAlloyInteraction.class)
    public static Capability<IAlloyInteraction> ALLOY_INTERACTION_CAPABILITY = null;

    @CapabilityInject(ITileNetwork.class)
    public static Capability<ITileNetwork> TILE_NETWORK_CAPABILITY = null;
    
    @CapabilityInject(ITubeConnection.class)
    public static Capability<ITubeConnection> TUBE_CONNECTION_CAPABILITY = null;

    public static void registerCapabilities()
    {
        StrictEnergyStorage.register();
        StrictEnergyAcceptor.register();
        CableOutputter.register();

        GridTransmitterTile.register();
        LogisticalTransporter.register();
        
        GasHandler.register();
        TubeConnection.register();

        Configurable.register();
        TileNetwork.register();
        AlloyInteraction.register();
    }
}