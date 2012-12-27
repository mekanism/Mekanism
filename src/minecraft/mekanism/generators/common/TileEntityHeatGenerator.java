package mekanism.generators.common;

import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import mekanism.common.LiquidSlot;
import mekanism.common.Mekanism;
import mekanism.common.MekanismUtils;
import mekanism.common.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;
import universalelectricity.core.implement.IItemElectric;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;

public class TileEntityHeatGenerator extends TileEntityGenerator implements ITankContainer
{
	/** The LiquidSlot fuel instance for this generator. */
	public LiquidSlot fuelSlot = new LiquidSlot(24000, Mekanism.hooks.BuildCraftFuelID);
	
	public TileEntityHeatGenerator()
	{
		super("Heat Generator", 160000, 512);
		inventory = new ItemStack[2];
	}
	
	@Override
	public void onUpdate()
	{		
		if(inventory[1] != null && electricityStored > 0)
		{
			if(inventory[1].getItem() instanceof IItemElectric)
			{
				IItemElectric electricItem = (IItemElectric)inventory[1].getItem();
				double sendingElectricity = 0;
				double actualSendingElectricity = 0;
				double rejectedElectricity = 0;
				double itemElectricityNeeded = electricItem.getMaxJoules(inventory[1]) - electricItem.getJoules(inventory[1]);
				
				if(electricItem.getVoltage() <= electricityStored)
				{
					sendingElectricity = electricItem.getVoltage();
				}
				else if(electricItem.getVoltage() > electricityStored)
				{
					sendingElectricity = electricityStored;
				}
				
				if(sendingElectricity <= itemElectricityNeeded)
				{
					actualSendingElectricity = sendingElectricity;
				}
				else if(sendingElectricity > itemElectricityNeeded)
				{
					rejectedElectricity = sendingElectricity-itemElectricityNeeded;
					actualSendingElectricity = itemElectricityNeeded;
				}
				
				electricItem.setJoules((electricItem.getJoules(inventory[1]) + actualSendingElectricity), inventory[1]);
				setJoules(electricityStored - (actualSendingElectricity - rejectedElectricity));
			}
			else if(inventory[1].getItem() instanceof IElectricItem)
			{
				double sent = ElectricItem.charge(inventory[1], (int)(electricityStored*Mekanism.TO_IC2), 3, false, false)*Mekanism.FROM_IC2;
				setJoules(electricityStored - sent);
			}
		}
		
		if(inventory[0] != null && fuelSlot.liquidStored < fuelSlot.MAX_LIQUID)
		{
			if(Mekanism.hooks.BuildCraftLoaded)
			{
				if(inventory[0].itemID == Mekanism.hooks.BuildCraftFuelBucket.itemID)
				{
					fuelSlot.setLiquid(fuelSlot.liquidStored + 1000);
					inventory[0] = new ItemStack(Item.bucketEmpty);
				}
			}
			
			int fuel = getFuel(inventory[0]);
			ItemStack prevStack = inventory[0].copy();
			if(fuel > 0)
			{
				int fuelNeeded = fuelSlot.MAX_LIQUID - fuelSlot.liquidStored;
				if(fuel <= fuelNeeded)
				{
					fuelSlot.liquidStored += fuel;
					--inventory[0].stackSize;
<<<<<<< HEAD
					
					if(prevStack != null && Mekanism.hooks.BuildCraftFuelBucket != null)
					{
						if(prevStack.itemID == Mekanism.hooks.BuildCraftFuelBucket.itemID)
						{
							inventory[0] = Mekanism.hooks.BuildCraftFuelBucket;
						}
					
					}
=======
>>>>>>> v5.0.2 Release
				}
				
				if(inventory[0].stackSize == 0)
				{
					inventory[0] = null;
				}
			}
		}
		
		if(electricityStored < MAX_ELECTRICITY)
		{
			setJoules(electricityStored + getEnvironmentBoost());
			
			if(canOperate())
			{	
				if(!worldObj.isRemote)
				{
					setActive(true);
				}
				fuelSlot.setLiquid(fuelSlot.liquidStored - 10);
				setJoules(electricityStored + 400);
			}
			else {
				if(!worldObj.isRemote)
				{
					setActive(false);
				}
			}
		}
	}
	
	@Override
	public boolean canOperate()
	{
		return electricityStored < MAX_ELECTRICITY && fuelSlot.liquidStored > 0;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        
        fuelSlot.liquidStored = nbtTags.getInteger("fuelStored");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("fuelStored", fuelSlot.liquidStored);
    }
	
