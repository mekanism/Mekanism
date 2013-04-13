package mekanism.common;

import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;

import java.util.ArrayList;

import mekanism.api.ICableOutputter;
import mekanism.api.IStrictEnergyAcceptor;
import mekanism.api.IUniversalCable;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.block.IConnectionProvider;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
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
			TileEntity acceptor = VectorHelper.getTileEntityFromSide(tileEntity.worldObj, new Vector3(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord), orientation);
			
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
			TileEntity cable = VectorHelper.getTileEntityFromSide(tileEntity.worldObj, new Vector3(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord), orientation);
			
			if(cable instanceof IUniversalCable && ((IUniversalCable)cable).canTransferEnergy(tileEntity))
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
			TileEntity outputter = VectorHelper.getTileEntityFromSide(tileEntity.worldObj, new Vector3(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord), orientation);
			
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
    	TileEntity tileEntity = VectorHelper.getTileEntityFromSide(tile.worldObj, new Vector3(tile.xCoord, tile.yCoord, tile.zCoord), side);
    	
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
    	TileEntity pointer = VectorHelper.getTileEntityFromSide(sender.worldObj, new Vector3(sender.xCoord, sender.yCoord, sender.zCoord), facing);
    	
    	if(pointer instanceof IUniversalCable)
    	{
	    	return new EnergyTransferProtocol(pointer, sender, amount, new ArrayList()).calculate();
    	}
    	
    	return amount;
    }
    
    /**
     * Emits energy from all sides of a TileEntity.
     * @param amount - amount to send
     * @param pointer - sending TileEntity
     * @return rejected energy
     */
    public static double emitEnergyFromAllSides(double amount, TileEntity pointer)
    {
    	if(pointer != null)
    	{
    		return new EnergyTransferProtocol(pointer, pointer, amount, new ArrayList()).calculate();
    	}
    	
    	return amount;
    }
    
    /**
     * Emits energy from all sides of a TileEntity, while ignoring specific acceptors.
     * @param amount - amount to send
     * @param pointer - sending TileEntity
     * @param ignored - ignored acceptors
     * @return rejected energy
     */
    public static double emitEnergyFromAllSidesIgnore(double amount, TileEntity pointer, ArrayList ignored)
    {
    	if(pointer != null)
    	{
    		return new EnergyTransferProtocol(pointer, pointer, amount, ignored).calculate();
    	}
    	
    	return amount;
    }
}
