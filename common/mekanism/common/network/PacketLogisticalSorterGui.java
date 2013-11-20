package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.api.Object3D;
import mekanism.client.gui.GuiTItemStackFilter;
import mekanism.client.gui.GuiLogisticalSorter;
import mekanism.client.gui.GuiTOreDictFilter;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.tileentity.TileEntityContainerBlock;
import mekanism.common.tileentity.TileEntityLogisticalSorter;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketLogisticalSorterGui implements IMekanismPacket
{
	public Object3D object3D;
	
	public SorterGuiPacket packetType;
	
	public int type;
	
	public int windowId = -1;
	
	public int index = -1;
	
	@Override
	public String getName()
	{
		return "LogisticalSorterGui";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		packetType = (SorterGuiPacket)data[0];
		
		object3D = (Object3D)data[1];
		type = (Integer)data[2];

		if(packetType == SorterGuiPacket.CLIENT)
		{
			windowId = (Integer)data[3];
		}
		else if(packetType == SorterGuiPacket.SERVER_INDEX)
		{
			index = (Integer)data[3];
		}
		else if(packetType == SorterGuiPacket.CLIENT_INDEX)
		{
			windowId = (Integer)data[3];
			index = (Integer)data[4];
		}
		
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
		packetType = SorterGuiPacket.values()[dataStream.readInt()];
		
		object3D = new Object3D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
		
		type = dataStream.readInt();
		
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
				openServerGui(packetType, type, worldServer, (EntityPlayerMP)player, object3D, index);
			}
		}
		else {
			if(object3D.getTileEntity(world) instanceof TileEntityLogisticalSorter)
			{
				try {
					if(packetType == SorterGuiPacket.CLIENT)
					{
						FMLCommonHandler.instance().showGuiScreen(getGui(packetType, type, player, world, object3D.xCoord, object3D.yCoord, object3D.zCoord, -1));
					}
					else if(packetType == SorterGuiPacket.CLIENT_INDEX)
					{
						FMLCommonHandler.instance().showGuiScreen(getGui(packetType, type, player, world, object3D.xCoord, object3D.yCoord, object3D.zCoord, index));
					}
					
					player.openContainer.windowId = windowId;
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void openServerGui(SorterGuiPacket t, int guiType, World world, EntityPlayerMP playerMP, Object3D obj, int i)
	{
		Container container = null;
		
        playerMP.closeScreen();
		
		if(guiType == 0)
		{
			container = new ContainerNull(playerMP, (TileEntityContainerBlock)obj.getTileEntity(world));
		}
		else if(guiType == 1 || guiType == 2)
		{
			container = new ContainerFilter(playerMP.inventory, (TileEntityContainerBlock)obj.getTileEntity(world));
		}
		
        playerMP.incrementWindowID();
        int window = playerMP.currentWindowId;
        
        if(t == SorterGuiPacket.SERVER)
        {
        	PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketLogisticalSorterGui().setParams(SorterGuiPacket.CLIENT, obj, guiType, window), playerMP);
        }
        else if(t == SorterGuiPacket.SERVER_INDEX)
        {
        	PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketLogisticalSorterGui().setParams(SorterGuiPacket.CLIENT_INDEX, obj, guiType, window, i), playerMP);
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
			return new GuiLogisticalSorter(player, (TileEntityLogisticalSorter)world.getBlockTileEntity(x, y, z));
		}
		else {
			if(packetType == SorterGuiPacket.CLIENT)
			{
				if(type == 1)
				{
					return new GuiTItemStackFilter(player, (TileEntityLogisticalSorter)world.getBlockTileEntity(x, y, z));
				}
				else if(type == 2)
				{
					return new GuiTOreDictFilter(player, (TileEntityLogisticalSorter)world.getBlockTileEntity(x, y, z));
				}
			}
			else if(packetType == SorterGuiPacket.CLIENT_INDEX)
			{
				if(type == 1)
				{
					return new GuiTItemStackFilter(player, (TileEntityLogisticalSorter)world.getBlockTileEntity(x, y, z), index);
				}
				else if(type == 2)
				{
					return new GuiTOreDictFilter(player, (TileEntityLogisticalSorter)world.getBlockTileEntity(x, y, z), index);
				}
			}
		}
		
		return null;
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(packetType.ordinal());
		
		dataStream.writeInt(object3D.xCoord);
		dataStream.writeInt(object3D.yCoord);
		dataStream.writeInt(object3D.zCoord);
		
		dataStream.writeInt(object3D.dimensionId);
		
		dataStream.writeInt(type);
		
		if(packetType == SorterGuiPacket.CLIENT || packetType == SorterGuiPacket.CLIENT_INDEX)
		{
			dataStream.writeInt(windowId);
		}
		
		if(packetType == SorterGuiPacket.SERVER_INDEX || packetType == SorterGuiPacket.CLIENT_INDEX)
		{
			dataStream.writeInt(index);
		}
	}
	
	public static enum SorterGuiPacket
	{
		SERVER, CLIENT, SERVER_INDEX, CLIENT_INDEX
	}
}
