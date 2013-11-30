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

import mekanism.api.energy.IEnergizedItem;
import mekanism.common.Mekanism;
import mekanism.common.Object3D;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tileentity.TileEntityElectricBlock;
import mekanism.common.util.CableUtils;
import mekanism.common.util.ListUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.induction.common.BatteryUpdateProtocol;
import mekanism.induction.common.SynchronizedBatteryData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.item.IItemElectric;

import com.google.common.io.ByteArrayDataInput;

/**
 * A modular battery with no GUI.
 * 
 * @author AidanBrady
 */
public class TileEntityBattery extends TileEntityElectricBlock
{
	public Set<EntityPlayer> playersUsing = new HashSet<EntityPlayer>();

	public SynchronizedBatteryData structure = SynchronizedBatteryData.getBase(this);

	public SynchronizedBatteryData prevStructure;

	public double clientEnergy;
	public int clientCells;
	public double clientMaxEnergy;
	public int clientVolume;

	private EnumSet inputSides = EnumSet.allOf(ForgeDirection.class);
	
	public TileEntityBattery()
	{
		super("Battery", 0);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if(!worldObj.isRemote)
		{
			if(ticker == 5 && !structure.isMultiblock)
			{
				update();
			}

			if(structure.visibleInventory[0] != null)
			{
				if(structure.inventory.size() < structure.getMaxCells())
				{
					if(structure.visibleInventory[0].itemID == Mekanism.EnergyTablet.itemID)
					{
						structure.inventory.add(structure.visibleInventory[0]);
						structure.visibleInventory[0] = null;
						structure.sortInventory();
						updateAllClients();
					}
				}
			}
			
			if(structure.visibleInventory[1] != null)
			{
				ItemStack itemStack = structure.visibleInventory[1];
				IEnergizedItem battery = (IEnergizedItem)itemStack.getItem();

				double energyStored = getMaxEnergy();
				double batteryNeeded = battery.getMaxEnergy(itemStack) - battery.getEnergy(itemStack);
				double toGive = Math.min(energyStored, Math.min(battery.getMaxTransfer(itemStack), batteryNeeded));

				battery.setEnergy(itemStack, battery.getEnergy(itemStack) + remove(toGive, true));
			}

			if(structure.visibleInventory[2] != null)
			{
				ItemStack itemStack = structure.visibleInventory[2];
				IEnergizedItem battery = (IEnergizedItem)itemStack.getItem();

				double energyNeeded = getMaxEnergy() - getEnergy();
				double batteryStored = battery.getEnergy(itemStack);
				double toReceive = Math.min(energyNeeded, Math.min(battery.getMaxTransfer(itemStack), batteryStored));
				battery.setEnergy(itemStack, battery.getEnergy(itemStack) - add(toReceive, true));
			}

			if(prevStructure != structure)
			{
				for(EntityPlayer player : playersUsing)
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

			for(EntityPlayer player : playersUsing)
			{
				PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())), player);
			}

			CableUtils.emit(this);
		}
	}

	public void updateClient()
	{
		PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())));
	}

	public void updateAllClients()
	{
		for(Object3D vec : structure.locations)
		{
			TileEntityBattery battery = (TileEntityBattery)vec.getTileEntity(worldObj);
			PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(battery), battery.getNetworkedData(new ArrayList())));
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

			for(int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
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

			for(int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
			{
				NBTTagCompound tagCompound = (NBTTagCompound) tagList.tagAt(tagCount);
				byte slotID = tagCompound.getByte("Slot");

				if(slotID >= 0 && slotID < structure.visibleInventory.length)
				{
					if(slotID == 0)
					{
						setInventorySlotContents(slotID, ItemStack.loadItemStackFromNBT(tagCompound));
					}
					else {
						setInventorySlotContents(slotID + 1, ItemStack.loadItemStackFromNBT(tagCompound));
					}
				}
			}
		}

		inputSides = EnumSet.noneOf(ForgeDirection.class);

		NBTTagList tagList = nbtTags.getTagList("inputSides");

		for(int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
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
			//Inventory
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

			//Visible inventory
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

			while(it.hasNext())
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

	public double add(double amount, boolean doAdd)
	{
		double added = 0;

		for(ItemStack itemStack : structure.inventory)
		{
			if(itemStack.getItem() instanceof IEnergizedItem)
			{
				IEnergizedItem battery = (IEnergizedItem)itemStack.getItem();

				double needed = amount - added;
				double itemAdd = Math.min(battery.getMaxEnergy(itemStack) - battery.getEnergy(itemStack), needed);

				if(doAdd)
				{
					battery.setEnergy(itemStack, battery.getEnergy(itemStack) + itemAdd);
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

	public double remove(double amount, boolean doRemove)
	{
		List<ItemStack> inverse = ListUtils.inverse(structure.inventory);

		float removed = 0;
		
		for(ItemStack itemStack : inverse)
		{
			if(itemStack.getItem() instanceof IEnergizedItem)
			{
				IEnergizedItem battery = (IEnergizedItem)itemStack.getItem();

				double needed = amount - removed;
				double itemRemove = Math.min(battery.getEnergy(itemStack), needed);

				if(doRemove)
				{
					battery.setEnergy(itemStack, battery.getEnergy(itemStack) - itemRemove);
				}

				removed += itemRemove;

				if(amount == removed)
				{
					break;
				}
			}
		}

		return removed;
	}
	
	@Override
	public void setEnergy(double energy)
	{
		double stored = getEnergy();
		
		if(energy > stored)
		{
			add(energy-stored, true);
		}
		else if(energy < stored)
		{
			remove(stored-energy, true);
		}
		
		MekanismUtils.saveChunk(this);
	}

	@Override
	public double getMaxEnergy()
	{
		if(!worldObj.isRemote)
		{
			float max = 0;

			for(ItemStack itemStack : structure.inventory)
			{
				if(itemStack != null)
				{
					if(itemStack.getItem() instanceof IEnergizedItem)
					{
						max += ((IEnergizedItem)itemStack.getItem()).getMaxEnergy(itemStack);
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
	public double getEnergy()
	{
		if(!worldObj.isRemote)
		{
			double energy = 0;

			for(ItemStack itemStack : structure.inventory)
			{
				if(itemStack != null)
				{
					if(itemStack.getItem() instanceof IEnergizedItem)
					{
						energy += ((IEnergizedItem)itemStack.getItem()).getEnergy(itemStack);
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

		clientEnergy = input.readDouble();
		clientCells = input.readInt();
		clientMaxEnergy = input.readDouble();
		clientVolume = input.readInt();
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(structure.isMultiblock);

		data.add(getEnergy());
		data.add(structure.inventory.size());
		data.add(getMaxEnergy());
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
				return ListUtils.getTop(structure.inventory);
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
					structure.inventory.remove(ListUtils.getTop(structure.inventory));
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
	public boolean isItemValidForSlot(int i, ItemStack itemStack)
	{
		return itemStack.getItem() instanceof IItemElectric;
	}

	@Override
	public EnumSet<ForgeDirection> getConsumingSides()
	{
		return inputSides;
	}

	@Override
	public EnumSet<ForgeDirection> getOutputtingSides()
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
