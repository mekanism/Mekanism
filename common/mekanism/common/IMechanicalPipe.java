package mekanism.common;

import net.minecraftforge.fluids.FluidStack;

/**
 * Implement this in your TileEntity class if the block can transfer fluid as a Mechanical Pipe.
 * @author AidanBrady
 *
 */
public interface IMechanicalPipe 
{
	/**
	 * Called when fluid is transferred through this pipe.
	 * @param fluidStack - the fluid transferred
	 */
	public void onTransfer(FluidStack fluidStack);
	
	/**
	 * Gets the FluidNetwork currently in use by this pipe segment.
	 * @return FluidNetwork this pipe is using
	 */
	public FluidNetwork getNetwork();
	
	/**
	 * Gets the FluidNetwork currently in use by this pipe segment.
	 * @param createIfNull - If true, the pipe will try and connect to an
	 * adjacent network, merging several if necessary, or creating a new one
	 * if none is available
	 * @return FluidNetwork this pipe is using
	 */
	public FluidNetwork getNetwork(boolean createIfNull);
	
	/**
	 * Sets this pipe segment's FluidNetwork to a new value.
	 * @param network - FluidNetwork to set to
	 */
	public void setNetwork(FluidNetwork network);
	
	/**
	 * Refreshes the pipe's FluidNetwork.
	 */
	public void refreshNetwork();
	
	/**
	 * Remove a pipe from its network.
	 */
	public void removeFromNetwork();

	/**
	 * Call this if you're worried a pipe's network is messed up and you want
	 * it to try and fix itself.
	 */
	public void fixNetwork();
}
