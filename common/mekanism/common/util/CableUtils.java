package mekanism.common.util;

import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.energy.ICableOutputter;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.tile.TileEntityElectricBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import cofh.api.energy.IEnergyHandler;

public final class CableUtils
{
    /**
     * Gets all the connected energy acceptors, whether IC2-based or BuildCraft-based, surrounding a specific tile entity.
     * @param tileEntity - center tile entity
     * @return TileEntity[] of connected acceptors
     */
    public static TileEntity[] getConnectedEnergyAcceptors(TileEntity tileEntity)
    {
    	TileEntity[] acceptors = new TileEntity[] {null, null, null, null, null, null};
    	
    	for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    	{
			TileEntity acceptor = Coord4D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.worldObj);
			
			if(acceptor instanceof IStrictEnergyAcceptor || 
					acceptor instanceof IEnergySink || 
					(acceptor instanceof IPowerReceptor && !(acceptor instanceof IGridTransmitter) && MekanismUtils.useBuildCraft()) ||
					acceptor instanceof IEnergyHandler)
			{
				acceptors[orientation.ordinal()] = acceptor;
			}
    	}
    	
    	return acceptors;
    }
    
    /**
     * Gets all the connected cables around a specific tile entity.
     * @param tileEntity - center tile entity
     * @return TileEntity[] of connected cables
     */
    public static TileEntity[] getConnectedCables(TileEntity tileEntity)
    {
    	TileEntity[] cables = new TileEntity[] {null, null, null, null, null, null};
    	
    	for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    	{
			TileEntity cable = Coord4D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.worldObj);
			
			if(TransmissionType.checkTransmissionType(cable, TransmissionType.ENERGY))
			{
				cables[orientation.ordinal()] = cable;
			}
    	}
    	
    	return cables;
    }
    
    /**
     * Gets all the adjacent connections to a TileEntity.
     * @param tileEntity - center TileEntity
     * @return boolean[] of adjacent connections
     */
    public static boolean[] getConnections(TileEntity tileEntity)
    {
		boolean[] connectable = new boolean[] {false, false, false, false, false, false};
		
		TileEntity[] connectedAcceptors = getConnectedEnergyAcceptors(tileEntity);
		TileEntity[] connectedCables = getConnectedCables(tileEntity);
		TileEntity[] connectedOutputters = getConnectedOutputters(tileEntity);
		
		for(TileEntity tile : connectedAcceptors)
		{
			int side = Arrays.asList(connectedAcceptors).indexOf(tile);
			
			if(canConnectToAcceptor(ForgeDirection.getOrientation(side), tileEntity))
			{
				connectable[side] = true;
			}
		}
		
		for(TileEntity tile : connectedOutputters)
		{
			if(tile != null)
			{
				int side = Arrays.asList(connectedOutputters).indexOf(tile);
				
				connectable[side] = true;
			}
		}
		
		for(TileEntity tile : connectedCables)
		{
			if(tile != null)
			{
				int side = Arrays.asList(connectedCables).indexOf(tile);
				
				connectable[side] = true;
			}
		}
		
		return connectable;
    }
    
    /**
     * Gets all the connected cables around a specific tile entity.
     * @param tileEntity - center tile entity
     * @return TileEntity[] of connected cables
     */
    public static TileEntity[] getConnectedOutputters(TileEntity tileEntity)
    {
    	TileEntity[] outputters = new TileEntity[] {null, null, null, null, null, null};
    	
    	for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    	{
			TileEntity outputter = Coord4D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.worldObj);
			
			if((outputter instanceof ICableOutputter && ((ICableOutputter)outputter).canOutputTo(orientation.getOpposite())) || 
					(outputter instanceof IEnergySource && ((IEnergySource)outputter).emitsEnergyTo(tileEntity, orientation.getOpposite())) ||
					(outputter instanceof IEnergyHandler && ((IEnergyHandler)outputter).canInterface(orientation.getOpposite())) ||
					(outputter instanceof IPowerEmitter && ((IPowerEmitter)outputter).canEmitPowerFrom(orientation.getOpposite())))
			{
				outputters[orientation.ordinal()] = outputter;
			}
    	}
    	
    	return outputters;
    }
    
    /**
     * Whether or not a cable can connect to a specific acceptor.
     * @param side - side to check
     * @param tileEntity - cable TileEntity
     * @return whether or not the cable can connect to the specific side
     */
    public static boolean canConnectToAcceptor(ForgeDirection side, TileEntity tile)
    {
    	if(tile == null)
    	{
    		return false;
    	}
    	
    	TileEntity tileEntity = Coord4D.get(tile).getFromSide(side).getTileEntity(tile.worldObj);
    	
    	return isConnectable(tile, tileEntity, side);
    }
    
    public static boolean isConnectable(TileEntity orig, TileEntity tileEntity, ForgeDirection side)
    {
    	if(tileEntity instanceof IGridTransmitter)
    	{
    		return false;
    	}
    	
    	if(tileEntity instanceof IStrictEnergyAcceptor)
    	{
    		if(((IStrictEnergyAcceptor)tileEntity).canReceiveEnergy(side.getOpposite()))
    		{
    			return true;
    		}
    	}
    	else if(tileEntity instanceof IEnergyAcceptor)
    	{
    		if(((IEnergyAcceptor)tileEntity).acceptsEnergyFrom(orig, side.getOpposite()))
    		{
    			return true;
    		}
    	}
    	else if(tileEntity instanceof ICableOutputter)
    	{
    		if(((ICableOutputter)tileEntity).canOutputTo(side.getOpposite()))
    		{
    			return true;
    		}
    	}
    	else if(tileEntity instanceof IEnergyHandler)
    	{
    		if(((IEnergyHandler)tileEntity).canInterface(side.getOpposite()))
    		{
    			return true;
    		}
    	}
    	else if(tileEntity instanceof IPowerReceptor && MekanismUtils.useBuildCraft())
    	{
    		if(((IPowerReceptor)tileEntity).getPowerReceiver(side.getOpposite()) != null)
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    public static void emit(TileEntityElectricBlock emitter)
    {
    	if(!emitter.worldObj.isRemote && MekanismUtils.canFunction(emitter))
    	{
			double sendingEnergy = Math.min(emitter.getEnergy(), emitter.getMaxOutput());
			
			if(sendingEnergy > 0)
			{
		    	List<ForgeDirection> outputtingSides = new ArrayList<ForgeDirection>();
		    	boolean[] connectable = getConnections(emitter);
		    	
		    	for(ForgeDirection side : emitter.getOutputtingSides())
		    	{
		    		if(connectable[side.ordinal()])
		    		{
		    			outputtingSides.add(side);
		    		}
		    	}
		    	
		    	if(outputtingSides.size() > 0)
		    	{
		    		double sent = 0;
		    		
		    		boolean cont = false;
		    		
		    		do {
		    			cont = false;
		    			double prev = sent;
		    			sent += emit_do(emitter, outputtingSides, sendingEnergy-sent);
		    			
		    			if(sendingEnergy-sent > 0 && sent-prev > 0)
		    			{
		    				cont = true;
		    			}
		    		} while(cont);
		    		
		    		emitter.setEnergy(emitter.getEnergy() - sent);
		    	}
			}
    	}
    }
    
    private static double emit_do(TileEntityElectricBlock emitter, List<ForgeDirection> outputtingSides, double totalToSend)
    {
		double remains = totalToSend%outputtingSides.size();
		double splitSend = (totalToSend-remains)/outputtingSides.size();
		double sent = 0;
		
		List<ForgeDirection> toRemove = new ArrayList<ForgeDirection>();
		
    	for(ForgeDirection side : outputtingSides)
		{
			TileEntity tileEntity = Coord4D.get(emitter).getFromSide(side).getTileEntity(emitter.worldObj);
			double toSend = splitSend+remains;
			remains = 0;
			
			double prev = sent;
			sent += emit_do_do(emitter, tileEntity, side, toSend);
			
			if(sent-prev == 0)
			{
				toRemove.add(side);
			}
		}
    	
    	for(ForgeDirection side : toRemove)
    	{
    		outputtingSides.remove(side);
    	}
    	
    	return sent;
    }
    
    private static double emit_do_do(TileEntityElectricBlock from, TileEntity tileEntity, ForgeDirection side, double sendingEnergy)
    {
    	double sent = 0;
    	
		if(tileEntity instanceof IStrictEnergyAcceptor)
		{
			IStrictEnergyAcceptor acceptor = (IStrictEnergyAcceptor)tileEntity;
			
			if(acceptor.canReceiveEnergy(side.getOpposite()))
			{
				sent += (sendingEnergy - acceptor.transferEnergyToAcceptor(side.getOpposite(), sendingEnergy));
			}
		}
		else if(tileEntity instanceof IEnergyHandler)
		{
			IEnergyHandler handler = (IEnergyHandler)tileEntity;
			
			if(handler.canInterface(side.getOpposite()))
			{
				int used = handler.receiveEnergy(side.getOpposite(), (int)Math.round(sendingEnergy*Mekanism.TO_TE), false);
				sent += used*Mekanism.FROM_TE;
			}
		}
		else if(tileEntity instanceof IEnergySink)
		{
			if(((IEnergySink)tileEntity).acceptsEnergyFrom(from, side.getOpposite()))
			{
				double toSend = Math.min(sendingEnergy, Math.min(((IEnergySink)tileEntity).getMaxSafeInput(), ((IEnergySink)tileEntity).demandedEnergyUnits())*Mekanism.FROM_IC2);
				double rejects = ((IEnergySink)tileEntity).injectEnergyUnits(side.getOpposite(), toSend*Mekanism.TO_IC2)*Mekanism.FROM_IC2;
				sent += (toSend - rejects);
			}
		}
		else if(tileEntity instanceof IPowerReceptor && MekanismUtils.useBuildCraft())
		{
			PowerReceiver receiver = ((IPowerReceptor)tileEntity).getPowerReceiver(side.getOpposite());
			
			if(receiver != null)
			{
            	double transferEnergy = Math.min(sendingEnergy, receiver.powerRequest()*Mekanism.FROM_BC);
            	float used = receiver.receiveEnergy(Type.STORAGE, (float)(transferEnergy*Mekanism.TO_BC), side.getOpposite());
            	sent += used*Mekanism.FROM_BC;
			}
		}
		
		return sent;
    }
}
