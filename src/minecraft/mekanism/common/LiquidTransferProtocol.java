package mekanism.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.FMLCommonHandler;

import mekanism.api.GasTransmission;
import mekanism.api.IGasAcceptor;
import mekanism.api.IGasStorage;
import mekanism.api.IMechanicalPipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;

public class LiquidTransferProtocol
{
	/** List of iterated pipes, to prevent infinite loops. */
	public ArrayList<TileEntity> iteratedPipes = new ArrayList<TileEntity>();
	
	/** List of ITankContainers that can take in the type of liquid requested. */
	public ArrayList<ITankContainer> availableAcceptors = new ArrayList<ITankContainer>();
	
	/** Map of directions liquid is transferred to. */
	public Map<ITankContainer, ForgeDirection> acceptorDirections = new HashMap<ITankContainer, ForgeDirection>();
	
	/** Pointer pipe of this calculation */
	public TileEntity pointer;
	
	/** Original outputter Tile Entity. */
	public TileEntity original;
	
	/** Type of liquid to distribute */
	public LiquidStack liquidToSend;
	
	/**
	 * LiquidTransferProtocol -- a calculation used to distribute liquids through a pipe network.
	 * @param head - pointer tile entity
	 * @param orig - original outputter
	 * @param liquid - the LiquidStack to transfer
	 */
	public LiquidTransferProtocol(TileEntity head, TileEntity orig, LiquidStack liquid)
	{
		pointer = head;
		original = orig;
		liquidToSend = liquid;
	}
	
	/**
	 * Recursive loop that iterates through connected tubes and adds connected acceptors to an ArrayList.  Note that it will NOT add
	 * the original outputting tile into the availableAcceptors list, to prevent loops.
	 * @param tile - pointer tile entity
	 */
	public void loopThrough(TileEntity tile)
	{
		ITankContainer[] acceptors = PipeUtils.getConnectedAcceptors(tile);
		
		for(ITankContainer acceptor : acceptors)
		{
			if(acceptor != null && !availableAcceptors.contains(acceptor))
			{
				ForgeDirection side = ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor)).getOpposite();
				
				if(acceptor != original && !(acceptor instanceof IMechanicalPipe))
				{
					if(acceptor.fill(side, liquidToSend, false) > 0)
					{
						availableAcceptors.add(acceptor);
						acceptorDirections.put(acceptor, ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor)).getOpposite());
					}
				}
			}
		}
		
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
	
	/**
	 * Updates the client-side pipes for rendering.
	 * @param transferred - the LiquidStack of server-side transferred liquid
	 */
	public void clientUpdate()
	{
		loopThrough(pointer);
		
		for(TileEntity tileEntity : iteratedPipes)
		{
			if(tileEntity instanceof IMechanicalPipe)
			{
				((IMechanicalPipe)tileEntity).onTransfer(liquidToSend);
			}
		}
	}
	
	/**
	 * Runs the protocol and distributes the liquid.
	 * @return liquid transferred
	 */
	public int calculate()
	{
		loopThrough(pointer);
		
		Collections.shuffle(availableAcceptors);
		
		int liquidSent = 0;
		
		if(!availableAcceptors.isEmpty())
		{
			int divider = availableAcceptors.size();
			int remaining = liquidToSend.amount % divider;
			int sending = (liquidToSend.amount-remaining)/divider;
			
			for(ITankContainer acceptor : availableAcceptors)
			{
				int currentSending = sending;
				
				if(remaining > 0)
				{
					currentSending++;
					remaining--;
				}
				
				liquidSent += acceptor.fill(acceptorDirections.get(acceptor), new LiquidStack(liquidToSend.itemID, currentSending, liquidToSend.itemMeta), true);
			}
		}
		
		if(liquidSent > 0 && FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			LiquidStack sendStack = liquidToSend.copy();
			sendStack.amount = liquidSent;
			MinecraftForge.EVENT_BUS.post(new LiquidTransferEvent(this, sendStack));
		}
		
		return liquidSent;
	}
	
	public static class LiquidTransferEvent extends Event
	{
		public final LiquidTransferProtocol transferProtocol;
		
		public final LiquidStack liquidSent;
		
		public LiquidTransferEvent(LiquidTransferProtocol protocol, LiquidStack liquid)
		{
			transferProtocol = protocol;
			liquidSent = liquid;
		}
	}
}
