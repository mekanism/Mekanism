package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.base.IFluidContainerManager;
import mekanism.common.content.boiler.BoilerCache;
import mekanism.common.content.boiler.BoilerUpdateProtocol;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.content.boiler.SynchronizedBoilerData.ValveData;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import mekanism.common.util.HeatUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

public class TileEntityBoiler extends TileEntityMultiblock<SynchronizedBoilerData> implements IFluidContainerManager, IHeatTransfer
{
	/** A client-sided and server-sided map of valves on this tank's structure, used on the client for rendering fluids. */
	public Map<ValveData, Integer> valveViewing = new HashMap<ValveData, Integer>();

	/** The capacity this tank has on the client-side. */
	public int clientWaterCapacity;
	public int clientSteamCapacity;

	public float prevWaterScale;
	public float prevSteamScale;

	public ForgeDirection innerSide;

	public double temperature;
	public double heatToAbsorb;
	public double invHeatCapacity = 5;

	public TileEntityBoiler()
	{
		this("SteamBoiler");
	}

	public TileEntityBoiler(String name)
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
				for(ValveData data : valveViewing.keySet())
				{
					if(valveViewing.get(data) > 0)
					{
						valveViewing.put(data, valveViewing.get(data)-1);
					}
				}

				float targetScale = (float)(structure.waterStored != null ? structure.waterStored.amount : 0)/clientWaterCapacity;

				if(Math.abs(prevWaterScale - targetScale) > 0.01)
				{
					prevWaterScale = (9*prevWaterScale + targetScale)/10;
				}

				targetScale = (float)(structure.steamStored != null ? structure.steamStored.amount : 0)/clientSteamCapacity;

