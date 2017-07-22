package mekanism.common.integration.multipart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.netty.buffer.ByteBuf;
import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.slot.EnumFaceSlot;
import mcmultipart.api.slot.IPartSlot;
import mcmultipart.api.world.IMultipartBlockAccess;
import mekanism.common.base.ITileNetwork;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

public class MultipartTileNetworkJoiner implements ITileNetwork
{
	private final HashMap<Byte, ITileNetwork> tileSideMap;  
	
	public MultipartTileNetworkJoiner(List<ITileNetwork> tileList)
	{
		tileSideMap = new HashMap<>();
		IMultipartContainer container = null;
		
		TileEntity first = (TileEntity)tileList.get(0);
		IBlockAccess world = first.getWorld();
		if(world instanceof IMultipartBlockAccess)
		{
			container = ((IMultipartBlockAccess)world).getPartInfo().getContainer();
		}
		else
		{
			TileEntity worldTile = first.getWorld().getTileEntity(first.getPos());
			if(worldTile instanceof IMultipartContainer)
			{
				container = (IMultipartContainer)worldTile;
			}
		}
		
		if(container != null)
		{
			for(IPartSlot slot : container.getParts().keySet())
			{
				int tileIndex = tileList.indexOf(container.getPartTile(slot).get().getTileEntity());
				if(tileIndex >= 0)
				{
					byte slotValue = slot instanceof EnumFaceSlot ? (byte)((EnumFaceSlot)slot).ordinal() : 6;
					tileSideMap.put(slotValue, tileList.get(tileIndex));
				}
			}
		}
	}
	
	@Override
	public void handlePacketData(ByteBuf dataStream) throws Exception
	{
		while(dataStream.readableBytes() > 0)
		{
			dataStream.markReaderIndex();
			byte side = dataStream.readByte();
			dataStream.resetReaderIndex();
			
			ITileNetwork networkTile = tileSideMap.get(side);
			if(networkTile == null)
			{
				break;
			}
			
			networkTile.handlePacketData(dataStream);
		}
	}

	@Override
	public ArrayList<Object> getNetworkedData(ArrayList<Object> data)
	{
		for(byte slotValue : tileSideMap.keySet())
		{
			tileSideMap.get(slotValue).getNetworkedData(data);
		}
		
		return data;
	}
}
