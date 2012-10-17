package universalelectricity.prefab.chunk;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.Chunk;
import net.minecraft.src.TileEntity;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkEvent.Load;
import net.minecraftforge.event.world.ChunkEvent.Unload;
import universalelectricity.prefab.Vector2;

public class ChunkEventCaller
{
	public static final ChunkEventCaller INSTANCE = new ChunkEventCaller();

	private static final List<IChunkLoadHandler> HANDLERS = new ArrayList<IChunkLoadHandler>();
	
	public static void register(IChunkLoadHandler handler)
	{
		if(!HANDLERS.contains(handler))
		{
			HANDLERS.add(handler);
		}
	}
	
	public static void remove(IChunkLoadHandler handler)
	{
		if(HANDLERS.contains(handler))
		{
			HANDLERS.remove(handler);
		}
	}
	
	public void cleanUpChunkHandler()
	{
		List<IChunkLoadHandler> removeList = new ArrayList<IChunkLoadHandler>();
	
		for(IChunkLoadHandler handler : HANDLERS)
		{
			if(handler == null)
			{
				removeList.add(handler);
				continue;
			}
			
			if(handler instanceof TileEntity)
			{
				if(((TileEntity)handler).isInvalid())
				{
					removeList.add(handler);
					continue;
				}
			}
		}
			
		HANDLERS.removeAll(removeList);
	}
	
	@ForgeSubscribe
	public void onChunkLoad(Load event)
	{
		this.cleanUpChunkHandler();
		
		for(IChunkLoadHandler handler : HANDLERS)
		{
			Chunk chunk = event.getChunk();
			
			if(handler instanceof TileEntity)
			{
				if(Vector2.isPointInRegion(new Vector2(((TileEntity)handler).xCoord, ((TileEntity)handler).zCoord), new Vector2(chunk.xPosition << 4, chunk.zPosition << 4), new Vector2((chunk.xPosition << 4) + 16, (chunk.zPosition << 4) + 16)))
				{
					handler.onChunkLoad(chunk);
				}
			}
			else
			{
				handler.onChunkLoad(chunk);
			}
		}
	}
	
	@ForgeSubscribe
	public void onChunkUnload(Unload event)
	{
		this.cleanUpChunkHandler();

		for(IChunkLoadHandler handler : HANDLERS)
		{
			Chunk chunk = event.getChunk();
			
			if(handler instanceof TileEntity)
			{
				if(Vector2.isPointInRegion(new Vector2(((TileEntity)handler).xCoord, ((TileEntity)handler).zCoord), new Vector2(chunk.xPosition << 4, chunk.zPosition << 4), new Vector2((chunk.xPosition << 4) + 16, (chunk.zPosition << 4) + 16)))
				{
					handler.onChunkUnload(chunk);
				}
			}
			else
			{
				handler.onChunkUnload(chunk);
			}
		}
	}
}
