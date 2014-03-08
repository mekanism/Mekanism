package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.tile.TileEntityBasicBlock;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketSimpleGui implements IMekanismPacket
{
	public Coord4D object3D;

	public int guiId;

	@Override
	public String getName()
	{
		return "SimpleGui";
	}

	@Override
	public IMekanismPacket setParams(Object... data)
	{
		object3D = (Coord4D)data[0];
		guiId = (Integer)data[1];

		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		object3D = new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());

		guiId = dataStream.readInt();

		if(!world.isRemote)
		{
			World worldServer = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(object3D.dimensionId);

			if(worldServer != null && object3D.getTileEntity(worldServer) instanceof TileEntityBasicBlock)
			{
				if(guiId == -1)
				{
					return;
				}

				openServerGui(guiId, (EntityPlayerMP)player, world, object3D);
			}
		}
		else {
			FMLCommonHandler.instance().showGuiScreen(getGui(guiId, player, world, object3D));
		}
	}

	public static void openServerGui(int id, EntityPlayerMP playerMP, World world, Coord4D obj)
	{
		playerMP.closeContainer();
		playerMP.incrementWindowID();

		int window = playerMP.currentWindowId;

		PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketSimpleGui().setParams(obj, id), playerMP);

		playerMP.openContainer = Mekanism.proxy.getServerGui(id, playerMP, world, obj.xCoord, obj.yCoord, obj.zCoord);
		playerMP.openContainer.windowId = window;
		playerMP.openContainer.addCraftingToCrafters(playerMP);
	}

	@SideOnly(Side.CLIENT)
	public GuiScreen getGui(int id, EntityPlayer player, World world, Coord4D obj)
	{
		return (GuiScreen)Mekanism.proxy.getClientGui(id, player, world, obj.xCoord, obj.yCoord, obj.zCoord);
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(object3D.xCoord);
		dataStream.writeInt(object3D.yCoord);
		dataStream.writeInt(object3D.zCoord);

		dataStream.writeInt(object3D.dimensionId);

		dataStream.writeInt(guiId);
	}
}
