package mekanism.common.multipart;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Range4D;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.HashList;
import mekanism.common.ILogisticalTransporter;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketDataRequest.DataRequestMessage;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.transporter.InvStack;
import mekanism.common.transporter.TransporterManager;
import mekanism.common.transporter.TransporterStack;
import mekanism.common.transporter.TransporterStack.Path;
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
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.PipeWire;
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Interface(iface = "buildcraft.api.transport.IPipeTile", modid = "BuildCraftAPI|transport")
public class PartLogisticalTransporter extends PartSidedPipe implements ILogisticalTransporter, IPipeTile
{
	public static TransmitterIcons transporterIcons = new TransmitterIcons(3, 2);

	public static final int SPEED = 5;

	public EnumColor color;

	public int pullDelay = 0;

	public HashList<TransporterStack> transit = new HashList<TransporterStack>();

	public Set<TransporterStack> needsSync = new HashSet<TransporterStack>();

	@Override
	public String getType()
	{
		return "mekanism:logistical_transporter";
	}

	@Override
	public TransmitterType getTransmitter()
	{
		return TransmitterType.LOGISTICAL_TRANSPORTER;
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.ITEM;
	}

	public static void registerIcons(IIconRegister register)
	{
		transporterIcons.registerCenterIcons(register, new String[] {"LogisticalTransporter", "RestrictiveTransporter", "DiversionTransporter"});
		transporterIcons.registerSideIcons(register, new String[] {"LogisticalTransporterSide", "RestrictiveTransporterSide"});
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
	public boolean canConnect(ForgeDirection side)
	{
		testingSide = side;
		boolean unblocked = tile().canReplacePart(this, this);
		testingSide = null;
		return unblocked;
	}

	@Override
	public byte getPossibleTransmitterConnections()
	{
		byte connections = 0x00;

		if(world().isBlockIndirectlyGettingPowered(x(), y(), z()))
		{
			return connections;
		}

		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			if(canConnectMutual(side))
			{
				TileEntity tileEntity = Coord4D.get(tile()).getFromSide(side).getTileEntity(world());

				if(TransmissionType.checkTransmissionType(tileEntity, getTransmitter().getTransmission()))
				{
					ILogisticalTransporter transporter = (ILogisticalTransporter)tileEntity;

					if(getColor() == null || transporter.getColor() == null || getColor() == transporter.getColor())
					{
						connections |= 1 << side.ordinal();
					}
				}
			}
		}

		return connections;
	}

	@Override
	public byte getPossibleAcceptorConnections()
	{
		byte connections = 0x00;

		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			if(canConnectMutual(side))
			{
				TileEntity tileEntity = Coord4D.get(tile()).getFromSide(side).getTileEntity(world());

				if(isValidAcceptor(tileEntity, side))
				{
					connections |= 1 << side.ordinal();
				}
			}
		}

