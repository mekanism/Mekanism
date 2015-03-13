package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.IFilterAccess;
import mekanism.api.Range4D;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISustainedData;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

public class TileEntityOredictionificator extends TileEntityContainerBlock implements IRedstoneControl, IFilterAccess, ISustainedData
{
	public static final int MAX_LENGTH = 24;
	
	public HashList<OredictionificatorFilter> filters = new HashList<OredictionificatorFilter>();
	
	public RedstoneControl controlType = RedstoneControl.DISABLED;
	
	public TileEntityOredictionificator()
	{
		super(MachineType.OREDICTIONIFICATOR.name);
		
		inventory = new ItemStack[2];
		doAutoSync = false;
	}

	@Override
	public void onUpdate()
	{
		if(!worldObj.isRemote)
		{
			if(playersUsing.size() > 0)
			{
				for(EntityPlayer player : playersUsing)
				{
					Mekanism.packetHandler.sendTo(new TileEntityMessage(Coord4D.get(this), getGenericPacket(new ArrayList())), (EntityPlayerMP)player);
				}
			}
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setInteger("controlType", controlType.ordinal());

		NBTTagList filterTags = new NBTTagList();

		for(OredictionificatorFilter filter : filters)
		{
			NBTTagCompound tagCompound = new NBTTagCompound();
			filter.write(tagCompound);
			filterTags.appendTag(tagCompound);
		}

		if(filterTags.tagCount() != 0)
		{
			nbtTags.setTag("filters", filterTags);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];

		if(nbtTags.hasKey("filters"))
		{
			NBTTagList tagList = nbtTags.getTagList("filters", NBT.TAG_COMPOUND);

			for(int i = 0; i < tagList.tagCount(); i++)
			{
				filters.add(OredictionificatorFilter.readFromNBT((NBTTagCompound)tagList.getCompoundTagAt(i)));
			}
		}
	}
	
	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);

		int type = dataStream.readInt();

		if(type == 0)
		{
			controlType = RedstoneControl.values()[dataStream.readInt()];

			filters.clear();

			int amount = dataStream.readInt();

			for(int i = 0; i < amount; i++)
			{
				filters.add(OredictionificatorFilter.readFromPacket(dataStream));
			}
		}
		else if(type == 1)
		{
			controlType = RedstoneControl.values()[dataStream.readInt()];
		}
		else if(type == 2)
		{
			filters.clear();

			int amount = dataStream.readInt();

			for(int i = 0; i < amount; i++)
			{
				filters.add(OredictionificatorFilter.readFromPacket(dataStream));
			}
		}
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(0);

		data.add(controlType.ordinal());

		data.add(filters.size());

		for(OredictionificatorFilter filter : filters)
		{
			filter.write(data);
		}

		return data;
	}

	public ArrayList getGenericPacket(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(1);

		data.add(controlType.ordinal());

		return data;

	}

	public ArrayList getFilterPacket(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(2);

		data.add(filters.size());

		for(OredictionificatorFilter filter : filters)
		{
			filter.write(data);
		}

		return data;
	}
	
	@Override
	public void openInventory()
	{
		if(!worldObj.isRemote)
		{
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getFilterPacket(new ArrayList())), new Range4D(Coord4D.get(this)));
		}
	}
	
	@Override
	public NBTTagCompound getFilterData(NBTTagCompound nbtTags)
	{
		nbtTags.setInteger("controlType", controlType.ordinal());

		NBTTagList filterTags = new NBTTagList();

		for(OredictionificatorFilter filter : filters)
		{
			NBTTagCompound tagCompound = new NBTTagCompound();
			filter.write(tagCompound);
			filterTags.appendTag(tagCompound);
		}

		if(filterTags.tagCount() != 0)
		{
			nbtTags.setTag("filters", filterTags);
		}
		
		return nbtTags;
	}

	@Override
	public void setFilterData(NBTTagCompound nbtTags)
	{
		controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];

		if(nbtTags.hasKey("filters"))
		{
			NBTTagList tagList = nbtTags.getTagList("filters", NBT.TAG_COMPOUND);

			for(int i = 0; i < tagList.tagCount(); i++)
			{
				filters.add(OredictionificatorFilter.readFromNBT((NBTTagCompound)tagList.getCompoundTagAt(i)));
			}
		}
	}

	@Override
	public String getDataType()
	{
		return "tooltip.filterCard.oredictionificator";
	}

	@Override
	public void writeSustainedData(ItemStack itemStack) 
	{
		itemStack.stackTagCompound.setBoolean("hasOredictionificatorConfig", true);

		NBTTagList filterTags = new NBTTagList();

		for(OredictionificatorFilter filter : filters)
		{
			NBTTagCompound tagCompound = new NBTTagCompound();
			filter.write(tagCompound);
			filterTags.appendTag(tagCompound);
		}

		if(filterTags.tagCount() != 0)
		{
			itemStack.stackTagCompound.setTag("filters", filterTags);
		}
	}

	@Override
	public void readSustainedData(ItemStack itemStack) 
	{
		if(itemStack.stackTagCompound.hasKey("hasOredictionificatorConfig"))
		{
			if(itemStack.stackTagCompound.hasKey("filters"))
			{
				NBTTagList tagList = itemStack.stackTagCompound.getTagList("filters", NBT.TAG_COMPOUND);

				for(int i = 0; i < tagList.tagCount(); i++)
				{
					filters.add(OredictionificatorFilter.readFromNBT((NBTTagCompound)tagList.getCompoundTagAt(i)));
				}
			}
		}
	}
	
	@Override
	public RedstoneControl getControlType()
	{
		return controlType;
	}

	@Override
	public void setControlType(RedstoneControl type)
	{
		controlType = type;
	}

	@Override
	public boolean canPulse()
	{
		return true;
	}
	
	public static class OredictionificatorFilter
	{
		public String filter;
		public int index;
		
		public void write(NBTTagCompound nbtTags)
		{
			nbtTags.setString("filter", filter);
			nbtTags.setInteger("index", index);
		}

		protected void read(NBTTagCompound nbtTags)
		{
			filter = nbtTags.getString("filter");
			index = nbtTags.getInteger("index");
		}

		public void write(ArrayList data)
		{
			data.add(filter);
			data.add(index);
		}

		protected void read(ByteBuf dataStream)
		{
			filter = PacketHandler.readString(dataStream);
			index = dataStream.readInt();
		}

		public static OredictionificatorFilter readFromNBT(NBTTagCompound nbtTags)
		{
			OredictionificatorFilter filter = new OredictionificatorFilter();

			filter.read(nbtTags);

			return filter;
		}

		public static OredictionificatorFilter readFromPacket(ByteBuf dataStream)
		{
			OredictionificatorFilter filter = new OredictionificatorFilter();

			filter.read(dataStream);

			return filter;
		}

		@Override
		public int hashCode()
		{
			int code = 1;
			code = 31 * code + filter.hashCode();
			return code;
		}

		@Override
		public boolean equals(Object filter)
		{
			return filter instanceof OredictionificatorFilter && ((OredictionificatorFilter)filter).filter.equals(filter);
		}
	}
}
