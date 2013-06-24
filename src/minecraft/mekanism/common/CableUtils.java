package mekanism.common;

import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mekanism.api.ICableOutputter;
import mekanism.api.IStrictEnergyAcceptor;
import mekanism.api.IUniversalCable;
import mekanism.api.Object3D;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.block.IConnectionProvider;
import buildcraft.api.power.IPowerReceptor;

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
			TileEntity acceptor = Object3D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.worldObj);
			
			if(acceptor instanceof IStrictEnergyAcceptor || acceptor instanceof IEnergySink || (acceptor instanceof IPowerReceptor && !(acceptor instanceof IUniversalCable) && Mekanism.hooks.BuildCraftLoaded))
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
			TileEntity cable = Object3D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.worldObj);
			
			if(cable instanceof IUniversalCable && ((IUniversalCable)cable).canTransferEnergy())
			{
				cables[orientation.ordinal()] = cable;
			}
    	}
    	
    	return cables;
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
			TileEntity outputter = Object3D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.worldObj);
			
			if(outputter instanceof ICableOutputter && ((ICableOutputter)outputter).canOutputTo(orientation.getOpposite()) || outputter instanceof IEnergySource && ((IEnergySource)outputter).emitsEnergyTo(tileEntity, MekanismUtils.toIC2Direction(orientation.getOpposite())))
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
    	TileEntity tileEntity = Object3D.get(tile).getFromSide(side).getTileEntity(tile.worldObj);
    	
    	if(tileEntity instanceof IStrictEnergyAcceptor && ((IStrictEnergyAcceptor)tileEntity).canReceiveEnergy(side.getOpposite()))
    	{
    		return true;
    	}
    	
    	if(tileEntity instanceof IConnectionProvider && ((IConnectionProvider)tileEntity).canConnect(side.getOpposite()))
    	{
    		return true;
    	}
    	
    	if(tileEntity instanceof IEnergyAcceptor && ((IEnergyAcceptor)tileEntity).acceptsEnergyFrom(tile, MekanismUtils.toIC2Direction(side).getInverse()))
    	{
    		return true;
    	}
    	
    	if(tileEntity instanceof ICableOutputter && ((ICableOutputter)tileEntity).canOutputTo(side.getOpposite()))
    	{
    		return true;
    	}
    	
    	if(tileEntity instanceof IPowerReceptor && !(tileEntity instanceof IUniversalCable) && Mekanism.hooks.BuildCraftLoaded)
    	{
    		if(!(tileEntity instanceof IEnergyAcceptor) || ((IEnergyAcceptor)tileEntity).acceptsEnergyFrom(null, MekanismUtils.toIC2Direction(side).getInverse()))
    		{
    			if(!(tileEntity instanceof IEnergySource) || ((IEnergySource)tileEntity).emitsEnergyTo(null, MekanismUtils.toIC2Direction(side).getInverse()))
    			{
    				return true;
    			}
    		}
    	}
    	
    	return false;
    }
    
    /**
     * Emits a defined amount of energy to the network, distributing between IC2-based and BuildCraft-based acceptors.
     * @param amount - amount to send
     * @param sender - sending TileEntity
     * @param facing - direction the TileEntity is facing
     * @return rejected energy
     */
    public static double emitEnergyToNetwork(double amount, TileEntity sender, ForgeDirection facing)
    {
    	TileEntity pointer = Object3D.get(sender).getFromSide(facing).getTileEntity(sender.worldObj);
    	
    	if(pointer instanceof IUniversalCable)
    	{
    		IUniversalCable cable = (IUniversalCable)pointer;
    		
    		ArrayList<TileEntity> ignored = new ArrayList<TileEntity>();
    		ignored.add(sender);
    		
    		return cable.getNetwork().emit(amount, ignored);
    	}
    	
    	return amount;
    }
    
    /**
     * Emits energy from all sides of a TileEntity.
     * @param amount - amount to send
     * @param pointer - sending TileEntity
     * @param ignored - ignored acceptors
     * @return rejected energy
     */
    public static double emitEnergyFromAllSides(double amount, TileEntity pointer, ArrayList ignored)
    {
    	if(pointer != null)
    	{
    		Set<EnergyNetwork> networks = new HashSet<EnergyNetwork>();
    		double totalRemaining = 0;
    		
    		ignored.add(pointer);
    		
    		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
    		{
    			TileEntity sideTile = Object3D.get(pointer).getFromSide(side).getTileEntity(pointer.worldObj);
    			
    			if(sideTile instanceof IUniversalCable)
    			{
    				networks.add(((IUniversalCable)sideTile).getNetwork());
    			}
    		}
    		
    		double splitEnergy = amount/(double)networks.size();
    		double remaining = amount%(double)networks.size();
    		
    		for(EnergyNetwork network : networks)
    		{
    			totalRemaining += network.emit(splitEnergy+remaining, ignored);
    			remaining = 0;
    		}
    		
    		return totalRemaining;
    	}
    	
    	return amount;
    }
}
