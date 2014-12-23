package mekanism.common.tile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.base.ITileComponent;
import mekanism.common.base.ITileNetwork;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.network.PacketDataRequest.DataRequestMessage;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional.Interface;

import io.netty.buffer.ByteBuf;

import ic2.api.tile.IWrenchable;

@Interface(iface = "ic2.api.tile.IWrenchable", modid = "IC2API")
public abstract class TileEntityBasicBlock extends TileEntity implements ITileNetwork, IUpdatePlayerListBox, IWrenchable
{
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
	public void update()
	{
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
	public void handlePacketData(ByteBuf dataStream)
	{
		redstone = dataStream.readBoolean();

		for(ITileComponent component : components)
		{
			component.read(dataStream);
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
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
	 * Update call for machines. Use instead of update -- it's called every tick.
	 */
	public abstract void onUpdate();

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

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

		nbtTags.setBoolean("redstone", redstone);

		for(ITileComponent component : components)
		{
			component.write(nbtTags);
		}
	}

	@Override
	public EnumFacing getFacing()
	{
		return (EnumFacing)getWorld().getBlockState(getPos()).getValue(BlockStateFacing.facingProperty);
	}

	/**
	 * Whether or not this block's orientation can be changed to a specific direction. True by default.
	 * @param facing - facing to check
	 * @return if the block's orientation can be changed
	 */
	public boolean canSetFacing(EnumFacing facing)
	{
		return true;
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
			boolean power = worldObj.isBlockIndirectlyGettingPowered(getPos()) > 0;

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

	@Override
	public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, EnumFacing side)
	{
		return canSetFacing(side);
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		getWorld().getBlockState(getPos()).withProperty(BlockStateFacing.facingProperty, facing);
	}

	@Override
	public boolean wrenchCanRemove(EntityPlayer entityPlayer)
	{
		return true;
	}

	@Override
	public float getWrenchDropRate()
	{
		return 1;
	}

	@Override
	public ItemStack getWrenchDrop(EntityPlayer entityPlayer)
	{
		return getBlockType().getDrops(getWorld(), getPos(), getWorld().getBlockState(getPos()), 0).get(0);
	}

}
