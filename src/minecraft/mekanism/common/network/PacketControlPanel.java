package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.api.Object3D;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketControlPanel implements IMekanismPacket
{
	public String modClass;
	public String modInstance;
	
	public Object3D object3D;
	
	public int guiId;
	
	public PacketControlPanel(String mClass, String mInstance, Object3D obj, int id)
	{
		modClass = mClass;
		modInstance = mInstance;
		
		object3D = obj;
		
		guiId = id;
	}
	
	public PacketControlPanel() {}
	
	@Override
	public String getName() 
	{
		return "ControlPanel";
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		String modClass = dataStream.readUTF();
		String modInstance = dataStream.readUTF();
		
		int x = dataStream.readInt();
		int y = dataStream.readInt();
		int z = dataStream.readInt();
		
		int guiId = dataStream.readInt();
		
		Class mod = Class.forName(modClass);
		
		if(mod == null)
		{
			System.err.println("[Mekanism] Incorrectly implemented IAccessibleGui -- ignoring handler packet.");
			System.err.println(" ~ Unable to locate class '" + modClass + ".'");
			System.err.println(" ~ GUI Container may not function correctly.");
			return;
		}
		
		Object instance = mod.getField(modInstance).get(null);
		
		if(instance == null)
		{
			System.err.println("[Mekanism] Incorrectly implemented IAccessibleGui -- ignoring handler packet.");
			System.err.println(" ~ Unable to locate instance object '" + modInstance + ".'");
			System.err.println(" ~ GUI Container may not function correctly.");
			return;
		}
		
		player.openGui(instance, guiId, world, x, y, z);
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception 
	{
		dataStream.writeUTF(modClass);
		dataStream.writeUTF(modInstance);
		
		dataStream.writeInt(object3D.xCoord);
		dataStream.writeInt(object3D.yCoord);
		dataStream.writeInt(object3D.zCoord);
		
		dataStream.writeInt(guiId);
	}
}
