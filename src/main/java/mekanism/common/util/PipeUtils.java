package mekanism.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.transmitters.ITransmitterTile;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public final class PipeUtils
{
	public static final FluidTankInfo[] EMPTY = new FluidTankInfo[] {};

	public static boolean isValidAcceptorOnSide(TileEntity tile, ForgeDirection side)
	{
		if(tile instanceof ITransmitterTile || !(tile instanceof IFluidHandler))
			return false;

		IFluidHandler container = (IFluidHandler)tile;
		FluidTankInfo[] infoArray = container.getTankInfo(side.getOpposite());

		if(container.canDrain(side.getOpposite(), FluidRegistry.WATER)
			|| container.canFill(side.getOpposite(), FluidRegistry.WATER)) //I hesitate to pass null to these.
		{
			return true;
		}
		else if(infoArray != null && infoArray.length > 0)
		{
			for(FluidTankInfo info : infoArray)
			{
				if(info != null)
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets all the acceptors around a tile entity.
	 * @param tileEntity - center tile entity
	 * @return array of IFluidHandlers
	 */
	public static IFluidHandler[] getConnectedAcceptors(TileEntity tileEntity)
	{
		IFluidHandler[] acceptors = new IFluidHandler[] {null, null, null, null, null, null};

		for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity acceptor = Coord4D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.getWorldObj());

			if(acceptor instanceof IFluidHandler && !(acceptor instanceof ITransmitterTile))
			{
				acceptors[orientation.ordinal()] = (IFluidHandler)acceptor;
			}
		}

		return acceptors;
	}
	
	/**
	 * Emits fluid from a central block by splitting the received stack among the sides given.
	 * @param sides - the list of sides to output from
	 * @param stack - the stack to output
	 * @param from - the TileEntity to output from
	 * @return the amount of gas emitted
	 */
	public static int emit(List<ForgeDirection> sides, FluidStack stack, TileEntity from)
	{
		if(stack == null)
		{
			return 0;
		}
		
		List<IFluidHandler> availableAcceptors = new ArrayList<IFluidHandler>();
		IFluidHandler[] possibleAcceptors = getConnectedAcceptors(from);
		
		for(int i = 0; i < possibleAcceptors.length; i++)
		{
			IFluidHandler handler = possibleAcceptors[i];
			
			if(handler != null && handler.canFill(ForgeDirection.getOrientation(i).getOpposite(), stack.getFluid()))
			{
				availableAcceptors.add(handler);
			}
		}

		Collections.shuffle(availableAcceptors);

		int toSend = stack.amount;
		int prevSending = toSend;

		if(!availableAcceptors.isEmpty())
		{
			int divider = availableAcceptors.size();
			int remaining = toSend % divider;
			int sending = (toSend-remaining)/divider;

			for(IFluidHandler acceptor : availableAcceptors)
			{
				int currentSending = sending;

				if(remaining > 0)
				{
					currentSending++;
					remaining--;
				}
				
				ForgeDirection dir = ForgeDirection.getOrientation(Arrays.asList(possibleAcceptors).indexOf(acceptor)).getOpposite();
				toSend -= acceptor.fill(dir, new FluidStack(stack.getFluid(), currentSending), true);
			}
		}

		return prevSending-toSend;
	}
}
