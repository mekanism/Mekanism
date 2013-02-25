package mekanism.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import mekanism.api.EnumGas;
import mekanism.api.IGasAcceptor;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class GasTransferProtocol 
{
	/** List of iterated tubes, to prevent infinite loops. */
	public ArrayList<TileEntity> iteratedTubes = new ArrayList<TileEntity>();
	
	/** List of IGasAcceptors that can take in the type of gas requested. */
	public ArrayList<IGasAcceptor> availableAcceptors = new ArrayList<IGasAcceptor>();
	
	/** Pointer of this calculation */
	public TileEntity pointer;
	
	/** Type of gas to distribute */
	public EnumGas transferType;
	
	/** Amount of gas to distribute  */
	public int gasToSend;
	
	/**
	 * GasTransferProtocol -- a calculation used to distribute gasses through a tube network.
	 * @param head - pointer tile entity
	 * @param type - type of gas to distribute
	 * @param amount - amount of gas to distribute
	 */
	public GasTransferProtocol(TileEntity head, EnumGas type, int amount)
	{
		pointer = head;
		transferType = type;
		gasToSend = amount;
	}
	
	/**
	 * Recursive loop that iterates through connected tubes and adds connected acceptors to an ArrayList.
	 * @param tile - pointer tile entity
	 */
	public void loopThrough(TileEntity tile)
	{
		IGasAcceptor[] acceptors = MekanismUtils.getConnectedAcceptors(tile);
		
		for(IGasAcceptor acceptor : acceptors)
		{
			if(acceptor != null)
			{
				if(acceptor.canReceiveGas(ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor)).getOpposite(), transferType))
				{
					availableAcceptors.add(acceptor);
				}
			}
		}
		
		iteratedTubes.add(tile);
		
		TileEntity[] tubes = MekanismUtils.getConnectedTubes(tile);
		
		for(TileEntity tube : tubes)
		{
			if(tube != null)
			{
				if(!iteratedTubes.contains(tube))
				{
					loopThrough(tube);
				}
			}
		}
	}
	
	/**
	 * Runs the protocol and distributes the gas.
	 * @return rejected gas
	 */
	public int calculate()
	{
		loopThrough(pointer);
		
		if(!availableAcceptors.isEmpty())
		{
			boolean sentRemaining = false;
			int divider = availableAcceptors.size();
			int remaining = gasToSend % divider;
			int sending = (gasToSend-remaining)/divider;
			
			for(IGasAcceptor acceptor : availableAcceptors)
			{
				int currentSending = sending;
				
				if(remaining > 0)
				{
					currentSending++;
					remaining--;
				}
				
				acceptor.transferGasToAcceptor(currentSending, transferType);
				gasToSend -= currentSending;
			}
		}
		
		return gasToSend;
	}
}
