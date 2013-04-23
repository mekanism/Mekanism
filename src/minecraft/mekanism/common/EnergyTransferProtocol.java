package mekanism.common;

import ic2.api.energy.tile.IEnergySink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import mekanism.api.IStrictEnergyAcceptor;
import mekanism.api.IUniversalCable;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerReceptor;
import cpw.mods.fml.common.FMLCommonHandler;

public class EnergyTransferProtocol
{
	/** List of iterated cables, to prevent infinite loops. */
	public ArrayList<TileEntity> iteratedCables = new ArrayList<TileEntity>();
	
	/** List of TileEntities that can take in the energy requested. */
	public ArrayList<TileEntity> availableAcceptors = new ArrayList<TileEntity>();
	
	/** Map of directions energy is transferred to. */
	public Map<TileEntity, ForgeDirection> acceptorDirections = new HashMap<TileEntity, ForgeDirection>();
	
	/** Pointer tube of this calculation. */
	public TileEntity pointer;
	
	/** Original outputter Tile Entity. */
	public TileEntity original;
	
	/** Amount of energy to distribute.  */
	public double energyToSend;
	
	/** Acceptors ignored by this calculation. */
	public ArrayList<TileEntity> ignoredAcceptors = new ArrayList<TileEntity>();
	
	/**
	 * EnergyTransferProtocol -- a calculation used to distribute energy through a cable network.
	 * @param head - pointer tile entity
	 * @param orig - original tile entity
	 * @param amount - amount of energy to distribute
	 * @param ignored - acceptors/pipes to ignore
	 */
	public EnergyTransferProtocol(TileEntity head, TileEntity orig, double amount, ArrayList ignored)
	{
		pointer = head;
		original = orig;
		energyToSend = amount;
		ignoredAcceptors = ignored;
	}
	
	/**
	 * EnergyTransferProtocol -- a calculation used to distribute energy through a cable network.
	 * @param head - pointer tile entity
	 * @param orig - original tile entity
	 */
	public EnergyTransferProtocol(TileEntity head, TileEntity orig, ArrayList ignored)
	{
		pointer = head;
		original = orig;
		energyToSend = 0;
		ignoredAcceptors = ignored;
	}
	
