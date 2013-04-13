package universalelectricity.core.block;

import universalelectricity.core.electricity.IElectricityNetwork;

/**
 * Applied to TileEntities that has an instance of an electricity network.
 * 
 * @author Calclavia
 * 
 */
public interface INetworkProvider
{
	public IElectricityNetwork getNetwork();

	public void setNetwork(IElectricityNetwork network);
}
