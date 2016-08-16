package mekanism.common.multipart;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Collection;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Range4D;
import mekanism.api.gas.IGasHandler;
import mekanism.api.transmitters.TransmissionType;
import mekanism.api.util.CapabilityUtils;
import mekanism.common.InventoryNetwork;
import mekanism.common.Mekanism;
import mekanism.common.Tier;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.TransporterTier;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.transporter.InvStack;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.util.Constants.NBT;

public class PartLogisticalTransporter extends PartTransmitter<IInventory, InventoryNetwork>
{
	public Tier.TransporterTier tier = Tier.TransporterTier.BASIC;

	public int pullDelay = 0;

	public PartLogisticalTransporter(TransporterTier transporterTier)
	{
		this();
		tier = transporterTier;
	}

	public PartLogisticalTransporter()
	{
		transmitterDelegate = new MultipartTransporter(this);
	}

	@Override
	public ResourceLocation getType()
	{
		return new ResourceLocation("mekanism:logistical_transporter_" + tier.name().toLowerCase());
	}

	@Override
	public TransmitterType getTransmitterType()
	{
		return tier.type;
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.ITEM;
	}

	@Override
	public void onWorldSeparate()
	{
		super.onWorldSeparate();
		
		if(!getWorld().isRemote)
		{
			PathfinderCache.onChanged(new Coord4D(getPos(), getWorld()));
		}
	}
	
	@Override
	public IInventory getCachedAcceptor(EnumFacing side)
	{
		return (IInventory)getCachedTile(side);
	}

	@Override
	protected boolean isValidTransmitter(TileEntity tileEntity)
	{
		ILogisticalTransporter transporter = CapabilityUtils.getCapability(tileEntity, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null);
	
		if(getTransmitter().getColor() == null || transporter.getColor() == null || getTransmitter().getColor() == transporter.getColor())
		{
			return super.isValidTransmitter(tileEntity);
		}
		
		return false;
	}

	@Override
	public boolean isValidAcceptor(TileEntity tile, EnumFacing side)
	{
		return TransporterUtils.isValidAcceptorOnSide(tile, side);
	}
	
	@Override
	public boolean handlesRedstone()
	{
		return false;
	}

	@Override
	public void update()
	{
		super.update();

		getTransmitter().update();
	}

	protected void pullItems()
	{
		if(pullDelay == 0)
		{
			boolean did = false;

			for(EnumFacing side : getConnections(ConnectionType.PULL))
			{
				TileEntity tile = getWorld().getTileEntity(getPos().offset(side));

				if(tile instanceof IInventory)
				{
					IInventory inv = (IInventory)tile;
					InvStack stack = InventoryUtils.takeTopItem(inv, side, tier.pullAmount);

					if(stack != null && stack.getStack() != null)
					{
						ItemStack rejects = TransporterUtils.insert(tile, getTransmitter(), stack.getStack(), getTransmitter().getColor(), true, 0);

						if(TransporterManager.didEmit(stack.getStack(), rejects))
						{
							did = true;
							stack.use(TransporterManager.getToUse(stack.getStack(), rejects).stackSize);
						}
					}
				}
			}

			if(did)
			{
				pullDelay = 10;
			}
		}
		else {
			pullDelay--;
		}
	}

	@Override
	public void onWorldJoin()
	{
		super.onWorldJoin();

		PathfinderCache.onChanged(new Coord4D(getPos(), getWorld()));
	}

	@Override
	public InventoryNetwork createNewNetwork()
	{
		return new InventoryNetwork();
	}

	@Override
	public InventoryNetwork createNetworkByMerging(Collection<InventoryNetwork> networks)
	{
		return new InventoryNetwork(networks);
	}

	@Override
	public void handlePacketData(ByteBuf dataStream) throws Exception
	{
		super.handlePacketData(dataStream);
		
		if(getWorld().isRemote)
		{
			int type = dataStream.readInt();
	
			if(type == 0)
			{
				int c = dataStream.readInt();
	
				EnumColor prev = getTransmitter().getColor();
	
				if(c != -1)
				{
					getTransmitter().setColor(TransporterUtils.colors.get(c));
				}
				else {
					getTransmitter().setColor(null);
				}
	
				if(prev != getTransmitter().getColor())
				{
					markRenderUpdate();
				}
	
				getTransmitter().transit.clear();
	
				int amount = dataStream.readInt();
	
				for(int i = 0; i < amount; i++)
				{
					getTransmitter().transit.add(TransporterStack.readFromPacket(dataStream));
				}
			}
			else if(type == 1)
			{
				boolean kill = dataStream.readBoolean();
				int index = dataStream.readInt();
	
				if(kill)
				{
					getTransmitter().transit.remove(index);
				}
				else {
					TransporterStack stack = TransporterStack.readFromPacket(dataStream);
	
					if(stack.progress == 0)
					{
						stack.progress = 5;
					}
	
					getTransmitter().transit.replace(index, stack);
				}
			}
		}
	}

