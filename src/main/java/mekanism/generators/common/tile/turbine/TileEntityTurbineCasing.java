package mekanism.generators.common.tile.turbine;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
import mekanism.api.MekanismConfig.generators;
import mekanism.api.Range4D;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.Mekanism;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.turbine.SynchronizedTurbineData;
import mekanism.generators.common.content.turbine.TurbineCache;
import mekanism.generators.common.content.turbine.TurbineUpdateProtocol;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityTurbineCasing extends TileEntityMultiblock<SynchronizedTurbineData> implements IStrictEnergyStorage
{	
	public TileEntityTurbineCasing() 
	{
		this("TurbineCasing");
	}
	
	public TileEntityTurbineCasing(String name)
	{
		super(name);
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!worldObj.isRemote)
		{
			if(structure != null)
			{
				if(structure.fluidStored != null && structure.fluidStored.amount <= 0)
				{
					structure.fluidStored = null;
					markDirty();
				}
				
				if(isRendering)
				{
					structure.lastSteamInput = structure.newSteamInput;
					structure.newSteamInput = 0;
					
					int stored = structure.fluidStored != null ? structure.fluidStored.amount : 0;
					double proportion = (double)stored/(double)structure.getFluidCapacity();
					double flowRate = 0;
					
					if(stored > 0 && getEnergy() < structure.getEnergyCapacity())
					{
						double energyMultiplier = (general.maxEnergyPerSteam/TurbineUpdateProtocol.MAX_BLADES)*Math.min(structure.blades, structure.coils*generators.turbineBladesPerCoil);
						double rate = structure.lowerVolume*(structure.getDispersers()*generators.turbineDisperserGasFlow);						
						rate = Math.min(rate, structure.vents*generators.turbineVentGasFlow);
						
						double origRate = rate;
						
						rate = Math.min(Math.min(stored, rate), (getMaxEnergy()-getEnergy())/energyMultiplier)*proportion;
						
						flowRate = rate/origRate;
						setEnergy(getEnergy()+((int)rate)*energyMultiplier);
						
						structure.fluidStored.amount -= rate;
						structure.clientFlow = Math.min((int)rate, structure.condensers*generators.condenserRate);
						structure.flowRemaining = (int)rate;
						
						if(structure.fluidStored.amount == 0)
						{
							structure.fluidStored = null;
						}
					}
					else {
						structure.clientFlow = 0;
					}
					
					if(structure.dumpMode == GasMode.DUMPING && structure.fluidStored != null)
					{
						structure.fluidStored.amount -= Math.min(structure.fluidStored.amount, Math.max(structure.fluidStored.amount/50, structure.lastSteamInput*2));
						
						if(structure.fluidStored.amount == 0)
						{
							structure.fluidStored = null;
						}
					}
					
					float newRotation = (float)flowRate;
					boolean needsRotationUpdate = false;
					
					if(Math.abs(newRotation-structure.clientRotation) > SynchronizedTurbineData.ROTATION_THRESHOLD)
					{
						structure.clientRotation = newRotation;
						needsRotationUpdate = true;
					}
					
					if(structure.needsRenderUpdate() || needsRotationUpdate)
					{
						sendPacketToRenderer();
					}
					
					structure.prevFluid = structure.fluidStored != null ? structure.fluidStored.copy() : null;
				}
			}
		}
	}
	
	@Override
	public String getInventoryName()
	{
		return LangUtils.localize("gui.industrialTurbine");
	}
	
	@Override
	public boolean onActivate(EntityPlayer player)
	{
		if(!player.isSneaking() && structure != null)
		{
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
			player.openGui(MekanismGenerators.instance, 6, worldObj, xCoord, yCoord, zCoord);
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public double getEnergy()
	{
		return structure != null ? structure.electricityStored : 0;
	}
	
	@Override
	public double getMaxEnergy() 
	{
		return structure.getEnergyCapacity();
	}

	@Override
	public void setEnergy(double energy)
	{
		if(structure != null)
		{
			structure.electricityStored = Math.max(Math.min(energy, getMaxEnergy()), 0);
			MekanismUtils.saveChunk(this);
		}
	}
	
	public int getScaledFluidLevel(int i)
	{
		if(structure.getFluidCapacity() == 0 || structure.fluidStored == null)
		{
			return 0;
		}

		return structure.fluidStored.amount*i / structure.getFluidCapacity();
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		if(structure != null)
		{
			data.add(structure.volume);
			data.add(structure.lowerVolume);
			data.add(structure.vents);
			data.add(structure.blades);
			data.add(structure.coils);
			data.add(structure.condensers);
			data.add(structure.getDispersers());
			data.add(structure.electricityStored);
			data.add(structure.clientFlow);
			data.add(structure.lastSteamInput);
			data.add(structure.dumpMode.ordinal());
			
			if(structure.fluidStored != null)
			{
				data.add(1);
				data.add(structure.fluidStored.getFluidID());
				data.add(structure.fluidStored.amount);
			}
			else {
				data.add(0);
			}
			
			if(isRendering)
			{
				structure.complex.write(data);
				data.add(structure.clientRotation);
			}
		}

		return data;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(!worldObj.isRemote)
		{
			if(structure != null)
			{
				byte type = dataStream.readByte();
	
				if(type == 0)
				{
					structure.dumpMode = GasMode.values()[structure.dumpMode.ordinal() == GasMode.values().length-1 ? 0 : structure.dumpMode.ordinal()+1];
				}
			}

			return;
		}
		
		super.handlePacketData(dataStream);
		
		if(worldObj.isRemote)
		{
			if(clientHasStructure)
			{
				structure.volume = dataStream.readInt();
				structure.lowerVolume = dataStream.readInt();
				structure.vents = dataStream.readInt();
				structure.blades = dataStream.readInt();
				structure.coils = dataStream.readInt();
				structure.condensers = dataStream.readInt();
				structure.clientDispersers = dataStream.readInt();
				structure.electricityStored = dataStream.readDouble();
				structure.clientFlow = dataStream.readInt();
				structure.lastSteamInput = dataStream.readInt();
				structure.dumpMode = GasMode.values()[dataStream.readInt()];
				
				if(dataStream.readInt() == 1)
				{
					structure.fluidStored = new FluidStack(FluidRegistry.getFluid(dataStream.readInt()), dataStream.readInt());
				}
				else {
					structure.fluidStored = null;
				}
				
				if(isRendering)
				{
					structure.complex = Coord4D.read(dataStream);
					
					structure.clientRotation = dataStream.readFloat();
					SynchronizedTurbineData.clientRotationMap.put(structure.inventoryID, structure.clientRotation);
				}
			}
		}
	}

	@Override
	protected SynchronizedTurbineData getNewStructure() 
	{
		return new SynchronizedTurbineData();
	}

	@Override
	public MultiblockCache<SynchronizedTurbineData> getNewCache() 
	{
		return new TurbineCache();
	}

	@Override
	protected UpdateProtocol<SynchronizedTurbineData> getProtocol() 
	{
		return new TurbineUpdateProtocol(this);
	}

	@Override
	public MultiblockManager<SynchronizedTurbineData> getManager() 
	{
		return MekanismGenerators.turbineManager;
	}
}
