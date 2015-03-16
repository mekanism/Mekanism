package mekanism.common.tile;

import ic2.api.tile.IWrenchable;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.base.IChunkLoadHandler;
import mekanism.common.base.ITileComponent;
import mekanism.common.base.ITileNetwork;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.network.PacketDataRequest.DataRequestMessage;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.Method;

@Interface(iface = "ic2.api.tile.IWrenchable", modid = "IC2")
public abstract class TileEntityBasicBlock extends TileEntity implements IWrenchable, ITileNetwork, IChunkLoadHandler
{
	/** The direction this block is facing. */
	public int facing;

	public int clientFacing;

	public HashSet<EntityPlayer> openedThisTick = new HashSet<EntityPlayer>();

	/** The players currently using this block. */
	public HashSet<EntityPlayer> playersUsing = new HashSet<EntityPlayer>();

	/** A timer used to send packets to clients. */
	public int ticker;

	public boolean redstone = false;
	public boolean redstoneLastTick = false;

	public boolean doAutoSync = true;

	public List<ITileComponent> components = new ArrayList<ITileComponent>();

	@Override
	public void updateEntity()
	{
		if(!worldObj.isRemote && general.destroyDisabledBlocks)
		{
			MachineType type = MachineType.get(getBlockType(), getBlockMetadata());
			
			if(type != null && !type.isEnabled())
			{
				Mekanism.logger.info("[Mekanism] Destroying machine of type '" + type.name + "' at coords " + Coord4D.get(this) + " as according to config.");
				worldObj.setBlockToAir(xCoord, yCoord, zCoord);
				return;
			}
		}
		
		for(ITileComponent component : components)
		{
			component.tick();
		}

		onUpdate();

		if(!worldObj.isRemote)
		{
			openedThisTick.clear();

			if(doAutoSync && playersUsing.size() > 0)
			{
				for(EntityPlayer player : playersUsing)
				{
					Mekanism.packetHandler.sendTo(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), (EntityPlayerMP)player);
				}
			}
		}

		ticker++;
		redstoneLastTick = redstone;
	}
	
	@Override
	public void onChunkLoad()
	{
		markDirty();
	}

	public void open(EntityPlayer player)
	{
		playersUsing.add(player);
	}

	public void close(EntityPlayer player)
	{
		playersUsing.remove(player);
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		facing = dataStream.readInt();
		redstone = dataStream.readBoolean();

		if(clientFacing != facing)
		{
			MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, worldObj.getBlock(xCoord, yCoord, zCoord));
			clientFacing = facing;
		}

		for(ITileComponent component : components)
		{
			component.read(dataStream);
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(facing);
		data.add(redstone);

		for(ITileComponent component : components)
		{
			component.write(data);
		}

		return data;
	}

	@Override
	public void validate()
	{
		super.validate();

		if(worldObj.isRemote)
		{
			Mekanism.packetHandler.sendToServer(new DataRequestMessage(Coord4D.get(this)));
		}
	}

	/**
	 * Update call for machines. Use instead of updateEntity -- it's called every tick.
	 */
	public abstract void onUpdate();

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		facing = nbtTags.getInteger("facing");
		redstone = nbtTags.getBoolean("redstone");

		for(ITileComponent component : components)
		{
			component.read(nbtTags);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setInteger("facing", facing);
		nbtTags.setBoolean("redstone", redstone);

		for(ITileComponent component : components)
		{
			component.write(nbtTags);
		}
	}

	@Override
	@Method(modid = "IC2")
	public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int side)
	{
		return true;
	}

	@Override
	@Method(modid = "IC2")
	public short getFacing()
	{
		return (short)facing;
	}

	@Override
	public void setFacing(short direction)
	{
		if(canSetFacing(direction))
		{
			facing = direction;
		}

		if(!(facing == clientFacing || worldObj.isRemote))
		{
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
			markDirty();
			clientFacing = facing;
		}
	}

	/**
	 * Whether or not this block's orientation can be changed to a specific direction. True by default.
	 * @param facing - facing to check
	 * @return if the block's orientation can be changed
	 */
	public boolean canSetFacing(int facing)
	{
		return true;
	}

	@Override
	@Method(modid = "IC2")
	public boolean wrenchCanRemove(EntityPlayer entityPlayer)
	{
		return true;
	}

	@Override
	@Method(modid = "IC2")
	public float getWrenchDropRate()
	{
		return 1.0F;
	}

	@Override
	@Method(modid = "IC2")
	public ItemStack getWrenchDrop(EntityPlayer entityPlayer)
	{
		return getBlockType().getPickBlock(null, worldObj, xCoord, yCoord, zCoord, entityPlayer);
	}

	public boolean isPowered()
	{
		return redstone;
	}

	public boolean wasPowered()
	{
		return redstoneLastTick;
	}

	public void onNeighborChange(Block block)
	{
		if(!worldObj.isRemote)
		{
			boolean power = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);

			if(redstone != power)
			{
				redstone = power;
				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
			}
		}
	}
	
	public void onAdded()
	{
		
	}
}
