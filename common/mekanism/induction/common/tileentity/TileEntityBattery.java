/**
 * 
 */
package mekanism.induction.common.tileentity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.api.induction.ICapacitor;
import mekanism.common.ITileNetwork;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketTileEntity;
import mekanism.induction.common.BatteryUpdateProtocol;
import mekanism.induction.common.ListUtil;
import mekanism.induction.common.SynchronizedBatteryData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.compatibility.TileEntityUniversalElectrical;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.item.IItemElectric;
import universalelectricity.core.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * A modular battery with no GUI.
 * 
 * @author AidanBrady
 */
public class TileEntityBattery extends TileEntityUniversalElectrical implements ITileNetwork, IInventory
{
	public Set<EntityPlayer> playersUsing = new HashSet<EntityPlayer>();

	public SynchronizedBatteryData structure = SynchronizedBatteryData.getBase(this);

	public SynchronizedBatteryData prevStructure;

	public float clientEnergy;
	public int clientCells;
	public float clientMaxEnergy;
	public int clientVolume;

	private EnumSet inputSides = EnumSet.allOf(ForgeDirection.class);

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if(!worldObj.isRemote)
		{
			if(ticks == 5 && !structure.isMultiblock)
			{
				update();
			}

			if(structure.visibleInventory[0] != null)
			{
				if(structure.inventory.size() < structure.getMaxCells())
				{
					if(structure.visibleInventory[0].getItem() instanceof ICapacitor)
					{
						structure.inventory.add(structure.visibleInventory[0]);
						structure.visibleInventory[0] = null;
						structure.sortInventory();
						updateAllClients();
					}
				}
			}

			/**
			 * Attempt to charge entities above it.
			 */
			ItemStack chargeItem = null;

			if(worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
			{
				List<Entity> entities = worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(xCoord, yCoord + 1, zCoord, xCoord + 1, yCoord + 2, zCoord + 1));

				electricItemLoop:
				for (Entity entity : entities)
				{
					if(entity instanceof EntityPlayer)
					{
						IInventory inventory = ((EntityPlayer) entity).inventory;
						for (int i = 0; i < inventory.getSizeInventory(); i++)
						{
							ItemStack checkStack = inventory.getStackInSlot(i);

							if(checkStack != null)
							{
								if(checkStack.getItem() instanceof IItemElectric)
								{
									if(((IItemElectric) checkStack.getItem()).recharge(checkStack, provideElectricity(getTransferThreshhold(), false).getWatts(), false) > 0)
									{
										chargeItem = checkStack;
										break electricItemLoop;
									}
								}
							}
						}
					}
					else if(entity instanceof EntityItem)
					{
						ItemStack checkStack = ((EntityItem) entity).getEntityItem();

						if(checkStack != null)
						{
							if(checkStack.getItem() instanceof IItemElectric)
							{
								if(((IItemElectric) checkStack.getItem()).recharge(checkStack, provideElectricity(getTransferThreshhold(), false).getWatts(), false) > 0)
								{
									chargeItem = checkStack;
									break electricItemLoop;
								}
							}
						}
					}
				}
			}

			if(chargeItem == null)
			{
				chargeItem = structure.visibleInventory[1];
			}

			if(chargeItem != null)
			{
				ItemStack itemStack = chargeItem;
				IItemElectric battery = (IItemElectric) itemStack.getItem();

				float energyStored = getMaxEnergyStored();
				float batteryNeeded = battery.recharge(itemStack, provideElectricity(getTransferThreshhold(), false).getWatts(), false);
				float toGive = Math.min(energyStored, Math.min(battery.getTransfer(itemStack), batteryNeeded));
				battery.recharge(itemStack, provideElectricity(toGive, true).getWatts(), true);
			}

			if(structure.visibleInventory[2] != null)
			{
				ItemStack itemStack = structure.visibleInventory[2];
				IItemElectric battery = (IItemElectric) itemStack.getItem();

				float energyNeeded = getMaxEnergyStored() - getEnergyStored();
				float batteryStored = battery.getElectricityStored(itemStack);
				float toReceive = Math.min(energyNeeded, Math.min(getTransferThreshhold(), Math.min(battery.getTransfer(itemStack), batteryStored)));
				battery.discharge(itemStack, receiveElectricity(toReceive, true), true);
			}

			if(prevStructure != structure)
			{
				for (EntityPlayer player : playersUsing)
				{
					player.closeScreen();
				}

				updateClient();
			}

			prevStructure = structure;

			structure.wroteInventory = false;
			structure.didTick = false;

			if(playersUsing.size() > 0)
			{
				updateClient();
			}

			for (EntityPlayer player : playersUsing)
			{
				PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())), player);
			}

			produce();
		}
	}

	public float getTransferThreshhold()
	{
		return structure.getVolume() * 50;
	}

	public void updateClient()
	{
		PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())));
	}

	public void updateAllClients()
	{
		for (Vector3 vec : structure.locations)
		{
			TileEntityBattery battery = (TileEntityBattery) vec.getTileEntity(worldObj);
			PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(battery), battery.getNetworkedData(new ArrayList())));
		}
	}

	@Override
	public void validate()
	{
		super.validate();
		
		if(worldObj.isRemote)
		{
			PacketHandler.sendPacket(Transmission.SERVER, new PacketDataRequest().setParams(Object3D.get(this)));
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		// Main inventory
		if(nbtTags.hasKey("Items"))
		{
			NBTTagList tagList = nbtTags.getTagList("Items");
			structure.inventory = new ArrayList<ItemStack>();

			for (int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
			{
				NBTTagCompound tagCompound = (NBTTagCompound) tagList.tagAt(tagCount);
				int slotID = tagCompound.getInteger("Slot");
				structure.inventory.add(slotID, ItemStack.loadItemStackFromNBT(tagCompound));
			}
		}

		// Visible inventory
		if(nbtTags.hasKey("VisibleItems"))
		{
			NBTTagList tagList = nbtTags.getTagList("VisibleItems");
			structure.visibleInventory = new ItemStack[3];

			for (int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
			{
				NBTTagCompound tagCompound = (NBTTagCompound) tagList.tagAt(tagCount);
				byte slotID = tagCompound.getByte("Slot");

				if(slotID >= 0 && slotID < structure.visibleInventory.length)
				{
					if(slotID == 0)
					{
						setInventorySlotContents(slotID, ItemStack.loadItemStackFromNBT(tagCompound));
					}
					else
					{
						setInventorySlotContents(slotID + 1, ItemStack.loadItemStackFromNBT(tagCompound));
					}
				}
			}
		}

		inputSides = EnumSet.noneOf(ForgeDirection.class);

		NBTTagList tagList = nbtTags.getTagList("inputSides");

		for (int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
		{
			NBTTagCompound tagCompound = (NBTTagCompound) tagList.tagAt(tagCount);
			byte side = tagCompound.getByte("side");
			inputSides.add(ForgeDirection.getOrientation(side));
		}

		inputSides.remove(ForgeDirection.UNKNOWN);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		if(!structure.wroteInventory)
		{
			// Inventory
			if(structure.inventory != null)
			{
				NBTTagList tagList = new NBTTagList();

				for (int slotCount = 0; slotCount < structure.inventory.size(); slotCount++)
				{
					if(structure.inventory.get(slotCount) != null)
					{
						NBTTagCompound tagCompound = new NBTTagCompound();
						tagCompound.setInteger("Slot", slotCount);
						structure.inventory.get(slotCount).writeToNBT(tagCompound);
						tagList.appendTag(tagCompound);
					}
				}

				nbt.setTag("Items", tagList);
			}

			// Visible inventory
			if(structure.visibleInventory != null)
			{
				NBTTagList tagList = new NBTTagList();

				for (int slotCount = 0; slotCount < structure.visibleInventory.length; slotCount++)
				{
					if(slotCount > 0)
					{
						slotCount++;
					}

					if(getStackInSlot(slotCount) != null)
					{
						NBTTagCompound tagCompound = new NBTTagCompound();
						tagCompound.setByte("Slot", (byte) slotCount);
						getStackInSlot(slotCount).writeToNBT(tagCompound);
						tagList.appendTag(tagCompound);
					}
				}

				nbt.setTag("VisibleItems", tagList);
			}

			structure.wroteInventory = true;

			/**
			 * Save the input sides.
			 */
			NBTTagList tagList = new NBTTagList();
			Iterator<ForgeDirection> it = inputSides.iterator();

			while (it.hasNext())
			{
				ForgeDirection dir = it.next();

				if(dir != ForgeDirection.UNKNOWN)
				{
					NBTTagCompound tagCompound = new NBTTagCompound();
					tagCompound.setByte("side", (byte) dir.ordinal());
					tagList.appendTag(tagCompound);
				}
			}

			nbt.setTag("inputSides", tagList);
		}
	}

	public void update()
	{
		if(!worldObj.isRemote && (structure == null || !structure.didTick))
		{
			new BatteryUpdateProtocol(this).updateBatteries();

			if(structure != null)
			{
				structure.didTick = true;
			}
		}
	}

	@Override
	public float receiveElectricity(ElectricityPack receive, boolean doAdd)
	{
		float amount = receive.getWatts();
		float added = 0;

		for (ItemStack itemStack : structure.inventory)
		{
			if(itemStack.getItem() instanceof IItemElectric)
			{
				IItemElectric battery = (IItemElectric) itemStack.getItem();

				float needed = amount - added;
				float itemAdd = Math.min(battery.getMaxElectricityStored(itemStack) - battery.getElectricityStored(itemStack), needed);

				if(doAdd)
				{
					battery.setElectricity(itemStack, battery.getElectricityStored(itemStack) + itemAdd);
				}

				added += itemAdd;

				if(amount == added)
				{
					break;
				}
			}
		}

		return added;
	}

	@Override
	public ElectricityPack provideElectricity(ElectricityPack pack, boolean doRemove)
	{
		float amount = pack.getWatts();

		List<ItemStack> inverse = ListUtil.inverse(structure.inventory);

		float removed = 0;
		for (ItemStack itemStack : inverse)
		{
			if(itemStack.getItem() instanceof IItemElectric)
			{
				IItemElectric battery = (IItemElectric) itemStack.getItem();

				float needed = amount - removed;
				float itemRemove = Math.min(battery.getElectricityStored(itemStack), needed);

				if(doRemove)
				{
					battery.setElectricity(itemStack, battery.getElectricityStored(itemStack) - itemRemove);
				}

				removed += itemRemove;

				if(amount == removed)
				{
					break;
				}
			}
		}

		return ElectricityPack.getFromWatts(removed, getVoltage());
	}

	@Override
	public float getMaxEnergyStored()
	{
		if(!worldObj.isRemote)
		{
			float max = 0;

			for (ItemStack itemStack : structure.inventory)
			{
				if(itemStack != null)
				{
					if(itemStack.getItem() instanceof IItemElectric)
					{
						max += ((IItemElectric) itemStack.getItem()).getMaxElectricityStored(itemStack);
					}
				}
			}

			return max;
		}
		else {
			return clientMaxEnergy;
		}
	}

	@Override
	public float getEnergyStored()
	{
		if(!worldObj.isRemote)
		{
			float energy = 0;

			for(ItemStack itemStack : structure.inventory)
			{
				if(itemStack != null)
				{
					if(itemStack.getItem() instanceof IItemElectric)
					{
						energy += ((IItemElectric) itemStack.getItem()).getElectricityStored(itemStack);
					}
				}
			}

			return energy;
		}
		else {
			return clientEnergy;
		}
	}

	@Override
	public void handlePacketData(ByteArrayDataInput input)
	{
		structure.isMultiblock = input.readBoolean();

		clientEnergy = input.readFloat();
		clientCells = input.readInt();
		clientMaxEnergy = input.readFloat();
		clientVolume = input.readInt();
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(structure.isMultiblock);

		data.add(getEnergyStored());
		data.add(structure.inventory.size());
		data.add(getMaxEnergyStored());
		data.add(structure.getVolume());

		return data;
	}

	@Override
	public int getSizeInventory()
	{
		return 4;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		if(i == 0)
		{
			return structure.visibleInventory[0];
		}
		else if(i == 1)
		{
			if(!worldObj.isRemote)
			{
				return ListUtil.getTop(structure.inventory);
			}
			else {
				return structure.tempStack;
			}
		}
		else {
			return structure.visibleInventory[i - 1];
		}
	}

	@Override
	public ItemStack decrStackSize(int slotID, int amount)
	{
		if(getStackInSlot(slotID) != null)
		{
			ItemStack tempStack;

			if(getStackInSlot(slotID).stackSize <= amount)
			{
				tempStack = getStackInSlot(slotID);
				setInventorySlotContents(slotID, null);
				return tempStack;
			}
			else {
				tempStack = getStackInSlot(slotID).splitStack(amount);

				if(getStackInSlot(slotID).stackSize == 0)
				{
					setInventorySlotContents(slotID, null);
				}

				return tempStack;
			}
		}
		else {
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		return getStackInSlot(i);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		if(i == 0)
		{
			structure.visibleInventory[0] = itemstack;
		}
		else if(i == 1)
		{
			if(itemstack == null)
			{
				if(!worldObj.isRemote)
				{
					structure.inventory.remove(ListUtil.getTop(structure.inventory));
				}
				else {
					structure.tempStack = null;
				}
			}
			else {
				if(worldObj.isRemote)
				{
					structure.tempStack = itemstack;
				}
			}
		}
		else {
			structure.visibleInventory[i - 1] = itemstack;
		}
	}

	@Override
	public String getInvName()
	{
		return "Battery";
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public void openChest() {}

	@Override
	public void closeChest() {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemsSack)
	{
		return itemsSack.getItem() instanceof IItemElectric;
	}

	@Override
	public float getRequest(ForgeDirection direction)
	{
		if(getInputDirections().contains(direction))
		{
			return Math.min(getMaxEnergyStored() - getEnergyStored(), getTransferThreshhold());
		}
		
		return 0;
	}

	@Override
	public float getProvide(ForgeDirection direction)
	{
		if(getOutputDirections().contains(direction))
		{
			return Math.min(getEnergyStored(), getTransferThreshhold());
		}

		return 0;
	}

	@Override
	public EnumSet<ForgeDirection> getInputDirections()
	{
		return inputSides;
	}

	@Override
	public EnumSet<ForgeDirection> getOutputDirections()
	{
		return EnumSet.complementOf(inputSides);
	}

	/**
	 * Toggles the input/output sides of the battery.
	 */
	public boolean toggleSide(ForgeDirection orientation)
	{
		if(inputSides.contains(orientation))
		{
			inputSides.remove(orientation);
			return false;
		}
		else {
			inputSides.add(orientation);
			return true;
		}
	}
}
