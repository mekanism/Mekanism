package mekanism.client;

import java.util.ArrayList;

import mekanism.common.IMechanicalPipe;
import mekanism.common.PipeUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;

public class FluidClientUpdate
{
	/** List of iterated pipes, to prevent infinite loops. */
	public ArrayList<TileEntity> iteratedPipes = new ArrayList<TileEntity>();
	
	/** Pointer pipe of this calculation */
	public TileEntity pointer;
	
	/** Type of fluid to distribute */
	public FluidStack fluidToSend;

	public FluidClientUpdate(TileEntity head, FluidStack fluid)
	{
		pointer = head;
		fluidToSend = fluid;
	}

	public void loopThrough(TileEntity tile)
	{
		if(!iteratedPipes.contains(tile))
		{
			iteratedPipes.add(tile);
		}
		
		TileEntity[] pipes = PipeUtils.getConnectedPipes(tile);
		
		for(TileEntity pipe : pipes)
		{
			if(pipe != null)
			{
				if(!iteratedPipes.contains(pipe))
				{
					loopThrough(pipe);
				}
			}
		}
	}

	public void clientUpdate()
	{
		loopThrough(pointer);
		
		for(TileEntity tileEntity : iteratedPipes)
		{
			if(tileEntity instanceof IMechanicalPipe)
			{
				((IMechanicalPipe)tileEntity).onTransfer(fluidToSend);
			}
		}
	}
}
