package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityMultiblock<T extends SynchronizedData<T>> extends TileEntityContainerBlock implements IMultiblock<T>
{
	/** The multiblock data for this structure. */
	public T structure;
	
	/** Whether or not to send this multiblock's structure in the next update packet. */
	public boolean sendStructure;

	/** This multiblock's previous "has structure" state. */
	public boolean prevStructure;

	/** Whether or not this multiblock has it's structure, for the client side mechanics. */
	public boolean clientHasStructure;
	
	/** Whether or not this multiblock segment is rendering the structure. */
	public boolean isRendering;
	
	/** This multiblock segment's cached data */
	public MultiblockCache cachedData = getNewCache();
	
	/** This multiblock segment's cached inventory ID */
	public String cachedID = null;
	
	public TileEntityMultiblock(String name)
	{
		super(name);
	}
	
	@Override
	public void onUpdate()
	{
		if(worldObj.isRemote)
		{
			if(structure == null)
			{
				structure = getNewStructure();
			}

			if(structure != null && clientHasStructure && isRendering)
			{
				if(!prevStructure)
				{
					Mekanism.proxy.doMultiblockSparkle(this);
				}
			}

			prevStructure = clientHasStructure;
		}

		if(playersUsing.size() > 0 && ((worldObj.isRemote && !clientHasStructure) || (!worldObj.isRemote && structure == null)))
		{
			for(EntityPlayer player : playersUsing)
			{
				System.out.println(worldObj.isRemote + " " + clientHasStructure + " " + structure);
				//player.closeScreen();
			}
		}

		if(!worldObj.isRemote)
		{
			//System.out.println(pos + " " + structure);
			if(structure == null)
			{
				isRendering = false;
				
				if(cachedID != null)
				{
					getManager().updateCache(this);
				}
			}
			
			if(structure == null && ticker == 5)
			{
				doUpdate();
			}

			if(prevStructure == (structure == null))
			{
				if(structure != null && !getSynchronizedData().hasRenderer)
				{
					getSynchronizedData().hasRenderer = true;
					isRendering = true;
					sendStructure = true;
				}

				for(EnumFacing side : EnumFacing.VALUES)
				{
					Coord4D obj = Coord4D.get(this).offset(side);
					TileEntity tile = obj.getTileEntity(worldObj);

					if(!obj.isAirBlock(worldObj) && (tile == null || tile.getClass() != getClass()))
					{
						obj.getBlock(worldObj).onNeighborChange(worldObj, obj.getPos(), getPos());
					}
				}

				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList<Object>())), new Range4D(Coord4D.get(this)));
			}

			prevStructure = structure != null;

			if(structure != null)
			{
				//System.out.println("Whee " + structure + " " + isInvalid());
				getSynchronizedData().didTick = false;

				if(getSynchronizedData().inventoryID != null)
				{
					cachedData.sync(getSynchronizedData());
					cachedID = getSynchronizedData().inventoryID;
					getManager().updateCache(this);
				}
			}
		}
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
		boolean b = super.shouldRefresh(world, pos, oldState, newSate);
		
		if(!world.isRemote)
		{
			System.out.println(b);
		}
		
		return b;
    }
	
	@Override
	public void doUpdate()
	{
		if(!worldObj.isRemote && (structure == null || !getSynchronizedData().didTick))
		{
			getProtocol().doUpdate();

			if(structure != null)
			{
				getSynchronizedData().didTick = true;
			}
		}
	}
	
	public void sendPacketToRenderer()
	{
		if(structure != null)
		{
			for(Coord4D obj : getSynchronizedData().locations)
			{
				TileEntityMultiblock<T> tileEntity = (TileEntityMultiblock<T>)obj.getTileEntity(worldObj);

				if(tileEntity != null && tileEntity.isRendering)
				{
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList<Object>())), new Range4D(Coord4D.get(tileEntity)));
				}
			}
		}
	}
	
	protected abstract T getNewStructure();
	
	public abstract MultiblockCache<T> getNewCache();
	
	protected abstract UpdateProtocol<T> getProtocol();
	
	public abstract MultiblockManager<T> getManager();
	
	@Override
	public ArrayList<Object> getNetworkedData(ArrayList<Object> data)
	{
		super.getNetworkedData(data);

		data.add(isRendering);
		data.add(structure != null);
		
		if(structure != null && isRendering)
		{
			if(sendStructure)
			{
				sendStructure = false;

				data.add(true);

				data.add(getSynchronizedData().volHeight);
				data.add(getSynchronizedData().volWidth);
				data.add(getSynchronizedData().volLength);

				getSynchronizedData().renderLocation.write(data);
				data.add(getSynchronizedData().inventoryID);
			}
			else {
				data.add(false);
			}
		}

		return data;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);

		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			if(structure == null)
			{
				structure = getNewStructure();
			}
	
			isRendering = dataStream.readBoolean();
			clientHasStructure = dataStream.readBoolean();
			
			if(clientHasStructure && isRendering)
			{
				if(dataStream.readBoolean())
				{
					getSynchronizedData().volHeight = dataStream.readInt();
					getSynchronizedData().volWidth = dataStream.readInt();
					getSynchronizedData().volLength = dataStream.readInt();
	
					getSynchronizedData().renderLocation = Coord4D.read(dataStream);
					getSynchronizedData().inventoryID = PacketHandler.readString(dataStream);
				}
			}
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		if(structure == null)
		{
			if(nbtTags.hasKey("cachedID"))
			{
				cachedID = nbtTags.getString("cachedID");
				cachedData.load(nbtTags);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		if(cachedID != null)
		{
			nbtTags.setString("cachedID", cachedID);
			cachedData.save(nbtTags);
		}
	}
	
	@Override
	public ItemStack getStackInSlot(int slotID)
	{
		return structure != null && getSynchronizedData().getInventory() != null ? getSynchronizedData().getInventory()[slotID] : null;
	}

	@Override
	public void setInventorySlotContents(int slotID, ItemStack itemstack)
	{
		if(structure != null && getSynchronizedData().getInventory() != null)
		{
			getSynchronizedData().getInventory()[slotID] = itemstack;

			if(itemstack != null && itemstack.stackSize > getInventoryStackLimit())
			{
				itemstack.stackSize = getInventoryStackLimit();
			}
		}
	}
	
	@Override
	public boolean onActivate(EntityPlayer player)
	{
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}
	
	@Override
	public boolean handleInventory()
	{
		return false;
	}

	@Override
	public T getSynchronizedData()
	{
		return structure;
	}
}
