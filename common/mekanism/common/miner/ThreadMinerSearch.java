package mekanism.common.miner;

import mekanism.common.tileentity.TileEntityDigitalMiner;

public class ThreadMinerSearch extends Thread
{
	public TileEntityDigitalMiner tileEntity;
	
	public boolean finished = false;
	
	public ThreadMinerSearch(TileEntityDigitalMiner tile)
	{
		tileEntity = tile;
	}
	
	@Override
	public void run()
	{
		if(tileEntity.isInvalid())
		{
			return;
		}
	}
	
	public void reset()
	{
		finished = false;
	}
}
