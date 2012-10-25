package universalelectricity.prefab;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.Vector3;
import universalelectricity.electricity.ElectricityManager;
import universalelectricity.electricity.ElectricityNetwork;
import universalelectricity.implement.IConductor;
import universalelectricity.implement.IConnector;
import universalelectricity.prefab.network.IPacketReceiver;

import com.google.common.io.ByteArrayDataInput;

/**
 * This tile entity pre-fabricated for all
 * conductors.
 * 
 * @author Calclavia
 * 
 */
public abstract class TileEntityConductor extends TileEntityAdvanced implements IConductor, IPacketReceiver
{
	private ElectricityNetwork network;

	/**
	 * Stores information on the blocks that this
	 * conductor is connected to
	 */
	public TileEntity[] connectedBlocks =
	{ null, null, null, null, null, null };

	public TileEntityConductor()
	{
		this.reset();
	}

	@Override
	public ElectricityNetwork getNetwork()
	{
		return this.network;
	}

	@Override
	public void setNetwork(ElectricityNetwork network)
	{
		this.network = network;
	}

	@Override
	public TileEntity[] getConnectedBlocks()
	{
		return connectedBlocks;
	}

	@Override
	public void initiate()
	{
		this.refreshConnectedBlocks();
	}

	@Override
	public void updateConnection(TileEntity tileEntity, ForgeDirection side)
	{
		if (tileEntity != null)
		{
			if (tileEntity instanceof IConnector)
			{
				this.connectedBlocks[side.ordinal()] = tileEntity;

				if (tileEntity.getClass() == this.getClass())
				{
					ElectricityManager.instance.mergeConnection(this.getNetwork(), ((TileEntityConductor) tileEntity).getNetwork());
				}

				return;
			}
		}

		if (this.connectedBlocks[side.ordinal()] != null)
		{
			if (this.connectedBlocks[side.ordinal()] instanceof IConductor)
			{
				ElectricityManager.instance.splitConnection(this, (IConductor) this.getConnectedBlocks()[side.ordinal()]);
			}
		}

		this.connectedBlocks[side.ordinal()] = null;
	}

	@Override
	public void updateConnectionWithoutSplit(TileEntity tileEntity, ForgeDirection side)
	{
		if (tileEntity != null)
		{
			if (tileEntity instanceof IConnector)
			{
				this.connectedBlocks[side.ordinal()] = tileEntity;

				if (tileEntity.getClass() == this.getClass())
				{
					ElectricityManager.instance.mergeConnection(this.getNetwork(), ((TileEntityConductor) tileEntity).getNetwork());
				}

				return;
			}
		}

		this.connectedBlocks[side.ordinal()] = null;
	}

	@Override
	public void handlePacketData(INetworkManager network, int type, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		if (this.worldObj.isRemote)
		{
			this.refreshConnectedBlocks();
		}
	}

	/**
	 * Determines if this TileEntity requires
	 * update calls.
	 * 
	 * @return True if you want updateEntity() to
	 *         be called, false if not
	 */
	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public void reset()
	{
		this.network = null;

		if (ElectricityManager.instance != null)
		{
			ElectricityManager.instance.registerConductor(this);
		}
	}

	@Override
	public void refreshConnectedBlocks()
	{
		if (this.worldObj != null)
		{
			for (byte i = 0; i < 6; i++)
			{
				this.updateConnection(Vector3.getConnectorFromSide(this.worldObj, Vector3.get(this), ForgeDirection.getOrientation(i)), ForgeDirection.getOrientation(i));
			}
		}
	}

	@Override
	public World getWorld()
	{
		return this.worldObj;
	}

	@Override
	public boolean canConnect(ForgeDirection side)
	{
		return true;
	}
}
