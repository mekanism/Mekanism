package mekanism.common;

import mekanism.api.ITransmitter;
import net.minecraftforge.fluids.FluidStack;

/**
 * Implement this in your TileEntity class if the block can transfer fluid as a Mechanical Pipe.
 * @author AidanBrady
 *
 */
public interface IMechanicalPipe extends ITransmitter<FluidNetwork>
{
	/**
	 * Called when fluid is transferred through this pipe.
	 * @param fluidStack - the fluid transferred
	 */
	public void onTransfer(FluidStack fluidStack);
}
