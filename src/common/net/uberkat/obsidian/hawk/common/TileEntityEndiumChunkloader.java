package net.uberkat.obsidian.hawk.common;

import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.uberkat.obsidian.common.ObsidianIngots;

public class TileEntityEndiumChunkloader extends TileEntity
{
	public Ticket heldChunk;

	public void invalidate()
	{
		forceChunkLoading(null);
	}

	public void validate()
	{
		forceChunkLoading(null);
	}
	
	public void forceChunkLoading(Ticket ticket)
	{
		if (ticket != null)
		{
			heldChunk = ticket;
			ForgeChunkManager.forceChunk(heldChunk, new ChunkCoordIntPair(xCoord >> 4, zCoord >> 4));
		}
		else
		{
			if (heldChunk == null)
			{
				Ticket newTicket = ForgeChunkManager.requestTicket(ObsidianIngots.instance, worldObj, Type.NORMAL);
				newTicket.getModData().setInteger("xCoord", xCoord);
				newTicket.getModData().setInteger("yCoord", yCoord);
				newTicket.getModData().setInteger("zCoord", zCoord);
				newTicket.setChunkListDepth(HawksMachinery.CORE.maxChunksLoaded);
				heldChunk = newTicket;
			}
			else
			{
				ForgeChunkManager.releaseTicket(heldChunk);
				heldChunk = null;
			}
		}
	}
}
