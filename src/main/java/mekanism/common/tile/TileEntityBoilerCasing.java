package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.common.Mekanism;
import mekanism.common.base.IFluidContainerManager;
import mekanism.common.content.boiler.BoilerCache;
import mekanism.common.content.boiler.BoilerUpdateProtocol;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.content.boiler.SynchronizedBoilerData.ValveData;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import mekanism.common.util.LangUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityBoilerCasing extends TileEntityMultiblock<SynchronizedBoilerData> implements IFluidContainerManager, IHeatTransfer
{
	/** A client-sided set of valves on this tank's structure that are currently active, used on the client for rendering fluids. */
	public Set<ValveData> valveViewing = new HashSet<ValveData>();

	/** The capacity this tank has on the client-side. */
	public int clientWaterCapacity;
	public int clientSteamCapacity;

	public float prevWaterScale;

	public ForgeDirection innerSide;

	public double temperature;
	public double heatToAbsorb;
	public double invHeatCapacity = 5;

	public TileEntityBoilerCasing()
	{
		this("SteamBoiler");
	}

	public TileEntityBoilerCasing(String name)
	{
		super(name);
		inventory = new ItemStack[2];
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(worldObj.isRemote)
		{
			if(structure != null && clientHasStructure && isRendering)
			{
				float targetScale = (float)(structure.waterStored != null ? structure.waterStored.amount : 0)/clientWaterCapacity;

				if(Math.abs(prevWaterScale - targetScale) > 0.01)
				{
					prevWaterScale = (9*prevWaterScale + targetScale)/10;
				}
			}

			if(!clientHasStructure || !isRendering)
			{
				for(ValveData data : valveViewing)
				{
					TileEntityBoilerCasing tileEntity = (TileEntityBoilerCasing)data.location.getTileEntity(worldObj);

					if(tileEntity != null)
					{
						tileEntity.clientHasStructure = false;
					}
				}

				valveViewing.clear();
			}
		}

		if(!worldObj.isRemote)
		{
			if(structure != null)
			{
				if(structure.waterStored != null && structure.waterStored.amount <= 0)
				{
					structure.waterStored = null;
					markDirty();
				}
				
				if(structure.steamStored != null && structure.steamStored.amount <= 0)
				{
					structure.steamStored = null;
					markDirty();
				}
				
				if(isRendering)
				{
					boolean needsValveUpdate = false;
					
					for(ValveData data : structure.valves)
					{
						if(data.activeTicks > 0)
						{
							data.activeTicks--;
						}
						
						if(data.activeTicks > 0 != data.prevActive)
						{
							needsValveUpdate = true;
						}
						
						data.prevActive = data.activeTicks > 0;
					}
					
					if(needsValveUpdate || structure.needsRenderUpdate())
					{
						sendPacketToRenderer();
					}
					
					structure.prevWater = structure.waterStored;
					structure.prevSteam = structure.steamStored;
				}
			}
		}
	}

	@Override
	protected SynchronizedBoilerData getNewStructure()
	{
		return new SynchronizedBoilerData();
	}
	
	@Override
	public BoilerCache getNewCache()
	{
		return new BoilerCache();
	}

	@Override
	protected BoilerUpdateProtocol getProtocol()
	{
		return new BoilerUpdateProtocol(this);
	}

	@Override
	public MultiblockManager<SynchronizedBoilerData> getManager()
	{
		return Mekanism.boilerManager;
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		if(structure != null)
		{
			data.add(structure.volume*BoilerUpdateProtocol.WATER_PER_TANK);
			data.add(structure.volume*BoilerUpdateProtocol.STEAM_PER_TANK);
			data.add(structure.editMode.ordinal());

			if(structure.waterStored != null)
			{
				data.add(1);
				data.add(structure.waterStored.getFluidID());
				data.add(structure.waterStored.amount);
			}
			else {
				data.add(0);
			}

			if(structure.steamStored != null)
			{
				data.add(1);
				data.add(structure.steamStored.getFluidID());
				data.add(structure.steamStored.amount);
			}
			else {
				data.add(0);
			}

			if(isRendering)
			{
				Set<ValveData> toSend = new HashSet<ValveData>();

				for(ValveData valveData : structure.valves)
				{
					if(valveData.activeTicks > 0)
					{
						toSend.add(valveData);
					}
				}
				
				data.add(toSend.size());
				
				for(ValveData valveData : toSend)
				{
					valveData.location.write(data);
					data.add(valveData.side.ordinal());
				}
			}
		}

		return data;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);

		if(clientHasStructure)
		{
			clientWaterCapacity = dataStream.readInt();
			clientSteamCapacity = dataStream.readInt();
			structure.editMode = ContainerEditMode.values()[dataStream.readInt()];
			
			if(dataStream.readInt() == 1)
			{
				structure.waterStored = new FluidStack(FluidRegistry.getFluid(dataStream.readInt()), dataStream.readInt());
			}
			else {
				structure.waterStored = null;
			}
	
			if(dataStream.readInt() == 1)
			{
				structure.steamStored = new FluidStack(FluidRegistry.getFluid(dataStream.readInt()), dataStream.readInt());
			}
			else {
				structure.steamStored = null;
			}
	
			if(isRendering)
			{
				int size = dataStream.readInt();
				
				valveViewing.clear();

				for(int i = 0; i < size; i++)
				{
					ValveData data = new ValveData();
					data.location = Coord4D.read(dataStream);
					data.side = ForgeDirection.getOrientation(dataStream.readInt());
					
					valveViewing.add(data);

					TileEntityBoilerCasing tileEntity = (TileEntityBoilerCasing)data.location.getTileEntity(worldObj);

					if(tileEntity != null)
					{
						tileEntity.clientHasStructure = true;
					}
				}
			}
		}
	}

	public int getScaledWaterLevel(int i)
	{
		if(clientWaterCapacity == 0 || structure.waterStored == null)
		{
			return 0;
		}

		return structure.waterStored.amount*i / clientWaterCapacity;
	}

	public int getScaledSteamLevel(int i)
	{
		if(clientSteamCapacity == 0 || structure.steamStored == null)
		{
			return 0;
		}

		return structure.steamStored.amount*i / clientSteamCapacity;
	}

	@Override
	public ContainerEditMode getContainerEditMode()
	{
		if(structure != null)
		{
			return structure.editMode;
		}

		return ContainerEditMode.BOTH;
	}

	@Override
	public void setContainerEditMode(ContainerEditMode mode)
	{
		if(structure == null)
		{
			return;
		}

		structure.editMode = mode;
	}

	@Override
	public double getTemp()
	{
		return temperature;
	}

	@Override
	public double getInverseConductionCoefficient()
	{
		return 1;
	}

	@Override
	public double getInsulationCoefficient(ForgeDirection side)
	{
		return 50;
	}

	@Override
	public void transferHeatTo(double heat)
	{
		if(structure != null)
		{
			structure.heatToAbsorb += heat;
		}
	}

	@Override
	public double[] simulateHeat()
	{
		return new double[] {0, 0};
	}

	@Override
	public double applyTemperatureChange()
	{
		temperature += invHeatCapacity * heatToAbsorb;
		heatToAbsorb = 0;
		
		return temperature;
	}

	@Override
	public boolean canConnectHeat(ForgeDirection side)
	{
		return structure != null;
	}

	@Override
	public IHeatTransfer getAdjacent(ForgeDirection side)
	{
		return null;
	}
	
	@Override
	public String getInventoryName()
	{
		return LangUtils.localize("gui.thermoelectricBoiler");
	}

	public boolean isInnerSide(ForgeDirection side)
	{
		if(innerSide != null)
		{
			return side == innerSide;
		}

		if(!Coord4D.get(this).getFromSide(side).getBlock(worldObj).isAir(worldObj, xCoord, yCoord, zCoord))
		{
			return false;
		}

		if(structure == null || structure.minLocation == null || structure.maxLocation == null)
		{
			return false;
		}

		switch(side)
		{
			case DOWN:
				return yCoord == structure.maxLocation.yCoord;
			case UP:
				return yCoord == structure.minLocation.yCoord;
			case NORTH:
				return zCoord == structure.maxLocation.zCoord;
			case SOUTH:
				return zCoord == structure.minLocation.zCoord;
			case WEST:
				return xCoord == structure.maxLocation.xCoord;
			case EAST:
				return xCoord == structure.minLocation.xCoord;
			default:
				return false;
		}
	}
}
