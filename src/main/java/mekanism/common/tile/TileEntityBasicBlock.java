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
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.frequency.IFrequencyHandler;
import mekanism.common.network.PacketDataRequest.DataRequestMessage;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.security.ISecurityTile;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;

@Interface(iface = "ic2.api.tile.IWrenchable", modid = "IC2")
public abstract class TileEntityBasicBlock extends TileEntity implements IWrenchable, ITileNetwork, IChunkLoadHandler, IFrequencyHandler, ITickable
{
	/** The direction this block is facing. */
	public EnumFacing facing = EnumFacing.NORTH;

	public EnumFacing clientFacing = facing;

	public HashSet<EntityPlayer> openedThisTick = new HashSet<EntityPlayer>();

	/** The players currently using this block. */
	public HashSet<EntityPlayer> playersUsing = new HashSet<EntityPlayer>();

	/** A timer used to send packets to clients. */
	public int ticker;

	public boolean redstone = false;
	public boolean redstoneLastTick = false;

	public boolean doAutoSync = true;

	public List<ITileComponent> components = new ArrayList<ITileComponent>();

	public void update()
	{
		if(!worldObj.isRemote && general.destroyDisabledBlocks)
		{
			MachineType type = BlockStateMachine.MachineType.get(getBlockType(), getBlockMetadata());
			
			if(type != null && !type.isEnabled())
			{
				Mekanism.logger.info("[Mekanism] Destroying machine of type '" + type.machineName + "' at coords " + Coord4D.get(this) + " as according to config.");
				worldObj.setBlockToAir(getPos());
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
					Mekanism.packetHandler.sendTo(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList<Object>())), (EntityPlayerMP)player);
				}
			}
		}

		ticker++;
		redstoneLastTick = redstone;
	}
	
	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		
		onAdded();
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
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			facing = EnumFacing.getFront(dataStream.readInt());
			redstone = dataStream.readBoolean();
	
			if(clientFacing != facing)
			{
				MekanismUtils.updateBlock(worldObj, getPos());
				worldObj.notifyNeighborsOfStateChange(getPos(), worldObj.getBlockState(getPos()).getBlock());
				clientFacing = facing;
			}
	
			for(ITileComponent component : components)
			{
				component.read(dataStream);
			}
		}
	}

	@Override
	public ArrayList<Object> getNetworkedData(ArrayList<Object> data)
	{
		data.add(facing == null ? -1 : facing.ordinal());
		data.add(redstone);

		for(ITileComponent component : components)
		{
			component.write(data);
		}

		return data;
	}
	
	@Override
	public void invalidate()
	{
		super.invalidate();
		
		for(ITileComponent component : components)
		{
			component.invalidate();
		}
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

		facing = EnumFacing.getFront(nbtTags.getInteger("facing"));
		redstone = nbtTags.getBoolean("redstone");

		for(ITileComponent component : components)
		{
			component.read(nbtTags);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setInteger("facing", facing == null ? -1 : facing.ordinal());
		nbtTags.setBoolean("redstone", redstone);

		for(ITileComponent component : components)
		{
			component.write(nbtTags);
		}
		
		return nbtTags;
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		return capability == Capabilities.TILE_NETWORK_CAPABILITY || super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(capability == Capabilities.TILE_NETWORK_CAPABILITY)
			return (T)this;
		return super.getCapability(capability, facing);
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
		return (short)facing.ordinal();
	}

	@Override
	public void setFacing(short direction)
	{
		if(canSetFacing(direction))
		{
			facing = EnumFacing.getFront(direction);
		}

		if(!(facing == clientFacing || worldObj.isRemote))
		{
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList<Object>())), new Range4D(Coord4D.get(this)));
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
		return getBlockType().getPickBlock(worldObj.getBlockState(getPos()), null, worldObj, getPos(), entityPlayer);
	}

	public boolean isPowered()
	{
		return redstone;
	}

	public boolean wasPowered()
	{
		return redstoneLastTick;
	}
	
	public void onPowerChange() {}

	public void onNeighborChange(Block block)
	{
		if(!worldObj.isRemote)
		{
			updatePower();
		}
	}
	
	private void updatePower()
	{
		boolean power = worldObj.isBlockIndirectlyGettingPowered(getPos()) > 0;

		if(redstone != power)
		{
			redstone = power;
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList<Object>())), new Range4D(Coord4D.get(this)));
		
			onPowerChange();
		}
	}
	
	/**
	 * Called when block is placed in world
	 */
	public void onAdded() 
	{
		updatePower();
	}
	
	@Override
	public Frequency getFrequency(FrequencyManager manager)
	{
		if(manager == Mekanism.securityFrequencies && this instanceof ISecurityTile)
		{
			return ((ISecurityTile)this).getSecurity().getFrequency();
		}
		
		return null;
	}
}
