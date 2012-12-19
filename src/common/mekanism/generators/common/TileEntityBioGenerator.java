package mekanism.generators.common;

import ic2.api.ElectricItem;
import ic2.api.IElectricItem;

import universalelectricity.core.electricity.ElectricInfo;
import universalelectricity.core.implement.IItemElectric;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;

import mekanism.common.LiquidSlot;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;

import net.minecraft.src.*;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;

public class TileEntityBioGenerator extends TileEntityGenerator implements ITankContainer
{
	/** The LiquidSlot biofuel instance for this generator. */
	public LiquidSlot bioFuelSlot = new LiquidSlot(24000, Mekanism.hooks.ForestryBiofuelID);

	public TileEntityBioGenerator()
	{
		super("Bio-Generator", 160000, 512);
		inventory = new ItemStack[2];
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
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
		
		if(inventory[0] != null && bioFuelSlot.liquidStored < bioFuelSlot.MAX_LIQUID)
		{
			if(Mekanism.hooks.ForestryLoaded)
			{
				if(inventory[0].itemID == Mekanism.hooks.ForestryBiofuelBucket.itemID)
				{
					bioFuelSlot.setLiquid(bioFuelSlot.liquidStored + 1000);
					inventory[0] = new ItemStack(Item.bucketEmpty);
				}
			}
			
			int fuel = getFuel(inventory[0]);
			ItemStack prevStack = inventory[0].copy();
			if(fuel > 0)
			{
				int fuelNeeded = bioFuelSlot.MAX_LIQUID - bioFuelSlot.liquidStored;
				if(fuel <= fuelNeeded)
				{
					bioFuelSlot.liquidStored += fuel;
					--inventory[0].stackSize;
				}
				
				if(prevStack.itemID == Mekanism.hooks.ForestryBiofuelBucket.itemID)
				{
					inventory[0] = Mekanism.hooks.ForestryBiofuelBucket;
				}
				
				if(inventory[0].stackSize == 0)
				{
					inventory[0] = null;
				}
			}
		}
		
		if(electricityStored < MAX_ELECTRICITY)
		{
			if(canOperate())
			{	
				if(!worldObj.isRemote)
				{
					setActive(true);
				}
				bioFuelSlot.setLiquid(bioFuelSlot.liquidStored - 10);
				setJoules(electricityStored + 800);
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
		return electricityStored < MAX_ELECTRICITY && bioFuelSlot.liquidStored > 0;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        
        bioFuelSlot.liquidStored = nbtTags.getInteger("bioFuelStored");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("bioFuelStored", bioFuelSlot.liquidStored);
    }
	
	@Override
	public int getEnvironmentBoost()
	{
		return 0;
	}

	public int getFuel(ItemStack itemstack)
	{
		return itemstack.itemID == MekanismGenerators.BioFuel.shiftedIndex ? 100 : 0;
	}
	
	/**
	 * Gets the scaled fuel level for the GUI.
	 * @param i - multiplier
	 * @return
	 */
	public int getScaledFuelLevel(int i)
	{
		return bioFuelSlot.liquidStored*i / bioFuelSlot.MAX_LIQUID;
	}
	
	@Override
	public boolean canSetFacing(int facing)
	{
		return facing != 0 && facing != 1;
	}
	
	@Override
	public void handlePacketData(INetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try {
			facing = dataStream.readInt();
			electricityStored = dataStream.readDouble();
			isActive = dataStream.readBoolean();
			bioFuelSlot.liquidStored = dataStream.readInt();
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
		PacketHandler.sendTileEntityPacketToClients(this, 0, facing, electricityStored, isActive, bioFuelSlot.liquidStored);
	}
	
	@Override
	public void sendPacketWithRange()
	{
		PacketHandler.sendTileEntityPacketToClients(this, 50, facing, electricityStored, isActive, bioFuelSlot.liquidStored);
	}
	
	@Override
	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getBioFuel", "getBioFuelNeeded"};
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
				return new Object[] {bioFuelSlot.liquidStored};
			case 5:
				return new Object[] {bioFuelSlot.MAX_LIQUID-bioFuelSlot.liquidStored};
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
			if(resource.itemID == Mekanism.hooks.ForestryBiofuelID)
			{
				int fuelTransfer = 0;
				int fuelNeeded = bioFuelSlot.MAX_LIQUID - bioFuelSlot.liquidStored;
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
					bioFuelSlot.setLiquid(bioFuelSlot.liquidStored + fuelTransfer);
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
		return new ILiquidTank[] {new LiquidTank(bioFuelSlot.liquidID, bioFuelSlot.liquidStored, bioFuelSlot.MAX_LIQUID)};
	}
	
	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type)
	{
		return null;
	}
}
