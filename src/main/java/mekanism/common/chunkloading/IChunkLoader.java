package mekanism.common.chunkloading;

import net.minecraftforge.common.ForgeChunkManager.Ticket;

public interface IChunkLoader
{
	public void forceChunks(Ticket ticket);
}