				if(Math.abs(prevSteamScale - targetScale) > 0.01)
				{
					prevSteamScale = (9*prevSteamScale+ targetScale)/10;
				}
			}

			if(!clientHasStructure || !isRendering)
			{
				for(ValveData data : valveViewing.keySet())
				{
					TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)data.location.getTileEntity(worldObj);

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
				manageInventory();
			}
		}
	}

	public void manageInventory()
	{
		int max = structure.volume * BoilerUpdateProtocol.WATER_PER_TANK;

		if(structure.inventory[0] != null)
		{
			if(structure.inventory[0].getItem() instanceof IFluidContainerItem)
			{
				if(structure.editMode == ContainerEditMode.FILL && structure.waterStored != null)
				{
					int prev = structure.waterStored.amount;

					structure.waterStored.amount -= FluidContainerUtils.insertFluid(structure.waterStored, structure.inventory[0]);

					if(prev == structure.waterStored.amount || structure.waterStored.amount == 0)
					{
						if(structure.inventory[1] == null)
						{
							structure.inventory[1] = structure.inventory[0].copy();
							structure.inventory[0] = null;

							markDirty();
						}
					}

					if(structure.waterStored.amount == 0)
					{
						structure.waterStored = null;
					}
				}
				else if(structure.editMode == ContainerEditMode.EMPTY)
				{
					if(structure.waterStored != null)
					{
						FluidStack received = FluidContainerUtils.extractFluid(max-structure.waterStored.amount, structure.inventory[0], structure.waterStored.getFluid());

						if(received != null)
						{
							structure.waterStored.amount += received.amount;
						}
					}
					else {
						structure.waterStored = FluidContainerUtils.extractFluid(max, structure.inventory[0], null);
					}

					int newStored = structure.waterStored != null ? structure.waterStored.amount : 0;

					if(((IFluidContainerItem)structure.inventory[0].getItem()).getFluid(structure.inventory[0]) == null || newStored == max)
					{
						if(structure.inventory[1] == null)
						{
							structure.inventory[1] = structure.inventory[0].copy();
							structure.inventory[0] = null;

							markDirty();
						}
					}
				}
			}
			else if(FluidContainerRegistry.isEmptyContainer(structure.inventory[0]) && (structure.editMode == ContainerEditMode.BOTH || structure.editMode == ContainerEditMode.FILL))
			{
				if(structure.waterStored != null && structure.waterStored.amount >= FluidContainerRegistry.BUCKET_VOLUME)
				{
					ItemStack filled = FluidContainerRegistry.fillFluidContainer(structure.waterStored, structure.inventory[0]);

					if(filled != null)
					{
						if(structure.inventory[1] == null || (structure.inventory[1].isItemEqual(filled) && structure.inventory[1].stackSize+1 <= filled.getMaxStackSize()))
						{
							structure.inventory[0].stackSize--;

							if(structure.inventory[0].stackSize <= 0)
							{
								structure.inventory[0] = null;
							}

							if(structure.inventory[1] == null)
							{
								structure.inventory[1] = filled;
							}
							else {
								structure.inventory[1].stackSize++;
							}

							markDirty();

							structure.waterStored.amount -= FluidContainerRegistry.getFluidForFilledItem(filled).amount;

							if(structure.waterStored.amount == 0)
							{
								structure.waterStored = null;
							}

							Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
						}
					}
				}
			}
			else if(FluidContainerRegistry.isFilledContainer(structure.inventory[0]) && (structure.editMode == ContainerEditMode.BOTH || structure.editMode == ContainerEditMode.EMPTY))
			{
				FluidStack itemFluid = FluidContainerRegistry.getFluidForFilledItem(structure.inventory[0]);

				if((structure.waterStored == null && itemFluid.amount <= max) || structure.waterStored.amount+itemFluid.amount <= max)
				{
					if(structure.waterStored != null && !structure.waterStored.isFluidEqual(itemFluid))
					{
						return;
					}

					ItemStack containerItem = structure.inventory[0].getItem().getContainerItem(structure.inventory[0]);

					boolean filled = false;

					if(containerItem != null)
					{
						if(structure.inventory[1] == null || (structure.inventory[1].isItemEqual(containerItem) && structure.inventory[1].stackSize+1 <= containerItem.getMaxStackSize()))
						{
							structure.inventory[0] = null;

							if(structure.inventory[1] == null)
							{
								structure.inventory[1] = containerItem;
							}
							else {
								structure.inventory[1].stackSize++;
							}

							filled = true;
						}
					}
					else {
						structure.inventory[0].stackSize--;

						if(structure.inventory[0].stackSize == 0)
						{
							structure.inventory[0] = null;
						}

						filled = true;
					}

					if(filled)
					{
						if(structure.waterStored == null)
						{
							structure.waterStored = itemFluid.copy();
						}
						else {
							structure.waterStored.amount += itemFluid.amount;
						}

						markDirty();
					}

					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
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
		}

		if(structure != null && structure.waterStored != null)
		{
			data.add(1);
			data.add(structure.waterStored.fluidID);
			data.add(structure.waterStored.amount);
		}
		else {
			data.add(0);
		}

		if(structure != null && structure.steamStored != null)
		{
			data.add(1);
			data.add(structure.steamStored.fluidID);
			data.add(structure.steamStored.amount);
		}
		else {
			data.add(0);
		}

		if(structure != null && isRendering)
		{
			data.add(structure.valves.size());

			for(ValveData valveData : structure.valves)
			{
				valveData.location.write(data);

				data.add(valveData.side.ordinal());
				data.add(valveData.serverFluid);
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
		}

		if(dataStream.readInt() == 1)
		{
			structure.waterStored = new FluidStack(dataStream.readInt(), dataStream.readInt());
		}
		else {
			structure.waterStored = null;
		}

		if(dataStream.readInt() == 1)
		{
			structure.steamStored = new FluidStack(dataStream.readInt(), dataStream.readInt());
		}
		else {
			structure.steamStored = null;
		}

		if(clientHasStructure && isRendering)
		{
			int size = dataStream.readInt();

			for(int i = 0; i < size; i++)
			{
				ValveData data = new ValveData();
				data.location = Coord4D.read(dataStream);
				data.side = ForgeDirection.getOrientation(dataStream.readInt());
				int viewingTicks = 0;

				if(dataStream.readBoolean())
				{
					viewingTicks = 30;
				}

				if(viewingTicks == 0)
				{
					if(valveViewing.containsKey(data) && valveViewing.get(data) > 0)
					{
						continue;
					}
				}

				valveViewing.put(data, viewingTicks);

				TileEntityBoiler tileEntity = (TileEntityBoiler)data.location.getTileEntity(worldObj);

				if(tileEntity != null)
				{
					tileEntity.clientHasStructure = true;
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
		return 50;
	}

	@Override
	public double getInsulationCoefficient(ForgeDirection side)
	{
		return 50;
	}

	@Override
	public void transferHeatTo(double heat)
	{
		heatToAbsorb += heat;
	}

	@Override
	public double[] simulateHeat()
	{
		innerSide = null;
		return HeatUtils.simulate(this);
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
		return structure == null || !isInnerSide(side);
	}

	@Override
	public IHeatTransfer getAdjacent(ForgeDirection side)
	{
		if(structure != null && isInnerSide(side))
		{
			return structure;
		}
		else
		{
			TileEntity adj = Coord4D.get(this).getFromSide(side).getTileEntity(worldObj);
			if(adj instanceof IHeatTransfer)
			{
				return (IHeatTransfer)adj;
			}
		}

		return null;
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
