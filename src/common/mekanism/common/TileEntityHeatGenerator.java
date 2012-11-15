package mekanism.common;

import java.io.DataOutputStream;

import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import universalelectricity.core.UniversalElectricity;
import universalelectricity.electricity.ElectricInfo;
import universalelectricity.implement.IItemElectric;
import buildcraft.api.core.Orientations;
import buildcraft.api.liquids.ILiquidTank;
import buildcraft.api.liquids.ITankContainer;
import buildcraft.api.liquids.LiquidStack;
import buildcraft.api.liquids.LiquidTank;
import buildcraft.api.power.PowerFramework;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;

import mekanism.api.IEnergizedItem;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityHeatGenerator extends TileEntityGenerator implements ITankContainer
{
	/** The LiquidSlot fuel instance for this generator. */
	public LiquidSlot fuelSlot = new LiquidSlot(24000, Mekanism.hooks.BuildCraftFuelID);
	
	public TileEntityHeatGenerator()
	{
		super("Heat Generator", 16000, 64);
		inventory = new ItemStack[2];
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(inventory[1] != null && energyStored > 0)
		{
			if(inventory[1].getItem() instanceof IEnergizedItem)
			{
				IEnergizedItem item = (IEnergizedItem)inventory[1].getItem();
				int sendingEnergy = 0;
				
				if(item.getRate() <= energyStored)
				{
					sendingEnergy = item.getRate();
				}
				else if(item.getRate() > energyStored)
				{
					sendingEnergy = energyStored;
				}
				
				int rejects = item.charge(inventory[1], sendingEnergy);
				setEnergy(energyStored - (sendingEnergy - rejects));
			}
			else if(inventory[1].getItem() instanceof IItemElectric)
			{
				IItemElectric electricItem = (IItemElectric) inventory[1].getItem();
				double ampsToGive = Math.min(ElectricInfo.getAmps(electricItem.getMaxJoules() * 0.005, getVoltage()), (energyStored*UniversalElectricity.IC2_RATIO));
				double joules = electricItem.onReceive(ampsToGive, getVoltage(), inventory[1]);
				setJoules((energyStored*UniversalElectricity.IC2_RATIO) - (ElectricInfo.getJoules(ampsToGive, getVoltage(), 1) - joules));
			}
			else if(inventory[1].getItem() instanceof IElectricItem)
			{
				int sent = ElectricItem.charge(inventory[1], energyStored, 3, false, false);
				setEnergy(energyStored - sent);
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
			if(fuel > 0)
			{
				int fuelNeeded = fuelSlot.MAX_LIQUID - fuelSlot.liquidStored;
				if(fuel <= fuelNeeded)
				{
					fuelSlot.liquidStored += fuel;
					--inventory[0].stackSize;
				}
				
				if(inventory[0].stackSize == 0)
				{
					inventory[0] = null;
				}
			}
		}
		
		if(energyStored < MAX_ENERGY)
		{
			setEnergy(energyStored + getEnvironmentBoost());
			
			if(canOperate())
			{	
				if(!worldObj.isRemote)
				{
					setActive(true);
				}
				fuelSlot.setLiquid(fuelSlot.liquidStored - 10);
				setEnergy(energyStored + 16);
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
		return energyStored < MAX_ENERGY && fuelSlot.liquidStored > 0;
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
			boost+=8;
		if(worldObj.getBlockId(xCoord-1, yCoord, zCoord) == 10 || worldObj.getBlockId(xCoord-1, yCoord, zCoord) == 11)
			boost+=8;
		if(worldObj.getBlockId(xCoord, yCoord+1, zCoord) == 10 || worldObj.getBlockId(xCoord, yCoord+1, zCoord) == 11)
			boost+=8;
		if(worldObj.getBlockId(xCoord, yCoord-1, zCoord) == 10 || worldObj.getBlockId(xCoord, yCoord-1, zCoord) == 11)
			boost+=8;
		if(worldObj.getBlockId(xCoord, yCoord, zCoord+1) == 10 || worldObj.getBlockId(xCoord, yCoord, zCoord+1) == 11)
			boost+=8;
		if(worldObj.getBlockId(xCoord, yCoord, zCoord-1) == 10 || worldObj.getBlockId(xCoord, yCoord, zCoord-1) == 11)
			boost+=8;
		
		return boost;
	}

	public int getFuel(ItemStack itemstack)
	{
		return TileEntityFurnace.getItemBurnTime(itemstack);
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
			energyStored = dataStream.readInt();
			isActive = dataStream.readBoolean();
			fuelSlot.liquidStored = dataStream.readInt();
			worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
		} catch (Exception e)
		{
			System.out.println("[Mekanism] Error while handling tile entity packet.");
			e.printStackTrace();
		}
	}
	
	@Override
	public void sendPacket()
	{
		PacketHandler.sendTileEntityPacket(this, facing, energyStored, isActive, fuelSlot.liquidStored);
	}
	
	@Override
	public void sendPacketWithRange()
	{
		PacketHandler.sendTileEntityPacketWithRange(this, 50, facing, energyStored, isActive, fuelSlot.liquidStored);
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
				return new Object[] {energyStored};
			case 1:
				return new Object[] {output};
			case 2:
				return new Object[] {MAX_ENERGY};
			case 3:
				return new Object[] {(MAX_ENERGY-energyStored)};
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
	public int fill(Orientations from, LiquidStack resource, boolean doFill) 
	{
		if(from.toDirection() != ForgeDirection.getOrientation(facing))
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
	public LiquidStack drain(Orientations from, int maxDrain, boolean doDrain) 
	{
		return null;
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain) 
	{
		return null;
	}

	@Override
	public ILiquidTank[] getTanks() 
	{
		return new ILiquidTank[] {new LiquidTank(fuelSlot.liquidID, fuelSlot.liquidStored, fuelSlot.MAX_LIQUID)};
	}
}
