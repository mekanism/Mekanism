package mekanism.common.chunkloading;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class ChunkManager implements LoadingCallback
{
	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world)
	{
		for(Ticket ticket : tickets)
		{
			int x = ticket.getModData().getInteger("x");
			int y = ticket.getModData().getInteger("y");
			int z = ticket.getModData().getInteger("z");
			
			TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
			
			if(tileEntity instanceof IChunkLoader)
			{
				((IChunkLoader)tileEntity).getChunkLoader().refreshChunkSet();
				((IChunkLoader)tileEntity).getChunkLoader().forceChunks(ticket);
			}
		}
	}
}