		return connections;
	}

	@Override
	public IIcon getCenterIcon()
	{
		return transporterIcons.getCenterIcon(0);
	}

	@Override
	public IIcon getSideIcon()
	{
		return transporterIcons.getSideIcon(0);
	}

	@Override
	public boolean isValidAcceptor(TileEntity tile, ForgeDirection side)
	{
		return TransporterUtils.getConnections(this)[side.ordinal()];
	}

	@Override
	public void onModeChange(ForgeDirection side) {}

	@Override
	public void update()
	{
		super.update();

		if(world().isRemote)
		{
			for(TransporterStack stack : transit)
			{
				if(stack != null)
				{
					stack.progress = Math.min(100, stack.progress+SPEED);
				}
			}
		}
		else {
			Set<TransporterStack> remove = new HashSet<TransporterStack>();

			pullItems();

			for(TransporterStack stack : transit)
			{
				if(!stack.initiatedPath)
				{
					if(!recalculate(stack, null))
					{
						remove.add(stack);
						continue;
					}
				}

				stack.progress += SPEED;

				if(stack.progress > 100)
				{
					Coord4D prevSet = null;

					if(stack.hasPath())
					{
						int currentIndex = stack.pathToTarget.indexOf(Coord4D.get(tile()));
						Coord4D next = stack.pathToTarget.get(currentIndex-1);

						if(!stack.isFinal(this))
						{
							if(next != null && stack.canInsertToTransporter(stack.getNext(this).getTileEntity(world()), ForgeDirection.getOrientation(stack.getSide(this))))
							{
								ILogisticalTransporter nextTile = (ILogisticalTransporter)next.getTileEntity(world());
								nextTile.entityEntering(stack);
								remove.add(stack);

								continue;
							}
							else if(next != null)
							{
								prevSet = next;
							}
						}
						else {
							if(stack.pathType != Path.NONE)
							{
								if(next != null && next.getTileEntity(world()) instanceof IInventory)
								{
									needsSync.add(stack);
									IInventory inventory = (IInventory)next.getTileEntity(world());

									if(inventory != null)
									{
										ItemStack rejected = InventoryUtils.putStackInInventory(inventory, stack.itemStack, stack.getSide(this), stack.pathType == Path.HOME);

										if(rejected == null)
										{
											TransporterManager.remove(stack);
											remove.add(stack);
											continue;
										}
										else {
											needsSync.add(stack);
											stack.itemStack = rejected;

											prevSet = next;
										}
									}
								}
							}
						}
					}

					if(!recalculate(stack, prevSet))
					{
						remove.add(stack);
						continue;
					}
					else {
						if(prevSet != null)
						{
							stack.progress = 0;
						}
						else {
							stack.progress = 50;
						}
					}
				}
				else if(stack.progress == 50)
				{
					if(stack.isFinal(this))
					{
						if(stack.pathType == Path.DEST && (!checkSideForInsert(stack) || !InventoryUtils.canInsert(stack.getDest().getTileEntity(world()), stack.color, stack.itemStack, stack.getSide(this), false)))
						{
							if(!recalculate(stack, null))
							{
								remove.add(stack);
								continue;
							}
						}
						else if(stack.pathType == Path.HOME && (!checkSideForInsert(stack) || !InventoryUtils.canInsert(stack.getDest().getTileEntity(world()), stack.color, stack.itemStack, stack.getSide(this), true)))
						{
							if(!recalculate(stack, null))
							{
								remove.add(stack);
								continue;
							}
						}
						else if(stack.pathType == Path.NONE)
						{
							if(!recalculate(stack, null))
							{
								remove.add(stack);
								continue;
							}
						}
					}
					else {
						TileEntity next = stack.getNext(this).getTileEntity(world());
						boolean recalculate = false;

						if(!stack.canInsertToTransporter(next, ForgeDirection.getOrientation(stack.getSide(this))))
						{
							recalculate = true;
						}

						if(recalculate)
						{
							if(!recalculate(stack, null))
							{
								remove.add(stack);
								continue;
							}
						}
					}
				}
			}

			for(TransporterStack stack : remove)
			{
				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tile()), getSyncPacket(stack, true)), new Range4D(Coord4D.get(tile())));
				transit.remove(stack);
				MekanismUtils.saveChunk(tile());
			}

			for(TransporterStack stack : needsSync)
			{
				if(transit.contains(stack))
				{
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tile()), getSyncPacket(stack, false)), new Range4D(Coord4D.get(tile())));
				}
			}

			needsSync.clear();
		}
	}

	private boolean checkSideForInsert(TransporterStack stack)
	{
		ForgeDirection side = ForgeDirection.getOrientation(stack.getSide(this));

		return getConnectionType(side) == ConnectionType.NORMAL || getConnectionType(side) == ConnectionType.PUSH;
	}

	private void pullItems()
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
					InvStack stack = InventoryUtils.takeTopItem(inv, side.getOpposite().ordinal());

					if(stack != null && stack.getStack() != null)
					{
						ItemStack rejects = TransporterUtils.insert(tile, this, stack.getStack(), color, true, 0);

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

	private boolean recalculate(TransporterStack stack, Coord4D from)
	{
		needsSync.add(stack);

		if(stack.pathType != Path.NONE)
		{
			if(!TransporterManager.didEmit(stack.itemStack, stack.recalculatePath(this, 0)))
			{
				if(!stack.calculateIdle(this))
				{
					TransporterUtils.drop(this, stack);
					return false;
				}
			}
		}
		else {
			if(!stack.calculateIdle(this))
			{
				TransporterUtils.drop(this, stack);
				return false;
			}
		}

		if(from != null)
		{
			stack.originalLocation = from;
		}

		return true;
	}

	@Override
	public ItemStack insert(Coord4D original, ItemStack itemStack, EnumColor color, boolean doEmit, int min)
	{
		return insert_do(original, itemStack, color, doEmit, min, false);
	}

	private ItemStack insert_do(Coord4D original, ItemStack itemStack, EnumColor color, boolean doEmit, int min, boolean force)
	{
		ForgeDirection from = Coord4D.get(tile()).sideDifference(original).getOpposite();

		TransporterStack stack = new TransporterStack();
		stack.itemStack = itemStack;
		stack.originalLocation = original;
		stack.homeLocation = original;
		stack.color = color;

		if((force && !canReceiveFrom(original.getTileEntity(world()), from)) || !stack.canInsertToTransporter(tile(), from))
		{
			return itemStack;
		}

		ItemStack rejected = stack.recalculatePath(this, min);

		if(TransporterManager.didEmit(stack.itemStack, rejected))
		{
			stack.itemStack = TransporterManager.getToUse(stack.itemStack, rejected);

			if(doEmit)
			{
				transit.add(stack);
				TransporterManager.add(stack);
				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tile()), getSyncPacket(stack, false)), new Range4D(Coord4D.get(tile())));
				MekanismUtils.saveChunk(tile());
			}

			return rejected;
		}

		return itemStack;
	}

	@Override
	public ItemStack insertRR(TileEntityLogisticalSorter outputter, ItemStack itemStack, EnumColor color, boolean doEmit, int min)
	{
		ForgeDirection from = Coord4D.get(tile()).sideDifference(Coord4D.get(outputter)).getOpposite();

		TransporterStack stack = new TransporterStack();
		stack.itemStack = itemStack;
		stack.originalLocation = Coord4D.get(outputter);
		stack.homeLocation = Coord4D.get(outputter);
		stack.color = color;

		if(!canReceiveFrom(outputter, from) || !stack.canInsertToTransporter(tile(), from))
		{
			return itemStack;
		}

		ItemStack rejected = stack.recalculateRRPath(outputter, this, min);

		if(TransporterManager.didEmit(stack.itemStack, rejected))
		{
			stack.itemStack = TransporterManager.getToUse(stack.itemStack, rejected);

			if(doEmit)
			{
				transit.add(stack);
				TransporterManager.add(stack);
				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tile()), getSyncPacket(stack, false)), new Range4D(Coord4D.get(tile())));
				MekanismUtils.saveChunk(tile());
			}

			return rejected;
		}

		return itemStack;
	}

	@Override
	public void entityEntering(TransporterStack stack)
	{
		stack.progress = 0;
		transit.add(stack);
		Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tile()), getSyncPacket(stack, false)), new Range4D(Coord4D.get(tile())));
		MekanismUtils.saveChunk(tile());
	}

	@Override
	public void onWorldJoin()
	{
		super.onWorldJoin();

		if(world().isRemote)
		{
			Mekanism.packetHandler.sendToServer(new DataRequestMessage(Coord4D.get(tile())));
		}
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		int type = dataStream.readInt();

		if(type == 0)
		{
			int c = dataStream.readInt();

			EnumColor prev = color;

			if(c != -1)
			{
				color = TransporterUtils.colors.get(c);
			}
			else {
				color = null;
			}

			if(prev != color)
			{
				tile().markRender();
			}

			transit.clear();

			int amount = dataStream.readInt();

			for(int i = 0; i < amount; i++)
			{
				transit.add(TransporterStack.readFromPacket(dataStream));
			}
		}
		else if(type == 1)
		{
			boolean kill = dataStream.readBoolean();
			int index = dataStream.readInt();

			if(kill)
			{
				transit.remove(index);
			}
			else {
				TransporterStack stack = TransporterStack.readFromPacket(dataStream);

				if(stack.progress == 0)
				{
					stack.progress = 5;
				}

				transit.replace(index, stack);
			}
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(0);

		if(color != null)
		{
			data.add(TransporterUtils.colors.indexOf(color));
		}
		else {
			data.add(-1);
		}

		data.add(transit.size());

		for(TransporterStack stack : transit)
		{
			stack.write(this, data);
		}

		return data;
	}

	public ArrayList getSyncPacket(TransporterStack stack, boolean kill)
	{
		ArrayList data = new ArrayList();

		data.add(1);
		data.add(kill);
		data.add(transit.indexOf(stack));

		if(!kill)
		{
			stack.write(this, data);
		}

		return data;
	}

	@Override
	public void load(NBTTagCompound nbtTags)
	{
		super.load(nbtTags);

		if(nbtTags.hasKey("color"))
		{
			color = TransporterUtils.colors.get(nbtTags.getInteger("color"));
		}

		if(nbtTags.hasKey("stacks"))
		{
			NBTTagList tagList = nbtTags.getTagList("stacks", NBT.TAG_COMPOUND);

			for(int i = 0; i < tagList.tagCount(); i++)
			{
				TransporterStack stack = TransporterStack.readFromNBT((NBTTagCompound)tagList.getCompoundTagAt(i));

				transit.add(stack);
				TransporterManager.add(stack);
			}
		}
	}

	@Override
	public void save(NBTTagCompound nbtTags)
	{
		super.save(nbtTags);

		if(color != null)
		{
			nbtTags.setInteger("color", TransporterUtils.colors.indexOf(color));
		}

		NBTTagList stacks = new NBTTagList();

		for(TransporterStack stack : transit)
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
	public PipeType getPipeType()
	{
		return PipeType.ITEM;
	}

	@Override
	public int injectItem(ItemStack stack, boolean doAdd, ForgeDirection from)
	{
		if(doAdd)
		{
			TileEntity tile = Coord4D.get(tile()).getFromSide(from).getTileEntity(world());

			ItemStack rejects = TransporterUtils.insert(tile, this, stack, null, true, 0);
			return TransporterManager.getToUse(stack, rejects).stackSize;
		}

		return 0;
	}

	@Override
	public boolean isPipeConnected(ForgeDirection with)
	{
		return true;
	}

	@Override
	protected boolean onConfigure(EntityPlayer player, int part, int side)
	{
		TransporterUtils.incrementColor(this);
		refreshConnections();
		tile().notifyTileChange();
		Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tile()), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(tile())));
		player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " " + MekanismUtils.localize("tooltip.configurator.toggleColor") + ": " + (color != null ? color.getName() : EnumColor.BLACK + MekanismUtils.localize("gui.none"))));

		return true;
	}

	@Override
	public boolean onRightClick(EntityPlayer player, int side)
	{
		super.onRightClick(player, side);
		player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " " + MekanismUtils.localize("tooltip.configurator.viewColor") + ": " + (color != null ? color.getName() : "None")));
		return true;
	}

	@Override
	public EnumColor getColor()
	{
		return color;
	}

	@Override
	public void setColor(EnumColor c)
	{
		color = c;
	}

	@Override
	public TileEntity getTile()
	{
		return tile();
	}

	@Override
	public EnumColor getRenderColor()
	{
		return color;
	}

	@Override
	public boolean canEmitTo(TileEntity tileEntity, ForgeDirection side)
	{
		if(!canConnect(side))
		{
			return false;
		}

		return getConnectionType(side) == ConnectionType.NORMAL || getConnectionType(side) == ConnectionType.PUSH;
	}

	@Override
	public boolean canReceiveFrom(TileEntity tileEntity, ForgeDirection side)
	{
		if(!canConnect(side))
		{
			return false;
		}

		return getConnectionType(side) == ConnectionType.NORMAL;
	}

	@Override
	public void onRemoved()
	{
		super.onRemoved();

		if(!world().isRemote)
		{
			for(TransporterStack stack : transit)
			{
				TransporterUtils.drop(this, stack);
			}
		}
	}

	@Override
	public int getCost()
	{
		return 1;
	}

	@Override
	@Method(modid = "BuildCraftAPI|transport")
	public boolean isWireActive(PipeWire wire)
	{
		return false;
	}
}
