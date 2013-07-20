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
	 * Gets the FluidNetwork currently in use by this cable segment.
	 * @return FluidNetwork this cable is using
	 */
	public FluidNetwork getNetwork();
	
	/**
	 * Sets this cable segment's FluidNetwork to a new value.
	 * @param network - FluidNetwork to set to
	 */
	public void setNetwork(FluidNetwork network);
	
	/**
	 * Refreshes the cable's FluidNetwork.
	 */
	public void refreshNetwork();
}
