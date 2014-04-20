package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.api.Coord4D;
import mekanism.client.gui.GuiLogisticalSorter;
import mekanism.client.gui.GuiTFilterSelect;
import mekanism.client.gui.GuiTItemStackFilter;
import mekanism.client.gui.GuiTMaterialFilter;
import mekanism.client.gui.GuiTOreDictFilter;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.tile.TileEntityContainerBlock;
import mekanism.common.tile.TileEntityLogisticalSorter;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketLogisticalSorterGui extends MekanismPacket
{
	public Coord4D object3D;

	public SorterGuiPacket packetType;

	public int guiType;

	public int windowId = -1;

	public int index = -1;

	public PacketLogisticalSorterGui(SorterGuiPacket type, Coord4D coord, int guiId, int extra, int extra2)
	{
		packetType = type;

		object3D = coord;
		guiType = guiId;

		if(packetType == SorterGuiPacket.CLIENT)
		{
			windowId = extra;
		}
		else if(packetType == SorterGuiPacket.SERVER_INDEX)
		{
			index = extra;
		}
		else if(packetType == SorterGuiPacket.CLIENT_INDEX)
		{
			windowId = extra2;
			index = extra2;
		}
	}

	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		packetType = SorterGuiPacket.values()[dataStream.readInt()];

		object3D = new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());

		guiType = dataStream.readInt();

		if(packetType == SorterGuiPacket.CLIENT || packetType == SorterGuiPacket.CLIENT_INDEX)
		{
			windowId = dataStream.readInt();
		}

		if(packetType == SorterGuiPacket.SERVER_INDEX || packetType == SorterGuiPacket.CLIENT_INDEX)
		{
			index = dataStream.readInt();
		}

		if(!world.isRemote)
		{
			World worldServer = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(object3D.dimensionId);

			if(worldServer != null && object3D.getTileEntity(worldServer) instanceof TileEntityLogisticalSorter)
			{
				openServerGui(packetType, guiType, worldServer, (EntityPlayerMP)player, object3D, index);
			}
		}
		else {
			if(object3D.getTileEntity(world) instanceof TileEntityLogisticalSorter)
			{
				try {
					if(packetType == SorterGuiPacket.CLIENT)
					{
						FMLCommonHandler.instance().showGuiScreen(getGui(packetType, guiType, player, world, object3D.xCoord, object3D.yCoord, object3D.zCoord, -1));
					}
					else if(packetType == SorterGuiPacket.CLIENT_INDEX)
					{
						FMLCommonHandler.instance().showGuiScreen(getGui(packetType, guiType, player, world, object3D.xCoord, object3D.yCoord, object3D.zCoord, index));
					}

					player.openContainer.windowId = windowId;
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void openServerGui(SorterGuiPacket t, int guiType, World world, EntityPlayerMP playerMP, Coord4D obj, int i)
	{
		Container container = null;

		playerMP.closeContainer();

		if(guiType == 0)
		{
			container = new ContainerNull(playerMP, (TileEntityContainerBlock)obj.getTileEntity(world));
		}
		else if(guiType == 4)
		{
			container = new ContainerNull(playerMP, (TileEntityContainerBlock)obj.getTileEntity(world));
		}
		else if(guiType == 1 || guiType == 2 || guiType == 3)
		{
			container = new ContainerFilter(playerMP.inventory, (TileEntityContainerBlock)obj.getTileEntity(world));
		}

		playerMP.incrementWindowID();
		int window = playerMP.currentWindowId;

		if(t == SorterGuiPacket.SERVER)
		{
			Mekanism.packetPipeline.sendTo(new PacketLogisticalSorterGui(SorterGuiPacket.CLIENT, obj, guiType, window, 0), playerMP);
		}
		else if(t == SorterGuiPacket.SERVER_INDEX)
		{
			Mekanism.packetPipeline.sendTo(new PacketLogisticalSorterGui(SorterGuiPacket.CLIENT_INDEX, obj, guiType, window, i), playerMP);
		}

		playerMP.openContainer = container;
		playerMP.openContainer.windowId = window;
		playerMP.openContainer.addCraftingToCrafters(playerMP);
	}

	@SideOnly(Side.CLIENT)
	public GuiScreen getGui(SorterGuiPacket packetType, int type, EntityPlayer player, World world, int x, int y, int z, int index)
	{
		if(type == 0)
		{
			return new GuiLogisticalSorter(player, (TileEntityLogisticalSorter)world.getTileEntity(x, y, z));
		}
		else if(type == 4)
		{
			return new GuiTFilterSelect(player, (TileEntityLogisticalSorter)world.getTileEntity(x, y, z));
		}
		else {
			if(packetType == SorterGuiPacket.CLIENT)
			{
				if(type == 1)
				{
					return new GuiTItemStackFilter(player, (TileEntityLogisticalSorter)world.getTileEntity(x, y, z));
				}
				else if(type == 2)
				{
					return new GuiTOreDictFilter(player, (TileEntityLogisticalSorter)world.getTileEntity(x, y, z));
				}
				else if(type == 3)
				{
					return new GuiTMaterialFilter(player, (TileEntityLogisticalSorter)world.getTileEntity(x, y, z));
				}
			}
			else if(packetType == SorterGuiPacket.CLIENT_INDEX)
			{
				if(type == 1)
				{
					return new GuiTItemStackFilter(player, (TileEntityLogisticalSorter)world.getTileEntity(x, y, z), index);
				}
				else if(type == 2)
				{
					return new GuiTOreDictFilter(player, (TileEntityLogisticalSorter)world.getTileEntity(x, y, z), index);
				}
				else if(type == 3)
				{
					return new GuiTMaterialFilter(player, (TileEntityLogisticalSorter)world.getTileEntity(x, y, z), index);
				}
			}
		}

		return null;
	}

	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(packetType.ordinal());

		dataStream.writeInt(object3D.xCoord);
		dataStream.writeInt(object3D.yCoord);
		dataStream.writeInt(object3D.zCoord);

		dataStream.writeInt(object3D.dimensionId);

		dataStream.writeInt(guiType);

		if(packetType == SorterGuiPacket.CLIENT || packetType == SorterGuiPacket.CLIENT_INDEX)
		{
			dataStream.writeInt(windowId);
		}

		if(packetType == SorterGuiPacket.SERVER_INDEX || packetType == SorterGuiPacket.CLIENT_INDEX)
		{
			dataStream.writeInt(index);
		}
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
	{

	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
	{

	}

	@Override
	public void handleClientSide(EntityPlayer player)
	{

	}

	@Override
	public void handleServerSide(EntityPlayer player)
	{

	}

	public static enum SorterGuiPacket
	{
		SERVER, CLIENT, SERVER_INDEX, CLIENT_INDEX
	}
}
