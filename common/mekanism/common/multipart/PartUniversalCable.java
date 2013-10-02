package mekanism.common.multipart;

import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;

import java.util.Set;

import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import universalelectricity.core.block.IElectrical;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import mekanism.api.energy.ICableOutputter;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.EnergyNetwork;
import mekanism.common.Mekanism;

public class PartUniversalCable extends PartTransmitter<EnergyNetwork, Double>
{
	public Double setLevel = 0.0;
	private int ticks;
	
	public PartUniversalCable()
	{
		super();
		transmitting = 0.0;
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
			RenderPartTransmitter.getInstance().renderContents(this, pos);
	}
	
	@Override
	public void clientUpdate(Double level)
	{
		setLevel = level;
	}
	
	@Override
	public void update()
	{
		if(world().isRemote)
		{
			if(transmitting != setLevel)
			{
				transmitting = (transmitting *4.0 + setLevel)/5.0;
				if(Math.max(transmitting - setLevel, setLevel - transmitting) < 0.05)
					transmitting = setLevel;
			}
		}
	}
}
