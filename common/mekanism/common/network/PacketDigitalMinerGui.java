package mekanism.common.network;

import java.io.DataOutputStream;
import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.client.gui.GuiDigitalMiner;
import mekanism.client.gui.GuiDigitalMinerConfig;
import mekanism.client.gui.GuiMItemStackFilter;
import mekanism.client.gui.GuiMOreDictFilter;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.inventory.container.ContainerDigitalMiner;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.tile.TileEntityContainerBlock;
import mekanism.common.tile.TileEntityDigitalMiner;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketDigitalMinerGui implements IMekanismPacket
{
	public Coord4D object3D;
	
	public MinerGuiPacket packetType;
	
	public int type;
	
	public int windowId = -1;
	
	public int index = -1;
	
	@Override
	public String getName()
	{
		return "DigitalMinerGui";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		packetType = (MinerGuiPacket)data[0];
		
		object3D = (Coord4D)data[1];
		type = (Integer)data[2];

		if(packetType == MinerGuiPacket.CLIENT)
		{
			windowId = (Integer)data[3];
		}
		else if(packetType == MinerGuiPacket.SERVER_INDEX)
		{
			index = (Integer)data[3];
		}
		else if(packetType == MinerGuiPacket.CLIENT_INDEX)
		{
			windowId = (Integer)data[3];
			index = (Integer)data[4];
		}
		
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
		packetType = MinerGuiPacket.values()[dataStream.readInt()];
		
		object3D = new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
		
		type = dataStream.readInt();
		
		if(packetType == MinerGuiPacket.CLIENT || packetType == MinerGuiPacket.CLIENT_INDEX)
		{
			windowId = dataStream.readInt();
		}
		
		if(packetType == MinerGuiPacket.SERVER_INDEX || packetType == MinerGuiPacket.CLIENT_INDEX)
		{
			index = dataStream.readInt();
		}
		
		if(!world.isRemote)
		{	
			World worldServer = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(object3D.dimensionId);
			
			if(worldServer != null && object3D.getTileEntity(worldServer) instanceof TileEntityDigitalMiner)
			{
				openServerGui(packetType, type, worldServer, (EntityPlayerMP)player, object3D, index);
			}
		}
		else {
			if(object3D.getTileEntity(world) instanceof TileEntityDigitalMiner)
			{
				try {
					if(packetType == MinerGuiPacket.CLIENT)
					{
						FMLCommonHandler.instance().showGuiScreen(getGui(packetType, type, player, world, object3D.xCoord, object3D.yCoord, object3D.zCoord, -1));
					}
					else if(packetType == MinerGuiPacket.CLIENT_INDEX)
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
	
	public static void openServerGui(MinerGuiPacket t, int guiType, World world, EntityPlayerMP playerMP, Coord4D obj, int i)
	{
		Container container = null;
		
		playerMP.closeContainer();
		
		if(guiType == 0)
		{
			container = new ContainerNull(playerMP, (TileEntityContainerBlock)obj.getTileEntity(world));
		}
		else if(guiType == 3)
		{
			container = new ContainerDigitalMiner(playerMP.inventory, (TileEntityDigitalMiner)obj.getTileEntity(world));
		}
		else if(guiType == 1 || guiType == 2)
		{
			container = new ContainerFilter(playerMP.inventory, (TileEntityContainerBlock)obj.getTileEntity(world));
		}
		
        playerMP.incrementWindowID();
        int window = playerMP.currentWindowId;
        
        if(t == MinerGuiPacket.SERVER)
        {
        	PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketDigitalMinerGui().setParams(MinerGuiPacket.CLIENT, obj, guiType, window), playerMP);
        }
        else if(t == MinerGuiPacket.SERVER_INDEX)
        {
        	PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketDigitalMinerGui().setParams(MinerGuiPacket.CLIENT_INDEX, obj, guiType, window, i), playerMP);
        }
        
        playerMP.openContainer = container;
        playerMP.openContainer.windowId = window;
        playerMP.openContainer.addCraftingToCrafters(playerMP);
        
        if(guiType == 0)
        {
        	TileEntityDigitalMiner tile = (TileEntityDigitalMiner)obj.getTileEntity(world);
        	
        	for(EntityPlayer player : tile.playersUsing)
        	{
        		PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(obj, tile.getFilterPacket(new ArrayList())), player);
        	}
        }
	}
	
	@SideOnly(Side.CLIENT)
	public GuiScreen getGui(MinerGuiPacket packetType, int type, EntityPlayer player, World world, int x, int y, int z, int index)
	{
		if(type == 0)
		{
			return new GuiDigitalMinerConfig(player, (TileEntityDigitalMiner)world.getBlockTileEntity(x, y, z));
		}
		else if(type == 3)
		{
			return new GuiDigitalMiner(player.inventory, (TileEntityDigitalMiner)world.getBlockTileEntity(x, y, z));
		}
		else {
			if(packetType == MinerGuiPacket.CLIENT)
			{
				if(type == 1)
				{
					return new GuiMItemStackFilter(player, (TileEntityDigitalMiner)world.getBlockTileEntity(x, y, z));
				}
				else if(type == 2)
				{
					return new GuiMOreDictFilter(player, (TileEntityDigitalMiner)world.getBlockTileEntity(x, y, z));
				}
			}
			else if(packetType == MinerGuiPacket.CLIENT_INDEX)
			{
				if(type == 1)
				{
					return new GuiMItemStackFilter(player, (TileEntityDigitalMiner)world.getBlockTileEntity(x, y, z), index);
				}
				else if(type == 2)
				{
					return new GuiMOreDictFilter(player, (TileEntityDigitalMiner)world.getBlockTileEntity(x, y, z), index);
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
		
		if(packetType == MinerGuiPacket.CLIENT || packetType == MinerGuiPacket.CLIENT_INDEX)
		{
			dataStream.writeInt(windowId);
		}
		
		if(packetType == MinerGuiPacket.SERVER_INDEX || packetType == MinerGuiPacket.CLIENT_INDEX)
		{
			dataStream.writeInt(index);
		}
	}
	
	public static enum MinerGuiPacket
	{
		SERVER, CLIENT, SERVER_INDEX, CLIENT_INDEX
	}
}
