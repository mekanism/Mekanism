package mekanism.common.multipart;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import codechicken.lib.vec.Vector3;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import universalelectricity.core.block.IConductor;
import universalelectricity.core.block.IElectrical;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.grid.IElectricityNetwork;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import mekanism.api.Object3D;
import mekanism.api.energy.ICableOutputter;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.EnergyNetwork;
import mekanism.common.Mekanism;
import mekanism.common.util.CableUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.core.block.IElectrical;
import universalelectricity.core.electricity.ElectricityPack;
import codechicken.lib.vec.Vector3;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartUniversalCable extends PartTransmitter<EnergyNetwork> implements IEnergySink, IEnergyHandler, IElectrical
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
	public EnergyNetwork createNetworkFromSingleTransmitter(ITransmitter<EnergyNetwork> transmitter)
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
		return CableUtils.isConnectable(tile(), acceptor, side.getOpposite());
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
	
	@Override
	public float receiveElectricity(ForgeDirection from, ElectricityPack receive, boolean doReceive)
	{
		ArrayList list = new ArrayList();
		list.add(Object3D.get(tile()).getFromSide(from).getTileEntity(world()));
		
		if(doReceive && receive != null && receive.getWatts() > 0)
		{
			return receive.getWatts() - (float)(getTransmitterNetwork().emit(receive.getWatts()*Mekanism.FROM_UE, list));
		}
		
		return 0;
	}

	@Override
	public ElectricityPack provideElectricity(ForgeDirection from, ElectricityPack request, boolean doProvide)
	{
		return null;
	}

	@Override
	public float getRequest(ForgeDirection direction)
	{
		ArrayList list = new ArrayList();
		list.add(Object3D.get(tile()).getFromSide(direction).getTileEntity(world()));
		return (float)(getTransmitterNetwork().getEnergyNeeded(list)*Mekanism.TO_UE);
	}

	@Override
	public float getProvide(ForgeDirection direction)
	{
		return 0;
	}

	@Override
	public float getVoltage()
	{
		return 120;
	}
	
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) 
	{
		ArrayList list = new ArrayList();
		list.add(Object3D.get(tile()).getFromSide(from).getTileEntity(world()));
		
		if(!simulate)
		{
	    	return maxReceive - (int)Math.round(getTransmitterNetwork().emit(maxReceive*Mekanism.FROM_TE, list)*Mekanism.TO_TE);
		}
		
		return 0;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) 
	{
		return 0;
	}

	@Override
	public boolean canInterface(ForgeDirection from) 
	{
		return true;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) 
	{
		return 0;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		ArrayList list = new ArrayList();
		list.add(Object3D.get(tile()).getFromSide(from).getTileEntity(world()));
		return (int)Math.round(getTransmitterNetwork().getEnergyNeeded(list)*Mekanism.TO_TE);
	}
}
