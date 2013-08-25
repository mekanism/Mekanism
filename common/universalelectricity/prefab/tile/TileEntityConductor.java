package universalelectricity.prefab.tile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.block.IConductor;
import universalelectricity.core.block.IConnector;
import universalelectricity.core.block.INetworkProvider;
import universalelectricity.core.electricity.NetworkLoader;
import universalelectricity.core.grid.IElectricityNetwork;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * This tile entity pre-fabricated for all conductors.
 * 
 * @author Calclavia
 * 
 */
public abstract class TileEntityConductor extends TileEntityAdvanced implements IConductor
{
	private IElectricityNetwork network;

	public TileEntity[] adjacentConnections = null;

	@Override
	public void invalidate()
	{
		if (!this.worldObj.isRemote)
		{
			this.getNetwork().split(this);
		}

		super.invalidate();
	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public IElectricityNetwork getNetwork()
	{
		if (this.network == null)
		{
			this.setNetwork(NetworkLoader.getNewNetwork(this));
		}

		return this.network;
	}

	@Override
	public void setNetwork(IElectricityNetwork network)
	{
		this.network = network;
	}

	@Override
	public void refresh()
	{
		if (!this.worldObj.isRemote)
		{
			this.adjacentConnections = null;

			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tileEntity = VectorHelper.getConnectorFromSide(this.worldObj, new Vector3(this), side);

				if (tileEntity != null)
				{
					if (tileEntity.getClass() == this.getClass() && tileEntity instanceof INetworkProvider)
					{
						this.getNetwork().merge(((INetworkProvider) tileEntity).getNetwork());
					}
				}
			}

			this.getNetwork().refresh();
		}
	}

	@Override
	public TileEntity[] getAdjacentConnections()
	{
		/**
		 * Cache the adjacentConnections.
		 */
		if (this.adjacentConnections == null)
		{
			this.adjacentConnections = new TileEntity[6];

			for (byte i = 0; i < 6; i++)
			{
				ForgeDirection side = ForgeDirection.getOrientation(i);
				TileEntity tileEntity = VectorHelper.getConnectorFromSide(this.worldObj, new Vector3(this), side);

				if (tileEntity instanceof IConnector)
				{
					if (((IConnector) tileEntity).canConnect(side.getOpposite()))
					{
						this.adjacentConnections[i] = tileEntity;
					}
				}
			}
		}

		return this.adjacentConnections;
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return AxisAlignedBB.getAABBPool().getAABB(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 1, this.zCoord + 1);
	}
}
