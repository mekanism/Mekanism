package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.api.Object3D;
import mekanism.common.Mekanism;
import mekanism.common.tileentity.TileEntityBasicBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLCommonHandler;

public class PacketSimpleGui implements IMekanismPacket
{
	public Object3D object3D;
	
	public int guiId;
	
	@Override
	public String getName()
	{
		return "SimpleGui";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		object3D = (Object3D)data[0];
		guiId = (Integer)data[1];
		
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
		object3D = new Object3D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
		
		guiId = dataStream.readInt();
		
		World worldServer = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(object3D.dimensionId);
		
		if(worldServer != null && object3D.getTileEntity(worldServer) instanceof TileEntityBasicBlock)
		{
			player.closeScreen();
			
			if(guiId == -1)
			{
				return;
			}
			
			player.openGui(Mekanism.instance, guiId, worldServer, object3D.xCoord, object3D.yCoord, object3D.zCoord);
		}
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
