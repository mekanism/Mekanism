package calclavia.lib.multiblock;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.network.IPacketReceiver;
import universalelectricity.prefab.network.PacketManager;

import com.google.common.io.ByteArrayDataInput;

/**
 * This is a multiblock to be used for blocks that are bigger than one block.
 * 
 * @author Calclavia
 * 
 */
public class TileEntityMultiBlockPart extends TileEntity implements IPacketReceiver
{
	// The the position of the main block. Relative to this block's position.
	private Vector3 mainBlockPosition;
	public String channel;

	public TileEntityMultiBlockPart()
	{

	}

	public TileEntityMultiBlockPart(String channel)
	{
		this.channel = channel;
	}

	public Vector3 getMainBlock()
	{
		if (this.mainBlockPosition != null)
		{
			return new Vector3(this).translate(this.mainBlockPosition);
		}

		return null;
	}

	public void setMainBlock(Vector3 mainBlock)
	{
		this.mainBlockPosition = mainBlock.clone().translate(new Vector3(this).invert());

		if (!this.worldObj.isRemote)
		{
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		if (this.mainBlockPosition != null)
		{
			if (this.channel == null || this.channel == "" && this.getBlockType() instanceof BlockMulti)
			{
				this.channel = ((BlockMulti) this.getBlockType()).channel;
			}

			return PacketManager.getPacket(this.channel, this, this.mainBlockPosition.intX(), this.mainBlockPosition.intY(), this.mainBlockPosition.intZ());
		}

		return null;
	}

	@Override
	public void handlePacketData(INetworkManager network, int packetType, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try
		{
			this.mainBlockPosition = new Vector3(dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void onBlockRemoval(BlockMulti block)
	{
		if (this.getMainBlock() != null)
		{
			TileEntity tileEntity = this.getMainBlock().getTileEntity(this.worldObj);

			if (tileEntity instanceof IMultiBlock)
			{
				block.destroyMultiBlockStructure((IMultiBlock) tileEntity);
			}
		}
	}

	public boolean onBlockActivated(World par1World, int x, int y, int z, EntityPlayer entityPlayer)
	{
		if (this.getMainBlock() != null)
		{
			TileEntity tileEntity = this.getMainBlock().getTileEntity(this.worldObj);

			if (tileEntity instanceof IBlockActivate)
			{
				return ((IBlockActivate) tileEntity).onActivated(entityPlayer);
			}
		}

		return false;
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		if (nbt.hasKey("mainBlockPosition"))
		{
			this.mainBlockPosition = new Vector3(nbt.getCompoundTag("mainBlockPosition"));
		}

		this.channel = nbt.getString("channel");

	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		if (this.mainBlockPosition != null)
		{
			nbt.setCompoundTag("mainBlockPosition", this.mainBlockPosition.writeToNBT(new NBTTagCompound()));
		}

		nbt.setString("channel", this.channel);
	}

	/**
	 * Determines if this TileEntity requires update calls.
	 * 
	 * @return True if you want updateEntity() to be called, false if not
	 */
	@Override
	public boolean canUpdate()
	{
		return false;
	}
}