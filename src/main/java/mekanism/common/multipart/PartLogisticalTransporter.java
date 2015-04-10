package mekanism.common.multipart;

import java.util.ArrayList;
import java.util.Collection;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Range4D;
import mekanism.common.base.ITransporterTile;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.InventoryNetwork;
import mekanism.common.Mekanism;
import mekanism.common.Tier;
import mekanism.common.Tier.TransporterTier;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.content.transporter.InvStack;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.network.PacketDataRequest.DataRequestMessage;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import io.netty.buffer.ByteBuf;

import codechicken.lib.vec.Vector3;

public class PartLogisticalTransporter extends PartTransmitter<IInventory, InventoryNetwork> implements ITransporterTile
{
	public Tier.TransporterTier tier = Tier.TransporterTier.BASIC;

	public static TransmitterIcons transporterIcons = new TransmitterIcons(8, 16);

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
	public String getType()
	{
		return "mekanism:logistical_transporter_" + tier.name().toLowerCase();
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

	public static void registerIcons(IIconRegister register)
	{
		transporterIcons.registerCenterIcons(register, new String[] {"LogisticalTransporterBasic", "LogisticalTransporterAdvanced", "LogisticalTransporterElite", "LogisticalTransporterUltimate", "RestrictiveTransporter", 
				"DiversionTransporter", "LogisticalTransporterGlass", "LogisticalTransporterGlassColored"});
		transporterIcons.registerSideIcons(register, new String[] {"LogisticalTransporterVerticalBasic", "LogisticalTransporterVerticalAdvanced", "LogisticalTransporterVerticalElite", "LogisticalTransporterVerticalUltimate", 
				"LogisticalTransporterHorizontalBasic", "LogisticalTransporterHorizontalAdvanced", "LogisticalTransporterHorizontalElite", "LogisticalTransporterHorizontalUltimate", "RestrictiveTransporterVertical", 
				"RestrictiveTransporterHorizontal", "LogisticalTransporterVerticalGlass", "LogisticalTransporterVerticalGlassColored", "LogisticalTransporterHorizontalGlass", "LogisticalTransporterHorizontalGlassColored",
				"DiversionTransporterVertical", "DiversionTransporterHorizontal"});
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float f, int pass)
	{
		if(pass == 0)
		{
			RenderPartTransmitter.getInstance().renderContents(this, f, pos);
		}
	}
	
	@Override
	public void onWorldSeparate()
	{
		super.onWorldSeparate();
		
		if(!world().isRemote)
		{
			PathfinderCache.onChanged(Coord4D.get(tile()));
		}
	}
	
	@Override
	protected boolean isValidTransmitter(TileEntity tileEntity)
	{
		ILogisticalTransporter transporter = ((ITransporterTile)tileEntity).getTransmitter();

		if(getTransmitter().getColor() == null || transporter.getColor() == null || getTransmitter().getColor() == transporter.getColor())
		{
			return super.isValidTransmitter(tileEntity);
		}
		
		return false;
	}

	@Override
	public IIcon getCenterIcon(boolean opaque)
	{
		return transporterIcons.getCenterIcon(opaque ? tier.ordinal() : (getTransmitter().color != null ? 7 : 6));
	}

	@Override
	public IIcon getSideIcon(boolean opaque)
	{
		return transporterIcons.getSideIcon(opaque ? tier.ordinal() : (getTransmitter().color != null ? 11 : 10));
	}

	@Override
	public IIcon getSideIconRotated(boolean opaque)
	{
		return transporterIcons.getSideIcon(opaque ? 4+tier.ordinal() : (getTransmitter().color != null ? 13 : 12));
	}

	@Override
	public boolean isValidAcceptor(TileEntity tile, ForgeDirection side)
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

			for(ForgeDirection side : getConnections(ConnectionType.PULL))
			{
				TileEntity tile = Coord4D.get(tile()).getFromSide(side).getTileEntity(world());

				if(tile instanceof IInventory)
				{
					IInventory inv = (IInventory)tile;
					InvStack stack = InventoryUtils.takeTopItem(inv, side.ordinal(), tier.pullAmount);

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

		if(world().isRemote)
		{
			Mekanism.packetHandler.sendToServer(new DataRequestMessage(Coord4D.get(tile())));
		}
		else {
			PathfinderCache.onChanged(Coord4D.get(tile()));
		}
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
				tile().markRender();
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

	@Override
	public ArrayList getNetworkedData(ArrayList data)
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

	public ArrayList getSyncPacket(TransporterStack stack, boolean kill)
	{
		ArrayList data = new ArrayList();

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
	public void load(NBTTagCompound nbtTags)
	{
		super.load(nbtTags);
		
		tier = TransporterTier.values()[nbtTags.getInteger("tier")];

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
				TransporterManager.add(stack);
			}
		}
	}

	@Override
	public void save(NBTTagCompound nbtTags)
	{
		super.save(nbtTags);
		
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
	}

	@Override
	protected boolean onConfigure(EntityPlayer player, int part, int side)
	{
		TransporterUtils.incrementColor(getTransmitter());
		refreshConnections();
		notifyTileChange();
		PathfinderCache.onChanged(Coord4D.get(tile()));
		Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tile()), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(tile())));
		player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " " + MekanismUtils.localize("tooltip.configurator.toggleColor") + ": " + (getTransmitter().getColor() != null ? getTransmitter().getColor().getName() : EnumColor.BLACK + MekanismUtils.localize("gui.none"))));

		return true;
	}

	@Override
	public boolean onRightClick(EntityPlayer player, int side)
	{
		super.onRightClick(player, side);
		player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " " + MekanismUtils.localize("tooltip.configurator.viewColor") + ": " + (getTransmitter().getColor() != null ? getTransmitter().getColor().getName() : "None")));
		return true;
	}

	@Override
	public EnumColor getRenderColor(boolean post)
	{
		return post ? null : getTransmitter().getColor();
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

		if(!world().isRemote)
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
	public MultipartTransporter getTransmitter()
	{
		return (MultipartTransporter)transmitterDelegate;
	}

	public double getCost()
	{
		return 5.D / tier.speed;
	}
}