	/**
	 * Recursive loop that iterates through connected cables and adds connected acceptors to an ArrayList.  Note that it will NOT add
	 * the original outputting tile into the availableAcceptors list, to prevent loops.
	 * @param tile - pointer tile entity
	 */
	public void loopThrough(TileEntity tile)
	{
		TileEntity[] acceptors = CableUtils.getConnectedEnergyAcceptors(tile);
		
		for(TileEntity acceptor : acceptors)
		{
			if(acceptor != original && !ignoredAcceptors.contains(acceptor))
			{
				if(acceptor instanceof IStrictEnergyAcceptor)
				{
					if(((IStrictEnergyAcceptor)acceptor).canReceiveEnergy(ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor)).getOpposite()))
					{
						if((((IStrictEnergyAcceptor)acceptor).getMaxEnergy() - ((IStrictEnergyAcceptor)acceptor).getEnergy()) > 0)
						{
							availableAcceptors.add(acceptor);
						}
					}
				}
				else if(acceptor instanceof IEnergySink)
				{
					if(((IEnergySink)acceptor).acceptsEnergyFrom(original, MekanismUtils.toIC2Direction(ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor))).getInverse()))
					{
						if(Math.min((((IEnergySink)acceptor).demandsEnergy()*Mekanism.FROM_IC2), (((IEnergySink)acceptor).getMaxSafeInput()*Mekanism.FROM_IC2)) > 0)
						{
							availableAcceptors.add(acceptor);
							acceptorDirections.put(acceptor, ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor)));
						}
					}
				}
				else if(acceptor instanceof IPowerReceptor && Mekanism.hooks.BuildCraftLoaded)
				{
					if(((IPowerReceptor)acceptor).getPowerProvider() != null)
					{
						if((((IPowerReceptor)acceptor).powerRequest(ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor)).getOpposite())*Mekanism.FROM_BC) > 0)
						{
							availableAcceptors.add(acceptor);
							acceptorDirections.put(acceptor, ForgeDirection.getOrientation(Arrays.asList(acceptors).indexOf(acceptor)));
						}
					}
				}
			}
		}
		
		if(!iteratedCables.contains(tile))
		{
			iteratedCables.add(tile);
		}
		
		TileEntity[] tubes = CableUtils.getConnectedCables(tile);
		
		for(TileEntity tube : tubes)
		{
			if(tube != null)
			{
				if(!iteratedCables.contains(tube))
				{
					loopThrough(tube);
				}
			}
		}
	}
	
	/**
	 * Updates the client-side cables for rendering.
	 */
	public void clientUpdate()
	{
		loopThrough(pointer);
		
		for(TileEntity tileEntity : iteratedCables)
		{
			if(tileEntity instanceof IUniversalCable)
			{
				((IUniversalCable)tileEntity).onTransfer();
			}
		}
	}
	
	/**
	 * Runs the protocol and distributes the energy.
	 * @return rejected energy
	 */
	public double calculate()
	{
		loopThrough(pointer);
		
		Collections.shuffle(availableAcceptors);
		
		double prevSending = energyToSend;
		
		if(!availableAcceptors.isEmpty())
		{
			int divider = availableAcceptors.size();
			double remaining = energyToSend % divider;
			double currentRemaining = remaining;
			double sending = (energyToSend-remaining)/divider;
			
			for(TileEntity acceptor : availableAcceptors)
			{
				double currentSending = sending;
				
				if(currentRemaining > 0)
				{
					currentSending += (currentRemaining/divider);
					currentRemaining -= (currentRemaining/divider);
				}
				
				if(acceptor instanceof IStrictEnergyAcceptor)
				{
					energyToSend -= (currentSending - ((IStrictEnergyAcceptor)acceptor).transferEnergyToAcceptor(currentSending));
				}
				else if(acceptor instanceof IEnergySink)
				{
					double toSend = Math.min(currentSending, (((IEnergySink)acceptor).getMaxSafeInput()*Mekanism.FROM_IC2));
					energyToSend -= (toSend - (((IEnergySink)acceptor).injectEnergy(MekanismUtils.toIC2Direction(acceptorDirections.get(acceptor).getOpposite()), (int)(toSend*Mekanism.TO_IC2))*Mekanism.FROM_IC2));
				}
				else if(acceptor instanceof IPowerReceptor && Mekanism.hooks.BuildCraftLoaded)
				{
					IPowerReceptor receptor = (IPowerReceptor)acceptor;
	            	double electricityNeeded = Math.min(receptor.powerRequest(acceptorDirections.get(acceptor).getOpposite()), receptor.getPowerProvider().getMaxEnergyStored() - receptor.getPowerProvider().getEnergyStored())*Mekanism.FROM_BC;
	            	float transferEnergy = (float)Math.min(electricityNeeded, currentSending);
	            	receptor.getPowerProvider().receiveEnergy((float)(transferEnergy*Mekanism.TO_BC), acceptorDirections.get(acceptor).getOpposite());
					energyToSend -= transferEnergy;
				}
			}
		}
		
		if(prevSending > energyToSend && FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			PacketHandler.sendEnergyTransferUpdate(pointer);
		}
		
		return energyToSend;
	}
	
	/**
	 * Gets the needed energy in this network.
	 * @return needed energy
	 */
	public double neededEnergy()
	{
		double totalNeeded = 0;
		
		loopThrough(pointer);
		
		if(!availableAcceptors.isEmpty())
		{
			for(TileEntity acceptor : availableAcceptors)
			{
				if(acceptor instanceof IStrictEnergyAcceptor)
				{
					totalNeeded += (((IStrictEnergyAcceptor)acceptor).getMaxEnergy() - ((IStrictEnergyAcceptor)acceptor).getEnergy());
				}
				else if(acceptor instanceof IEnergySink)
				{
					totalNeeded += Math.min((((IEnergySink)acceptor).demandsEnergy()*Mekanism.FROM_IC2), (((IEnergySink)acceptor).getMaxSafeInput()*Mekanism.FROM_IC2));
				}
				else if(acceptor instanceof IPowerReceptor && Mekanism.hooks.BuildCraftLoaded)
				{
					totalNeeded += (((IPowerReceptor)acceptor).powerRequest(acceptorDirections.get(acceptor).getOpposite())*Mekanism.FROM_BC);
				}
			}
		}
		
		return totalNeeded;
	}
}
