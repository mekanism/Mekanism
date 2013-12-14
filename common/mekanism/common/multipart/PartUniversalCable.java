package mekanism.common.multipart;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;

import java.util.Set;

import mekanism.api.Object3D;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.PartTransmitterIcons;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.EnergyNetwork;
import mekanism.common.Mekanism;
import mekanism.common.util.CableUtils;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
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
    public static PartTransmitterIcons cableIcons;
    
    public double currentPower = 0;

    @Override
    public void update()
    {
        if(world().isRemote)
        {
            double targetPower = getTransmitterNetwork().clientEnergyScale;
            
            if(Math.abs(currentPower - targetPower) > 0.01)
            {
                currentPower = (9*currentPower + targetPower)/10;
            }
        }
        
        super.update();
    }

	@Override
	public String getType()
	{
		return "mekanism:universal_cable";
	}

    public static void registerIcons(IconRegister register)
    {
        cableIcons = new PartTransmitterIcons(1);
        cableIcons.registerCenterIcons(register, new String[] {"UniversalCable"});
        cableIcons.registerSideIcon(register, "TransmitterSideSmall");
    }

    @Override
    public Icon getCenterIcon()
    {
        return cableIcons.getCenterIcon(0);
    }

    @Override
    public Icon getSideIcon()
    {
        return cableIcons.getSideIcon();
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
		return getTransmitterNetwork().getEnergyNeeded()*Mekanism.TO_IC2;
	}
	
	@Override
	public int getMaxSafeInput()
	{
		return Integer.MAX_VALUE;
	}

	@Override
    public double injectEnergyUnits(ForgeDirection direction, double i)
    {
    	return getTransmitterNetwork().emit(i*Mekanism.FROM_IC2)*Mekanism.TO_IC2;
    }
	
	@Override
	public float receiveElectricity(ForgeDirection from, ElectricityPack receive, boolean doReceive)
	{
		if(doReceive && receive != null && receive.getWatts() > 0)
		{
			return receive.getWatts() - (float)(getTransmitterNetwork().emit(receive.getWatts()*Mekanism.FROM_UE));
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
		return (float)(getTransmitterNetwork().getEnergyNeeded()*Mekanism.TO_UE);
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
		if(!simulate)
		{
	    	return maxReceive - (int)Math.round(getTransmitterNetwork().emit(maxReceive*Mekanism.FROM_TE)*Mekanism.TO_TE);
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
		return (int)Math.round(getTransmitterNetwork().getEnergyNeeded()*Mekanism.TO_TE);
	}

    @Override
    public int getCapacity()
    {
        return 10000;
    }
}
