package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.IFluidContainerManager;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tank.DynamicTankCache;
import mekanism.common.tank.SynchronizedTankData;
import mekanism.common.tank.SynchronizedTankData.ValveData;
import mekanism.common.tank.TankUpdateProtocol;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityDynamicTank extends TileEntityContainerBlock implements IFluidContainerManager
{
	/** Unique inventory ID for the dynamic tank, serves as a way to retrieve cached inventories. */
	public int inventoryID = -1;

	/** The tank data for this structure. */
	public SynchronizedTankData structure;
	
	/** The cache used by this specific tank segment */
	public DynamicTankCache cachedData = new DynamicTankCache();

	/** Whether or not to send this tank's structure in the next update packet. */
	public boolean sendStructure;

	/** This tank's previous "has structure" state. */
	public boolean prevStructure;

	/** Whether or not this tank has it's structure, for the client side mechanics. */
	public boolean clientHasStructure;

	/** A client-sided and server-sided map of valves on this tank's structure, used on the client for rendering fluids. */
	public Map<ValveData, Integer> valveViewing = new HashMap<ValveData, Integer>();

	/** The capacity this tank has on the client-side. */
	public int clientCapacity;

	/** Whether or not this tank segment is rendering the structure. */
	public boolean isRendering;

	public float prevScale;

	public TileEntityDynamicTank()
	{
		this("DynamicTank");
	}

	public TileEntityDynamicTank(String name)
	{
		super(name);
		inventory = new ItemStack[2];
	}

	public void update()
	{
		if(!worldObj.isRemote && (structure == null || !structure.didTick))
		{
			new TankUpdateProtocol(this).updateTanks();

			if(structure != null)
			{
				structure.didTick = true;
			}
		}
	}

	@Override
	public void onUpdate()
	{
		if(worldObj.isRemote)
		{
			if(structure == null)
			{
				structure = new SynchronizedTankData();
			}

			if(structure != null && clientHasStructure && isRendering)
			{
				for(ValveData data : valveViewing.keySet())
				{
					if(valveViewing.get(data) > 0)
					{
						valveViewing.put(data, valveViewing.get(data)-1);
					}
				}

				if(!prevStructure)
				{
					Mekanism.proxy.doTankAnimation(this);
				}

				float targetScale = (float)(structure.fluidStored != null ? structure.fluidStored.amount : 0)/clientCapacity;

				if(Math.abs(prevScale - targetScale) > 0.01)
				{
					prevScale = (9*prevScale + targetScale)/10;
				}
			}

			prevStructure = clientHasStructure;

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

		if(playersUsing.size() > 0 && ((worldObj.isRemote && !clientHasStructure) || (!worldObj.isRemote && structure == null)))
		{
			for(EntityPlayer player : playersUsing)
			{
				player.closeScreen();
			}
		}

		if(!worldObj.isRemote)
		{
			if(structure == null)
			{
				isRendering = false;
			}

			if(inventoryID != -1 && structure == null)
			{
				MekanismUtils.updateCache(inventoryID, cachedData, this);
			}

			if(structure == null && ticker == 5)
			{
				update();
			}

			if(prevStructure != (structure != null))
			{
				if(structure != null && !structure.hasRenderer)
				{
					structure.hasRenderer = true;
					isRendering = true;
					sendStructure = true;
				}

				for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
				{
					Coord4D obj = Coord4D.get(this).getFromSide(side);

					if(!(obj.getTileEntity(worldObj) instanceof TileEntityDynamicTank))
					{
						worldObj.notifyBlockOfNeighborChange(obj.xCoord, obj.yCoord, obj.zCoord, getBlockType());
					}
				}

				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
			}

			prevStructure = structure != null;

			if(structure != null)
			{
				structure.didTick = false;

				if(inventoryID != -1)
				{
					cachedData.sync(structure);
					MekanismUtils.updateCache(inventoryID, cachedData, this);
				}

				manageInventory();
			}
		}
	}

	public void manageInventory()
	{
		int max = structure.volume*TankUpdateProtocol.FLUID_PER_TANK;

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
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(isRendering);
		data.add(structure != null);
		
		if(structure != null)
		{
			data.add(structure.volume*TankUpdateProtocol.FLUID_PER_TANK);
			data.add(structure.editMode.ordinal());
		}

		if(structure != null && structure.fluidStored != null)
		{
			data.add(1);
			data.add(structure.fluidStored.fluidID);
			data.add(structure.fluidStored.amount);
		}
		else {
			data.add(0);
		}

		if(structure != null && isRendering)
		{
			if(sendStructure)
			{
				sendStructure = false;

				data.add(true);

				data.add(structure.volHeight);
				data.add(structure.volWidth);
				data.add(structure.volLength);

				structure.renderLocation.write(data);
			}
			else {
				data.add(false);
			}

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

		if(structure == null)
		{
			structure = new SynchronizedTankData();
		}

		isRendering = dataStream.readBoolean();
		clientHasStructure = dataStream.readBoolean();
		
		if(clientHasStructure)
		{
			clientCapacity = dataStream.readInt();
			structure.editMode = ContainerEditMode.values()[dataStream.readInt()];
		}

		if(dataStream.readInt() == 1)
		{
			structure.fluidStored = new FluidStack(dataStream.readInt(), dataStream.readInt());
		}
		else {
			structure.fluidStored = null;
		}

		if(clientHasStructure && isRendering)
		{
			if(dataStream.readBoolean())
			{
				structure.volHeight = dataStream.readInt();
				structure.volWidth = dataStream.readInt();
				structure.volLength = dataStream.readInt();

				structure.renderLocation = Coord4D.read(dataStream);
			}

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

	public void sendPacketToRenderer()
	{
		if(structure != null)
		{
			for(Coord4D obj : structure.locations)
			{
				TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)obj.getTileEntity(worldObj);

				if(tileEntity != null && tileEntity.isRendering)
				{
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(tileEntity)));
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
	public ItemStack getStackInSlot(int slotID)
	{
		return structure != null ? structure.inventory[slotID] : null;
	}

	@Override
	public void setInventorySlotContents(int slotID, ItemStack itemstack)
	{
		if(structure != null)
		{
			structure.inventory[slotID] = itemstack;

			if(itemstack != null && itemstack.stackSize > getInventoryStackLimit())
			{
				itemstack.stackSize = getInventoryStackLimit();
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		if(structure == null)
		{
			inventoryID = nbtTags.getInteger("inventoryID");

			if(inventoryID != -1)
			{
				cachedData.load(nbtTags);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setInteger("inventoryID", inventoryID);

		if(inventoryID != -1)
		{
			cachedData.save(nbtTags);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
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
	public boolean handleInventory()
	{
		return false;
	}
}