	@Override
	public ArrayList<Object> getNetworkedData(ArrayList<Object> data)
	{
		super.getNetworkedData(data);
		
		data.add(0);

		if(getTransmitter().getColor() != null)
		{
			data.add(TransporterUtils.colors.indexOf(getTransmitter().getColor()));
		}
		else {
			data.add(-1);
		}

		data.add(getTransmitter().transit.size());

		for(TransporterStack stack : getTransmitter().transit)
		{
			stack.write(getTransmitter(), data);
		}

		return data;
	}

	public ArrayList<Object> getSyncPacket(TransporterStack stack, boolean kill)
	{
		ArrayList<Object> data = new ArrayList<Object>();

		data.add(1);
		data.add(kill);
		data.add(getTransmitter().transit.indexOf(stack));

		if(!kill)
		{
			stack.write(getTransmitter(), data);
		}

		return data;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);
		
		if(nbtTags.hasKey("tier")) tier = TransporterTier.values()[nbtTags.getInteger("tier")];

		if(nbtTags.hasKey("color"))
		{
			getTransmitter().setColor(TransporterUtils.colors.get(nbtTags.getInteger("color")));
		}

		if(nbtTags.hasKey("stacks"))
		{
			NBTTagList tagList = nbtTags.getTagList("stacks", NBT.TAG_COMPOUND);

			for(int i = 0; i < tagList.tagCount(); i++)
			{
				TransporterStack stack = TransporterStack.readFromNBT(tagList.getCompoundTagAt(i));

				getTransmitter().transit.add(stack);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);
		
		nbtTags.setInteger("tier", tier.ordinal());

		if(getTransmitter().getColor() != null)
		{
			nbtTags.setInteger("color", TransporterUtils.colors.indexOf(getTransmitter().getColor()));
		}

		NBTTagList stacks = new NBTTagList();

		for(TransporterStack stack : getTransmitter().transit)
		{
			NBTTagCompound tagCompound = new NBTTagCompound();
			stack.write(tagCompound);
			stacks.appendTag(tagCompound);
		}

		if(stacks.tagCount() != 0)
		{
			nbtTags.setTag("stacks", stacks);
		}
		
		return nbtTags;
	}

	@Override
	protected EnumActionResult onConfigure(EntityPlayer player, int part, EnumFacing side)
	{
		TransporterUtils.incrementColor(getTransmitter());
		onPartChanged(this);
		PathfinderCache.onChanged(new Coord4D(getPos(), getWorld()));
		Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(new Coord4D(getPos(), getWorld()), getNetworkedData(new ArrayList())), new Range4D(new Coord4D(getPos(), getWorld())));
		player.addChatMessage(new TextComponentString(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " " + LangUtils.localize("tooltip.configurator.toggleColor") + ": " + (getTransmitter().getColor() != null ? getTransmitter().getColor().getColoredName() : EnumColor.BLACK + LangUtils.localize("gui.none"))));

		return EnumActionResult.SUCCESS;
	}

	@Override
	public EnumActionResult onRightClick(EntityPlayer player, EnumFacing side)
	{
		super.onRightClick(player, side);
		player.addChatMessage(new TextComponentString(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " " + LangUtils.localize("tooltip.configurator.viewColor") + ": " + (getTransmitter().getColor() != null ? getTransmitter().getColor().getColoredName() : "None")));
		
		return EnumActionResult.SUCCESS;
	}

	@Override
	public EnumColor getRenderColor()
	{
		return getTransmitter().getColor();
	}

	@Override
	public boolean transparencyRender()
	{
		return true;
	}

	@Override
	public void onRemoved()
	{
		super.onRemoved();

		if(!getWorld().isRemote)
		{
			for(TransporterStack stack : getTransmitter().transit)
			{
				TransporterUtils.drop(getTransmitter(), stack);
			}
		}
	}

	@Override
	public int getCapacity()
	{
		return 0;
	}

	@Override
	public Object getBuffer()
	{
		return null;
	}

	@Override
	public void takeShare() {}

    @Override
    public void updateShare() {}

	@Override
	public MultipartTransporter getTransmitter()
	{
		return (MultipartTransporter)transmitterDelegate;
	}

	public double getCost()
	{
		return (double)TransporterTier.ULTIMATE.speed / (double)tier.speed;
	}
	
	@Override
	public boolean upgrade(int tierOrdinal)
	{
		if(tier.ordinal() < BaseTier.ULTIMATE.ordinal() && tierOrdinal == tier.ordinal()+1)
		{
			tier = TransporterTier.values()[tier.ordinal()+1];
			
			markDirtyTransmitters();
			sendDesc = true;
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public IBlockState getExtendedState(IBlockState state)
	{
		return ((IExtendedBlockState)super.getExtendedState(state)).withProperty(ColorProperty.INSTANCE, new ColorProperty(getRenderColor()));
	}
	
	@Override
	public void readUpdatePacket(PacketBuffer packet)
	{
		tier = TransporterTier.values()[packet.readInt()];
		
		super.readUpdatePacket(packet);
	}

	@Override
	public void writeUpdatePacket(PacketBuffer packet)
	{
		packet.writeInt(tier.ordinal());
		
		super.writeUpdatePacket(packet);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing side)
	{
		return capability == Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY || super.hasCapability(capability, side);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing side)
	{
		if(capability == Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY)
		{
			return (T)getTransmitter();
		}
		
		return super.getCapability(capability, side);
	}
}
