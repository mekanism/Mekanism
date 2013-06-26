package mekanism.common;

import net.minecraftforge.liquids.LiquidStack;

/**
 * Implement this in your TileEntity class if the block can transfer liquid as a Mechanical Pipe.
 * @author AidanBrady
 *
 */
public interface IMechanicalPipe 
{
	/**
	 * Whether or not this pipe can transfer liquids.
	 * @return if the pipe can transfer liquids
	 */
	public boolean canTransferLiquids();
	
	/**
	 * Called when liquid is transferred through this pipe.
	 * @param liquidStack - the liquid transferred
	 */
	public void onTransfer(LiquidStack liquidStack);
	
	/**
	 * Gets the LiquidNetwork currently in use by this cable segment.
	 * @return LiquidNetwork this cable is using
	 */
	public LiquidNetwork getNetwork();
	
	/**
	 * Sets this cable segment's LiquidNetwork to a new value.
	 * @param network - LiquidNetwork to set to
	 */
	public void setNetwork(LiquidNetwork network);
	
	/**
	 * Refreshes the cable's LiquidNetwork.
	 */
	public void refreshNetwork();
}
