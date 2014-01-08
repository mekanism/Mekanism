package mekanism.common.multipart;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;

import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.EnergyNetwork;
import mekanism.common.Mekanism;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import codechicken.lib.vec.Vector3;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartUniversalCable extends PartTransmitter<EnergyNetwork> implements IStrictEnergyAcceptor, IEnergySink, IEnergyHandler, IPowerReceptor
{
	public CableTier tier;

	/** A fake power handler used to initiate energy transfer calculations. */
	public PowerHandler powerHandler;
	
    public static TransmitterIcons cableIcons = new TransmitterIcons(4, 1);
    
    public double currentPower = 0;
    
    public double cacheEnergy = 0;
    public double lastWrite = 0;
    
	public PartUniversalCable(CableTier cableTier)
	{
		tier = cableTier;
		powerHandler = new PowerHandler(this, PowerHandler.Type.STORAGE);
		powerHandler.configurePowerPerdition(0, 0);
		powerHandler.configure(0, 0, 0, 0);
	}

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
        else {
        	if(cacheEnergy > 0)
        	{
        		getTransmitterNetwork().electricityStored += cacheEnergy;
        		cacheEnergy = 0;
        	}
        }
        
        super.update();
    }
    
	@Override
	public void refreshTransmitterNetwork()
	{
		super.refreshTransmitterNetwork();
		
		reconfigure();
	}
    
    @Override
    public TransmitterType getTransmitter()
    {
    	return tier.type;
    }
    
    @Override
    public void load(NBTTagCompound nbtTags)
    {
    	super.load(nbtTags);
    	
    	cacheEnergy = nbtTags.getDouble("cacheEnergy");
		tier = CableTier.values()[nbtTags.getInteger("tier")];
    }
    
    @Override
    public void save(NBTTagCompound nbtTags)
    {
    	super.save(nbtTags);
    	
    	double toSave = EnergyNetwork.round(getTransmitterNetwork().electricityStored*(1F/getTransmitterNetwork().transmitters.size()));
    	
    	lastWrite = toSave;
    	nbtTags.setDouble("cacheEnergy", toSave);
		nbtTags.setInteger("tier", tier.ordinal());
    }

	@Override
	public String getType()
	{
		return "mekanism:universal_cable_" + tier.name().toLowerCase();
	}

    public static void registerIcons(IconRegister register)
    {
        cableIcons.registerCenterIcons(register, new String[] {"UniversalCableBasic", "UniversalCableAdvanced",
				"UniversalCableElite", "UniversalCableUltimate"});
        cableIcons.registerSideIcons(register, new String[] {"TransmitterSideSmall"});
    }
    
    @Override
    public void preSingleMerge(EnergyNetwork network)
    {
    	network.electricityStored += cacheEnergy;
    	cacheEnergy = 0;
    }

    @Override
    public Icon getCenterIcon()
    {
        return cableIcons.getCenterIcon(tier.ordinal());
    }

    @Override
    public Icon getSideIcon()
    {
        return cableIcons.getSideIcon(0);
    }

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.ENERGY;
	}
	
	@Override
	public EnergyNetwork createNetworkFromSingleTransmitter(IGridTransmitter<EnergyNetwork> transmitter)
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
		return CableUtils.getConnections(tile())[side.ordinal()];
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
			if(!Mekanism.ic2Registered.contains(Coord4D.get(tile())))
			{
				Mekanism.ic2Registered.add(Coord4D.get(tile()));
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
			Mekanism.ic2Registered.remove(Coord4D.get(tile()));
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
		if(!world().isRemote)
		{			
			Mekanism.ic2Registered.remove(Coord4D.get(tile()));
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent((IEnergyTile)tile()));
			
			getTransmitterNetwork().electricityStored -= lastWrite;
		}
		
		super.onChunkUnload();
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
        return tier.cableCapacity;
    }

	@Override
	public double transferEnergyToAcceptor(ForgeDirection side, double amount)
	{
		if(!canReceiveEnergy(side))
		{
			return amount;
		}
		
    	double toUse = Math.min(getMaxEnergy()-getEnergy(), amount);
    	setEnergy(getEnergy() + toUse);
    	
    	return amount-toUse;
	}

	@Override
	public boolean canReceiveEnergy(ForgeDirection side)
	{
		return getConnectionType(side) == ConnectionType.NORMAL || getConnectionType(side) == ConnectionType.PULL;
	}
	
	@Override
	public double getMaxEnergy()
	{
		return getTransmitterNetwork().getCapacity();
	}
	
	@Override
	public double getEnergy()
	{
		return getTransmitterNetwork().electricityStored;
	}
	
	@Override
	public void setEnergy(double energy)
	{
		getTransmitterNetwork().electricityStored = energy;
	}
	
	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) 
	{
		if(getTransmitterNetwork().getEnergyNeeded() == 0)
		{
			return null;
		}
		
		return powerHandler.getPowerReceiver();
	}
	
	@Override
	public World getWorld()
	{
		return world();
	}
	
	private void reconfigure()
	{
		if(MekanismUtils.useBuildCraft())
		{
			float needed = (float)(getTransmitterNetwork().getEnergyNeeded()*Mekanism.TO_BC);
			powerHandler.configure(1, needed, 0, needed);
		}
	}

	@Override
	public void doWork(PowerHandler workProvider) 
	{
		if(MekanismUtils.useBuildCraft())
		{
			if(powerHandler.getEnergyStored() > 0)
			{
				getTransmitterNetwork().emit(powerHandler.getEnergyStored()*Mekanism.FROM_BC);
			}
			
			powerHandler.setEnergy(0);
			reconfigure();
		}
	}

	public static enum CableTier
	{
		BASIC(500, TransmitterType.UNIVERSAL_CABLE_BASIC),
		ADVANCED(2000, TransmitterType.UNIVERSAL_CABLE_ADVANCED),
		ELITE(8000, TransmitterType.UNIVERSAL_CABLE_ELITE),
		ULTIMATE(32000, TransmitterType.UNIVERSAL_CABLE_ULTIMATE);

		int cableCapacity;
		TransmitterType type;

		private CableTier(int capacity, TransmitterType transmitterType)
		{
			cableCapacity = capacity;
			type = transmitterType;
		}
	}
}
