package mekanism.common.miner;

import mekanism.common.tileentity.TileEntityDigitalMiner;

public class ThreadMinerSearch extends Thread
{
	public TileEntityDigitalMiner tileEntity;
	
	public State state = State.IDLE;
	
	public ThreadMinerSearch(TileEntityDigitalMiner tile)
	{
		tileEntity = tile;
	}
	
	@Override
	public void run()
	{
		state = State.SEARCHING;
		
		if(tileEntity.isInvalid())
		{
			return;
		}
		
		state = State.FINISHED;
	}
	
	public void reset()
	{
		state = State.IDLE;
	}
	
	public static enum State
	{
		IDLE("Not ready"), 
		SEARCHING("Searching"), 
		FINISHED("Ready");
		
		public String desc;
		
		private State(String s)
		{
			desc = s;
		}
	}
}
