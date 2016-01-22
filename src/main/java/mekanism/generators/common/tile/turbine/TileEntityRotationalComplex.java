package mekanism.generators.common.tile.turbine;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityBasicBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityRotationalComplex extends TileEntityBasicBlock
{
	public static Map<String, Float> clientRotationMap = new HashMap<String, Float>();
	
	public static final float ROTATION_THRESHOLD = 0.01F;
	
	public String multiblockUUID;
	public float rotation;
	
	@Override
	public void onUpdate() {}
	
	@Override
	public boolean canUpdate()
	{
		return false;
	}
	
	@Override
	public void onAdded()
	{
		super.onAdded();
		
		if(!worldObj.isRemote)
		{
			setMultiblock("asdf");
			setRotation(1F);
		}
	}
	
	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);
		
		rotation = dataStream.readFloat();
		
		if(dataStream.readBoolean())
		{
			multiblockUUID = PacketHandler.readString(dataStream);
			clientRotationMap.put(multiblockUUID, rotation);
		}
		else {
			multiblockUUID = null;
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(rotation);
		
		if(multiblockUUID != null)
		{
			data.add(true);
			data.add(multiblockUUID);
		}
		else {
			data.add(false);
		}
		
		return data;
	}
	
	public void setMultiblock(String id)
	{
		multiblockUUID = id;
		
		Coord4D coord = Coord4D.get(this).getFromSide(ForgeDirection.DOWN);
		TileEntity tile = coord.getTileEntity(worldObj);
		
		if(tile instanceof TileEntityTurbineRod)
		{
			((TileEntityTurbineRod)tile).updateRods();
		}
	}
	
	public void setRotation(float newRotation)
	{
		if(Math.abs(newRotation-rotation) > ROTATION_THRESHOLD)
		{
			rotation = newRotation;
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
		}
	}
}
