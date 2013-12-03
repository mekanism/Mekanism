package mekanism.common.multipart;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;

import java.util.ArrayList;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.api.energy.ICableOutputter;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.EnergyNetwork;
import mekanism.common.Mekanism;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.core.block.IElectrical;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartUniversalCable extends PartTransmitter<EnergyNetwork, Double> implements IEnergySink
{
	public PartUniversalCable()
	{
		super();
	}

	@Override
	public String getType()
	{
		return "mekanism:universal_cable";
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.ENERGY;
	}
	
	@Override
	public EnergyNetwork createNetworkFromSingleTransmitter(ITransmitter<EnergyNetwork, Double> transmitter)
	{
		return new EnergyNetwork(transmitter);
	}
	
	@Override
	public EnergyNetwork createNetworkByMergingSet(Set<EnergyNetwork> networks)
	{
		return new EnergyNetwork(networks);
	}

	@Override
	public boolean isValidAcceptor(TileEntity acceptor, ForgeDirection side)
	{
		if(acceptor instanceof ITransmitter)
			return false;
		
		//Mekanism
		if(acceptor instanceof ICableOutputter && ((ICableOutputter)acceptor).canOutputTo(side.getOpposite()))
			return true;
		
		if(acceptor instanceof IStrictEnergyAcceptor && ((IStrictEnergyAcceptor)acceptor).canReceiveEnergy(side.getOpposite()))
    		return true;
    	
		//UE
		if(acceptor instanceof IElectrical && ((IElectrical)acceptor).canConnect(side.getOpposite()))
			return true;
		
		//IC2
		if(Mekanism.hooks.IC2Loaded)
		{
			if(acceptor instanceof IEnergySource && ((IEnergySource)acceptor).emitsEnergyTo(tile(), side.getOpposite())) 
				return true;
	    	
			if(acceptor instanceof IEnergyAcceptor && ((IEnergyAcceptor)acceptor).acceptsEnergyFrom(tile(), side.getOpposite()))
				return true;
		}
    			
		//Buildcraft
		if(Mekanism.hooks.BuildCraftLoaded)
		{
	    	if(acceptor instanceof IPowerReceptor && ((IPowerReceptor)acceptor).getPowerReceiver(side.getOpposite()) != null)
				return true;
			
	    	if(acceptor instanceof IPowerEmitter && ((IPowerEmitter)acceptor).canEmitPowerFrom(side.getOpposite()))
	    			return true;
		}
    	
    	return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float frame, int pass)
	{
		if(pass == 1)
		{
			RenderPartTransmitter.getInstance().renderContents(this, pos);
		}
	}
	
	public void register()
	{
		if(!world().isRemote)
		{
			if(!Mekanism.ic2Registered.contains(Object3D.get(tile())))
			{
				Mekanism.ic2Registered.add(Object3D.get(tile()));
				MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent((IEnergyTile)tile()));
			}
		}
	}

	@Override
	public void chunkLoad()
	{
		register();
	}
	
	@Override
	public void preRemove()
	{		
		if(!world().isRemote)
		{	
			Mekanism.ic2Registered.remove(Object3D.get(tile()));
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent((IEnergyTile)tile()));
		}
		
		super.preRemove();
	}
	
	@Override
	public void onAdded()
	{
		super.onAdded();
		
		register();
	}
	
	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		
		if(!world().isRemote)
		{			
			Mekanism.ic2Registered.remove(Object3D.get(tile()));
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent((IEnergyTile)tile()));
		}
	}

	@Override
	public int getTransmitterNetworkSize()
	{
		return getTransmitterNetwork().getSize();
	}

	@Override
	public int getTransmitterNetworkAcceptorSize()
	{
		return getTransmitterNetwork().getAcceptorSize();
	}

	@Override
	public String getTransmitterNetworkNeeded()
	{
		return getTransmitterNetwork().getNeeded();
	}
	
	@Override
	public String getTransmitterNetworkFlow()
	{
		return getTransmitterNetwork().getFlow();
	}
	
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return true;
	}

	@Override
	public double demandedEnergyUnits()
	{
		return getTransmitterNetwork().getEnergyNeeded(new ArrayList())*Mekanism.TO_IC2;
	}
	
	@Override
	public int getMaxSafeInput()
	{
		return Integer.MAX_VALUE;
	}

	@Override
    public double injectEnergyUnits(ForgeDirection direction, double i)
    {
		ArrayList list = new ArrayList();
		list.add(Object3D.get(tile()).getFromSide(direction).getTileEntity(world()));
    	return getTransmitterNetwork().emit(i*Mekanism.FROM_IC2, list)*Mekanism.TO_IC2;
    }
}
