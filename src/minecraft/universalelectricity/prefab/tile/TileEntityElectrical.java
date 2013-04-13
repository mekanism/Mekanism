package universalelectricity.prefab.tile;

import universalelectricity.core.block.IConnector;
import universalelectricity.core.block.IVoltage;
import universalelectricity.core.electricity.ElectricityNetworkHelper;

/**
 * Extend this if your TileEntity is electrical.
 * 
 * @author Calclavia
 */
public abstract class TileEntityElectrical extends TileEntityDisableable implements IConnector, IVoltage
{
	public TileEntityElectrical()
	{
		super();
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
	}

	@Override
	public double getVoltage()
	{
		return 120;
	}

	@Override
	public void invalidate()
	{
		ElectricityNetworkHelper.invalidate(this);
		super.invalidate();

	}
}
