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

/**
 * Used to route {@link ITileNetwork} packets sent to multipart containers with more than one possible recipient.<br>
 * <br>
 * When MCMP is enabled single byte EnumFacing ordinal headers are added to packets sent by glow panels and
 * transmitters that are then used by this class to route packets to the part attached to the appropriate side.<br>
 * <br>
 * In this case, since transmitters do not attach to a side and therefore have no matching EnumFacing the special
 * value 6 is used to represent the center slot.
 */
public class MultipartTileNetworkJoiner implements ITileNetwork
{
	private final HashMap<Byte, ITileNetwork> tileSideMap;  
	
	 /**
	 * Called by MCMP's multipart container when more than one part implements {@link ITileNetwork}.<br>
	 * <br>
	 * Builds an internal map of part slots to {@link ITileNetwork} implementations in order to route packets.
	 * @param tileList A list of the tile entities that implement {@link ITileNetwork} in the container.
	 */
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
