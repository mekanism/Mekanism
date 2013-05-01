package mekanism.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import cpw.mods.fml.common.FMLCommonHandler;

/**
 * The actual protocol gas goes through when it is transferred via Pressurized Tubes.
 * @author AidanBrady
 *
 */
public class GasTransferProtocol 
{
	/** List of iterated tubes, to prevent infinite loops. */
	public ArrayList<TileEntity> iteratedTubes = new ArrayList<TileEntity>();
	
	/** List of IGasAcceptors that can take in the type of gas requested. */
	public ArrayList<IGasAcceptor> availableAcceptors = new ArrayList<IGasAcceptor>();
	
	/** Pointer tube of this calculation */
	public TileEntity pointer;
	
	/** Original outputter Tile Entity. */
	public TileEntity original;
	
	/** Type of gas to distribute */
	public EnumGas transferType;
	
	/** Amount of gas to distribute  */
	public int gasToSend;
	
	/**
	 * GasTransferProtocol -- a calculation used to distribute gasses through a tube network.
	 * @param head - pointer tile entity
	 * @param orig - original outputter
	 * @param type - type of gas being transferred
	 * @param amount - amount of gas to distribute
	 */
	public GasTransferProtocol(TileEntity head, TileEntity orig, EnumGas type, int amount)
	{
		pointer = head;
		transferType = type;
		gasToSend = amount;
		original = orig;
	}
	
	/**
	 * Recursive loop that iterates through connected tubes and adds connected acceptors to an ArrayList.  Note that it will NOT add
	 * the original outputting tile into the availableAcceptors list, to prevent loops.
	 * @param tile - pointer tile entity
	 */
	public void loopThrough(TileEntity tile)
	{
		IGasAcceptor[] acceptors = GasTransmission.getConnectedAcceptors(tile);
		
		for(IGasAcceptor acceptor : acceptors)
		{
			if(acceptor != null && !availableAcceptors.contains(acceptor))
			{
				if(acceptor != original && acceptor.canReceiveGas(ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor)).getOpposite(), transferType))
				{
					if(!(acceptor instanceof IGasStorage) || (acceptor instanceof IGasStorage && (((IGasStorage)acceptor).getMaxGas(transferType) - ((IGasStorage)acceptor).getGas(transferType)) > 0))
					{
						availableAcceptors.add(acceptor);
					}
				}
			}
		}
		
		if(!iteratedTubes.contains(tile))
		{
			iteratedTubes.add(tile);
		}
		
		TileEntity[] tubes = GasTransmission.getConnectedTubes(tile);
		
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
	 * Updates the client-side tubes for rendering.
	 */
	public void clientUpdate()
	{
		loopThrough(pointer);
		
		for(TileEntity tileEntity : iteratedTubes)
		{
			if(tileEntity instanceof IPressurizedTube)
			{
				((IPressurizedTube)tileEntity).onTransfer(transferType);
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
		
		Collections.shuffle(availableAcceptors);
		
		int prevSending = gasToSend;
		
		if(!availableAcceptors.isEmpty())
		{
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
				
				gasToSend -= (currentSending - acceptor.transferGasToAcceptor(currentSending, transferType));
			}
		}
		
		if(prevSending > gasToSend && FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			MinecraftForge.EVENT_BUS.post(new GasTransferEvent(this));
		}
		
		return gasToSend;
	}
	
	public static class GasTransferEvent extends Event
	{
		public final GasTransferProtocol transferProtocol;
		
		public GasTransferEvent(GasTransferProtocol protocol)
		{
			transferProtocol = protocol;
		}
	}
}
