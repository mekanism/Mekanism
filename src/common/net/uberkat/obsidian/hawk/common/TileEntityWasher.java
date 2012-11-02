package net.uberkat.obsidian.hawk.common;

import hawk.api.ProcessingRecipes;
import hawk.api.ProcessingRecipes.EnumProcessing;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import obsidian.api.IEnergizedItem;

import buildcraft.api.core.Orientations;
import com.google.common.io.ByteArrayDataInput;
import cpw.mods.fml.common.FMLCommonHandler;
import dan200.computer.api.IComputerAccess;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import net.uberkat.obsidian.common.PacketHandler;
import net.uberkat.obsidian.common.TileEntityBasicMachine;
import universalelectricity.electricity.ElectricInfo;
import universalelectricity.prefab.TileEntityElectricityReceiver;
import universalelectricity.implement.IItemElectric;
import universalelectricity.implement.IRedstoneReceptor;
import universalelectricity.implement.IRotatable;
import universalelectricity.prefab.network.IPacketReceiver;
import universalelectricity.prefab.network.PacketManager;

public class TileEntityWasher extends TileEntityDamagableMachine
{
	public static Vector recipes;
	
	public float waterUnits = 0;
	
	public float WATER_LIMIT = 25.0F;
	
	public TileEntityWasher()
	{
		super("hawk/Washer.ogg", "Washer", "/gui/hawk/GuiWasher.png", 10, 200, 1200);
		inventory = new ItemStack[6];
		machineEnum = EnumProcessing.WASHING;
		isProcessor = true;
	}

	public void onUpdate()
	{
		super.onUpdate();
		
		if (inventory[0] != null)
		{
			if(energyStored < currentMaxEnergy)
			{
				if(inventory[0].getItem() instanceof IEnergizedItem)
				{
					int received = 0;
					int energyNeeded = currentMaxEnergy - energyStored;
					IEnergizedItem item = (IEnergizedItem)inventory[0].getItem();
					if(item.getRate() <= energyNeeded)
					{
						received = item.discharge(inventory[0], item.getRate());
					}
					else if(item.getRate() > energyNeeded)
					{
						received = item.discharge(inventory[0], energyNeeded);
					}
					
					setEnergy(energyStored + received);
				}
			}
		}
		
		if (inventory[1] != null)
		{
			if (inventory[1].getItem() == Item.bucketWater && waterUnits + 1.0F <= WATER_LIMIT)
			{
				waterUnits += 1.0;
				inventory[1] = new ItemStack(Item.bucketEmpty, 1);
			}
			
		}
		
		if (canOperate())
		{
			if (inventory[2] != null && operatingTicks == 0)
			{
				operatingTicks = TICKS_REQUIRED;
			}
			
			if (canOperate() && operatingTicks > 0)
			{
				--operatingTicks;
				waterUnits -= 0.01F;
				
				if (operatingTicks == 1)
				{
					operate();
					operatingTicks = 0;
				}
				
				energyStored = energyStored - ENERGY_PER_TICK;
			}
			else
			{
				operatingTicks = 0;
			}
		}
		
		if (waterUnits > WATER_LIMIT)
		{
			waterUnits = WATER_LIMIT;
		}
		
		if (worldObj.getBlockId(xCoord, yCoord + 1, zCoord) == Block.waterStill.blockID && waterUnits + 1.0F <= WATER_LIMIT)
		{
			waterUnits += 1.0F;
			worldObj.setBlockWithNotify(xCoord, yCoord + 1, zCoord, 0);
		}
		
		if (!canOperate() && operatingTicks != 0)
		{
			operatingTicks = 0;
		}
	}
	
	public boolean canOperate()
	{
		if (inventory[2] == null)
		{
			return false;
		}
		else
		{
			if (energyStored >= ENERGY_PER_TICK * 2 && waterUnits >= 1.0F && !isDisabled())
			{
				ItemStack var1 = ProcessingRecipes.getResult(inventory[2], machineEnum);
				if (var1 == null) return false;
				if (inventory[3] == null) return true;
				if (!inventory[3].isItemEqual(var1)) return false;
				int result = inventory[3].stackSize + var1.stackSize;
				return (result <= getInventoryStackLimit() && result <= var1.getMaxStackSize());
			}
			else
			{
				return false;
			}
		}
	}
	
	public void operate()
	{
		if (canOperate())
		{
			ItemStack newItem = ProcessingRecipes.getResult(inventory[2], machineEnum);
			
			if (inventory[3] == null)
			{
				inventory[3] = newItem.copy();
			}
			else if (inventory[3].isItemEqual(newItem))
			{
				inventory[3].stackSize += newItem.stackSize;
			}
			
			inventory[2].stackSize -= ProcessingRecipes.getQuantity(inventory[2], machineEnum);
			
			if (inventory[2].stackSize <= 0)
			{
				inventory[2] = null;
			}
		}
	}
	
	public void handlePacketData(INetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try {
			facing = dataStream.readInt();
			isActive = dataStream.readByte() != 0;
			operatingTicks = dataStream.readInt();
			energyStored = dataStream.readInt();
			currentMaxEnergy = dataStream.readInt();
			currentTicksRequired = dataStream.readInt();
			waterUnits = dataStream.readFloat();
			worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
		} catch (Exception e)
		{
			System.out.println("[ObsidianIngots] Error while handling tile entity packet.");
			e.printStackTrace();
		}
	}
	
	public int getWashingStatus(int par1)
	{
		return operatingTicks * par1 / 200;
	}

	public void readFromNBT(NBTTagCompound NBTTag)
	{
		super.readFromNBT(NBTTag);
		waterUnits = NBTTag.getFloat("waterUnits");
	}

	public void writeToNBT(NBTTagCompound NBTTag)
	{
		super.writeToNBT(NBTTag);
		NBTTag.setFloat("waterUnits", waterUnits);
	}

	public void sendPacket() 
	{
		PacketHandler.sendWasherPacket(this);
	}

	public void sendPacketWithRange() 
	{
		PacketHandler.sendWasherPacketWithRange(this, 50);
	}
	
	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded", "getWater", "getWaterNeeded"};
	}

	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {energyStored};
			case 1:
				return new Object[] {operatingTicks};
			case 2:
				return new Object[] {isActive};
			case 3:
				return new Object[] {facing};
			case 4:
				return new Object[] {canOperate()};
			case 5:
				return new Object[] {currentMaxEnergy};
			case 6:
				return new Object[] {(currentMaxEnergy-energyStored)};
			case 7:
				return new Object[] {waterUnits};
			case 8:
				return new Object[] {(WATER_LIMIT - waterUnits)};
			default:
				System.err.println("[ObsidianIngots] Attempted to call unknown method with computer ID " + computer.getID());
				return new Object[] {"Unknown command."};
		}
	}

	public List getRecipes() 
	{
		return recipes;
	}
}
