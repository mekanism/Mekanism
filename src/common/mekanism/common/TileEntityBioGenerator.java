package mekanism.common;

import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import universalelectricity.core.UniversalElectricity;
import universalelectricity.core.electricity.ElectricInfo;
import universalelectricity.core.implement.IItemElectric;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import mekanism.api.IEnergizedItem;
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
		super("Bio-Generator", 16000, 64);
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
			if(fuel > 0)
			{
				int fuelNeeded = bioFuelSlot.MAX_LIQUID - bioFuelSlot.liquidStored;
				if(fuel <= fuelNeeded)
				{
					bioFuelSlot.liquidStored += fuel;
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
			if(canOperate())
			{	
				if(!worldObj.isRemote)
				{
					setActive(true);
				}
				bioFuelSlot.setLiquid(bioFuelSlot.liquidStored - 10);
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
		return energyStored < MAX_ENERGY && bioFuelSlot.liquidStored > 0;
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
		return itemstack.itemID == Mekanism.BioFuel.shiftedIndex ? 100 : 0;
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
	public void handlePacketData(INetworkManager network, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try {
			facing = dataStream.readInt();
			energyStored = dataStream.readInt();
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
		PacketHandler.sendTileEntityPacketToClients(this, 0, facing, energyStored, isActive, bioFuelSlot.liquidStored);
	}
	
	@Override
	public void sendPacketWithRange()
	{
		PacketHandler.sendTileEntityPacketToClients(this, 50, facing, energyStored, isActive, bioFuelSlot.liquidStored);
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
				return new Object[] {energyStored};
			case 1:
				return new Object[] {output};
			case 2:
				return new Object[] {MAX_ENERGY};
			case 3:
				return new Object[] {(MAX_ENERGY-energyStored)};
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
