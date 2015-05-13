package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.base.IFluidContainerManager;
import mekanism.common.content.tank.SynchronizedTankData;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.content.tank.TankCache;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

public class TileEntityDynamicTank extends TileEntityMultiblock<SynchronizedTankData> implements IFluidContainerManager
{
	/** A client-sided and server-sided map of valves on this tank's structure, used on the client for rendering fluids. */
	public Map<ValveData, Integer> valveViewing = new HashMap<ValveData, Integer>();

	/** The capacity this tank has on the client-side. */
	public int clientCapacity;

	public float prevScale;

	public TileEntityDynamicTank()
	{
		super("DynamicTank");
	}

	public TileEntityDynamicTank(String name)
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

				float targetScale = (float)(structure.fluidStored != null ? structure.fluidStored.amount : 0)/clientCapacity;

				if(Math.abs(prevScale - targetScale) > 0.01)
				{
					prevScale = (9*prevScale + targetScale)/10;
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
				if(structure.fluidStored != null && structure.fluidStored.amount <= 0)
				{
					structure.fluidStored = null;
					markDirty();
				}
				
				manageInventory();
			}
		}
	}

	public void manageInventory()
	{
		int max = structure.volume * TankUpdateProtocol.FLUID_PER_TANK;

		if(structure.inventory[0] != null)
		{
			if(structure.inventory[0].getItem() instanceof IFluidContainerItem)
			{
				if(structure.editMode == ContainerEditMode.FILL && structure.fluidStored != null)
				{
					int prev = structure.fluidStored.amount;
					
					structure.fluidStored.amount -= FluidContainerUtils.insertFluid(structure.fluidStored, structure.inventory[0]);
					
					if(prev == structure.fluidStored.amount || structure.fluidStored.amount == 0)
					{
						if(structure.inventory[1] == null)
						{
							structure.inventory[1] = structure.inventory[0].copy();
							structure.inventory[0] = null;
							
							markDirty();
						}
					}
					
					if(structure.fluidStored.amount == 0)
					{
						structure.fluidStored = null;
					}
				}
				else if(structure.editMode == ContainerEditMode.EMPTY)
				{
					if(structure.fluidStored != null)
					{
						FluidStack received = FluidContainerUtils.extractFluid(max-structure.fluidStored.amount, structure.inventory[0], structure.fluidStored.getFluid());
						
						if(received != null)
						{
							structure.fluidStored.amount += received.amount;
						}
					}
					else {
						structure.fluidStored = FluidContainerUtils.extractFluid(max, structure.inventory[0], null);
					}
					
					int newStored = structure.fluidStored != null ? structure.fluidStored.amount : 0;
					
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
				if(structure.fluidStored != null && structure.fluidStored.amount >= FluidContainerRegistry.BUCKET_VOLUME)
				{
					ItemStack filled = FluidContainerRegistry.fillFluidContainer(structure.fluidStored, structure.inventory[0]);

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

							structure.fluidStored.amount -= FluidContainerRegistry.getFluidForFilledItem(filled).amount;

							if(structure.fluidStored.amount == 0)
							{
								structure.fluidStored = null;
							}

							Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
						}
					}
				}
			}
			else if(FluidContainerRegistry.isFilledContainer(structure.inventory[0]) && (structure.editMode == ContainerEditMode.BOTH || structure.editMode == ContainerEditMode.EMPTY))
			{
				FluidStack itemFluid = FluidContainerRegistry.getFluidForFilledItem(structure.inventory[0]);

				if((structure.fluidStored == null && itemFluid.amount <= max) || structure.fluidStored.amount+itemFluid.amount <= max)
				{
					if(structure.fluidStored != null && !structure.fluidStored.isFluidEqual(itemFluid))
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
						if(structure.fluidStored == null)
						{
							structure.fluidStored = itemFluid.copy();
						}
						else {
							structure.fluidStored.amount += itemFluid.amount;
						}
						
						markDirty();
					}

					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
				}
			}
		}
	}
	
	@Override
	protected SynchronizedTankData getNewStructure()
	{
		return new SynchronizedTankData();
	}
	
	@Override
	public TankCache getNewCache()
	{
		return new TankCache();
	}
	
	@Override
	protected TankUpdateProtocol getProtocol()
	{
		return new TankUpdateProtocol(this);
	}
	
	@Override
	public MultiblockManager<SynchronizedTankData> getManager()
	{
		return Mekanism.tankManager;
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		if(structure != null)
		{
			data.add(structure.volume*TankUpdateProtocol.FLUID_PER_TANK);
			data.add(structure.editMode.ordinal());
			
			if(structure.fluidStored != null)
			{
				data.add(1);
				data.add(structure.fluidStored.fluidID);
				data.add(structure.fluidStored.amount);
			}
			else {
				data.add(0);
			}
			
			if(isRendering)
			{
				data.add(structure.valves.size());

				for(ValveData valveData : structure.valves)
				{
					valveData.location.write(data);
					data.add(valveData.side.ordinal());
					data.add(valveData.serverFluid);
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
			clientCapacity = dataStream.readInt();
			structure.editMode = ContainerEditMode.values()[dataStream.readInt()];
			
			if(dataStream.readInt() == 1)
			{
				structure.fluidStored = new FluidStack(dataStream.readInt(), dataStream.readInt());
			}
			else {
				structure.fluidStored = null;
			}

			if(isRendering)
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

					TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)data.location.getTileEntity(worldObj);

					if(tileEntity != null)
					{
						tileEntity.clientHasStructure = true;
					}
				}
			}
		}
	}

	public int getScaledFluidLevel(int i)
	{
		if(clientCapacity == 0 || structure.fluidStored == null)
		{
			return 0;
		}

		return structure.fluidStored.amount*i / clientCapacity;
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
}