	@Override
	public int getEnvironmentBoost()
	{
		int boost = 0;
		
		if(worldObj.getBlockId(xCoord+1, yCoord, zCoord) == 10 || worldObj.getBlockId(xCoord+1, yCoord, zCoord) == 11)
			boost+=200;
		if(worldObj.getBlockId(xCoord-1, yCoord, zCoord) == 10 || worldObj.getBlockId(xCoord-1, yCoord, zCoord) == 11)
			boost+=200;
		if(worldObj.getBlockId(xCoord, yCoord+1, zCoord) == 10 || worldObj.getBlockId(xCoord, yCoord+1, zCoord) == 11)
			boost+=200;
		if(worldObj.getBlockId(xCoord, yCoord-1, zCoord) == 10 || worldObj.getBlockId(xCoord, yCoord-1, zCoord) == 11)
			boost+=200;
		if(worldObj.getBlockId(xCoord, yCoord, zCoord+1) == 10 || worldObj.getBlockId(xCoord, yCoord, zCoord+1) == 11)
			boost+=200;
		if(worldObj.getBlockId(xCoord, yCoord, zCoord-1) == 10 || worldObj.getBlockId(xCoord, yCoord, zCoord-1) == 11)
			boost+=200;
		
		return boost;
	}

	public int getFuel(ItemStack itemstack)
	{
		return TileEntityFurnace.getItemBurnTime(itemstack);
	}
	
	@Override
	public int getStartInventorySide(ForgeDirection side) 
	{
		if(side == MekanismUtils.getRight(facing))
		{
			return 1;
		}
		
		return 0;
	}
	
	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		return 1;
	}
	
	/**
	 * Gets the scaled fuel level for the GUI.
	 * @param i - multiplier
	 * @return
	 */
	public int getScaledFuelLevel(int i)
	{
		return fuelSlot.liquidStored*i / fuelSlot.MAX_LIQUID;
	}
	
	@Override
	public void handlePacketData(INetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try {
			facing = dataStream.readInt();
			electricityStored = dataStream.readDouble();
			isActive = dataStream.readBoolean();
			fuelSlot.liquidStored = dataStream.readInt();
			worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
		} catch (Exception e)
		{
			System.out.println("[Mekanism] Error while handling tile entity packet.");
			e.printStackTrace();
		}
	}
	
	@Override
	public void sendPacket()
	{
		PacketHandler.sendTileEntityPacketToClients(this, 0, facing, electricityStored, isActive, fuelSlot.liquidStored);
	}
	
	@Override
	public void sendPacketWithRange()
	{
		PacketHandler.sendTileEntityPacketToClients(this, 50, facing, electricityStored, isActive, fuelSlot.liquidStored);
	}
	
	@Override
	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getFuel", "getFuelNeeded"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				return new Object[] {output};
			case 2:
				return new Object[] {MAX_ELECTRICITY};
			case 3:
				return new Object[] {(MAX_ELECTRICITY-electricityStored)};
			case 4:
				return new Object[] {fuelSlot.liquidStored};
			case 5:
				return new Object[] {fuelSlot.MAX_LIQUID-fuelSlot.liquidStored};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return null;
		}
	}

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) 
	{
		if(from != ForgeDirection.getOrientation(facing))
		{
			if(resource.itemID == Mekanism.hooks.BuildCraftFuelID)
			{
				int fuelTransfer = 0;
				int fuelNeeded = fuelSlot.MAX_LIQUID - fuelSlot.liquidStored;
				int attemptTransfer = resource.amount;
				
				if(attemptTransfer <= fuelNeeded)
				{
					fuelTransfer = attemptTransfer;
				}
				else {
					fuelTransfer = fuelNeeded;
				}
				
				if(doFill)
				{
					fuelSlot.setLiquid(fuelSlot.liquidStored + fuelTransfer);
				}
				
				return fuelTransfer;
			}
		}
		
		return 0;
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill) 
	{
		return 0;
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) 
	{
		return null;
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain) 
	{
		return null;
	}

	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction) 
	{
		return new ILiquidTank[] {new LiquidTank(fuelSlot.liquidID, fuelSlot.liquidStored, fuelSlot.MAX_LIQUID)};
	}
	
	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type)
	{
		return null;
	}
}
