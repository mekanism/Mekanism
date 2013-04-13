package universalelectricity.prefab.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;

import org.bouncycastle.util.Arrays;

import universalelectricity.core.block.IConductor;
import universalelectricity.core.block.IConnector;
import universalelectricity.core.block.INetworkProvider;
import universalelectricity.core.electricity.ElectricityNetwork;
import universalelectricity.core.electricity.IElectricityNetwork;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import universalelectricity.prefab.network.IPacketReceiver;
import universalelectricity.prefab.network.PacketManager;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * This tile entity pre-fabricated for all conductors.
 * 
 * @author Calclavia
 * 
 */
public abstract class TileEntityConductor extends TileEntityAdvanced implements IConductor, IPacketReceiver
{
	private IElectricityNetwork network;

	/**
	 * Used client side to render.
	 */
	public boolean[] visuallyConnected = { false, false, false, false, false, false };

	/**
	 * Stores information on the blocks that this conductor is connected to.
	 */
	public TileEntity[] connectedBlocks = { null, null, null, null, null, null };

	protected String channel = "";

	public void updateConnection(TileEntity tileEntity, ForgeDirection side)
	{
		if (!this.worldObj.isRemote)
		{
			if (tileEntity instanceof IConnector)
			{
				if (((IConnector) tileEntity).canConnect(side.getOpposite()))
				{
					this.connectedBlocks[side.ordinal()] = tileEntity;
					this.visuallyConnected[side.ordinal()] = true;

					if (tileEntity.getClass() == this.getClass() && tileEntity instanceof INetworkProvider)
					{
						this.getNetwork().mergeConnection(((INetworkProvider) tileEntity).getNetwork());
					}

					return;
				}
			}

			if (this.connectedBlocks[side.ordinal()] != null)
			{
				this.getNetwork().stopProducing(this.connectedBlocks[side.ordinal()]);
				this.getNetwork().stopRequesting(this.connectedBlocks[side.ordinal()]);
			}

			this.connectedBlocks[side.ordinal()] = null;
			this.visuallyConnected[side.ordinal()] = false;
		}
	}

	@Override
	public void handlePacketData(INetworkManager network, int type, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		if (this.worldObj.isRemote)
		{
			this.visuallyConnected[0] = dataStream.readBoolean();
			this.visuallyConnected[1] = dataStream.readBoolean();
			this.visuallyConnected[2] = dataStream.readBoolean();
			this.visuallyConnected[3] = dataStream.readBoolean();
			this.visuallyConnected[4] = dataStream.readBoolean();
			this.visuallyConnected[5] = dataStream.readBoolean();
		}
	}

	@Override
	public void initiate()
	{
		this.updateAdjacentConnections();
	}

	@Override
	public void invalidate()
	{
		if (!this.worldObj.isRemote)
		{
			this.getNetwork().splitNetwork(this);
		}

		super.invalidate();
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{
			if (this.ticks % 300 == 0)
			{
				this.updateAdjacentConnections();
			}
		}
	}

	@Override
	public void updateAdjacentConnections()
	{
		if (this.worldObj != null)
		{
			if (!this.worldObj.isRemote)
			{
				boolean[] previousConnections = this.visuallyConnected.clone();

				for (byte i = 0; i < 6; i++)
				{
					this.updateConnection(VectorHelper.getConnectorFromSide(this.worldObj, new Vector3(this), ForgeDirection.getOrientation(i)), ForgeDirection.getOrientation(i));
				}

				/**
				 * Only send packet updates if visuallyConnected changed.
				 */
				if (!Arrays.areEqual(previousConnections, this.visuallyConnected))
				{
					this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
				}
			}

		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return PacketManager.getPacket(this.channel, this, this.visuallyConnected[0], this.visuallyConnected[1], this.visuallyConnected[2], this.visuallyConnected[3], this.visuallyConnected[4], this.visuallyConnected[5]);
	}

	@Override
	public IElectricityNetwork getNetwork()
	{
		if (this.network == null)
		{
			this.setNetwork(new ElectricityNetwork(this));
		}

		return this.network;
	}

	@Override
	public void setNetwork(IElectricityNetwork network)
	{
		this.network = network;
	}

	@Override
	public TileEntity[] getAdjacentConnections()
	{
		return this.connectedBlocks;
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
