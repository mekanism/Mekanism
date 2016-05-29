package mekanism.common.capabilities;

import mekanism.api.capabilities.DefaultAlloyInteraction;
import mekanism.api.capabilities.DefaultBlockableConnection;
import mekanism.api.capabilities.DefaultCableOutputter;
import mekanism.api.capabilities.DefaultConfigCardAccess;
import mekanism.api.capabilities.DefaultConfigurable;
import mekanism.api.capabilities.DefaultEvaporationSolar;
import mekanism.api.capabilities.DefaultGasHandler;
import mekanism.api.capabilities.DefaultGridTransmitter;
import mekanism.api.capabilities.DefaultHeatTransfer;
import mekanism.api.capabilities.DefaultLaserReceptor;
import mekanism.api.capabilities.DefaultSpecialConfigData;
import mekanism.api.capabilities.DefaultStrictEnergyAcceptor;
import mekanism.api.capabilities.DefaultStrictEnergyStorage;
import mekanism.api.capabilities.DefaultTubeConnection;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.base.ITileNetwork;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class BaseCapabilities {

	@CapabilityInject(ILogisticalTransporter.class)
	public static Capability<ILogisticalTransporter> LOGISTICAL_TRANSPORTER_CAPABILITY = null;
	
	@CapabilityInject(ITileNetwork.class)
	public static Capability<ITileNetwork> TILE_NETWORK_CAPABILITY = null;

    public static void registerCapabilities()
    {
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
