package mekanism.common.tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tank.SynchronizedTankData;
import mekanism.common.tank.TankUpdateProtocol;
import mekanism.common.tank.SynchronizedTankData.ValveData;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityDynamicTank extends TileEntityContainerBlock
{
	/** Unique inventory ID for the dynamic tank, serves as a way to retrieve cached inventories. */
	public int inventoryID = -1;

	/** The tank data for this structure. */
	public SynchronizedTankData structure;

	/** Whether or not to send this tank's structure in the next update packet. */
	public boolean sendStructure;

	/** This tank's previous "has structure" state. */
	public boolean prevStructure;

	/** Whether or not this tank has it's structure, for the client side mechanics. */
	public boolean clientHasStructure;

	/** The cached fluid this tank segment contains. */
	public FluidStack cachedFluid;

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
				MekanismUtils.updateCache(inventoryID, cachedFluid, inventory, this);
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
						worldObj.notifyBlockOfNeighborChange(obj.xCoord, obj.yCoord, obj.zCoord, getBlockType().blockID);
					}
				}

				PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Coord4D.get(this), getNetworkedData(new ArrayList())));
			}

			prevStructure = structure != null;

			if(structure != null)
			{
				structure.didTick = false;

				if(inventoryID != -1)
				{
					MekanismUtils.updateCache(inventoryID, structure.fluidStored, structure.inventory, this);

					cachedFluid = structure.fluidStored;
					inventory = structure.inventory;
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
			if(FluidContainerRegistry.isEmptyContainer(structure.inventory[0]))
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

							onInventoryChanged();

							structure.fluidStored.amount -= FluidContainerRegistry.getFluidForFilledItem(filled).amount;

							if(structure.fluidStored.amount == 0)
							{
								structure.fluidStored = null;
							}

							PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Coord4D.get(this), getNetworkedData(new ArrayList())));
						}
					}
				}
			}
			else if(FluidContainerRegistry.isFilledContainer(structure.inventory[0]))
			{
				FluidStack itemFluid = FluidContainerRegistry.getFluidForFilledItem(structure.inventory[0]);

				if((structure.fluidStored == null && itemFluid.amount <= max) || structure.fluidStored.amount+itemFluid.amount <= max)
				{
					if(structure.fluidStored != null && !structure.fluidStored.isFluidEqual(itemFluid))
					{
						return;
					}

					ItemStack containerItem = structure.inventory[0].getItem().getContainerItemStack(structure.inventory[0]);

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

							onInventoryChanged();
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
					}

					PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Coord4D.get(this), getNetworkedData(new ArrayList())));
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
		data.add(structure != null ? structure.volume*TankUpdateProtocol.FLUID_PER_TANK : 0);

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
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);

		if(structure == null)
		{
			structure = new SynchronizedTankData();
		}

		isRendering = dataStream.readBoolean();
		clientHasStructure = dataStream.readBoolean();

		clientCapacity = dataStream.readInt();

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
					PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Coord4D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())));
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
				if(nbtTags.hasKey("cachedFluid"))
				{
					cachedFluid = FluidStack.loadFluidStackFromNBT(nbtTags.getCompoundTag("cachedFluid"));
				}
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setInteger("inventoryID", inventoryID);

		if(cachedFluid != null)
		{
			nbtTags.setTag("cachedFluid", cachedFluid.writeToNBT(new NBTTagCompound()));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}
}
