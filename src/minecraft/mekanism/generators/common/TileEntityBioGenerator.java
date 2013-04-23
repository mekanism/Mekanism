package mekanism.generators.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import universalelectricity.core.item.ElectricItemHelper;
import universalelectricity.core.item.IItemElectric;

import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import mekanism.client.Sound;
import mekanism.common.ChargeUtils;
import mekanism.common.LiquidSlot;
import mekanism.common.Mekanism;
import mekanism.common.MekanismUtils;
import mekanism.common.RecipeHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import dan200.computer.api.IComputerAccess;

public class TileEntityBioGenerator extends TileEntityGenerator implements ITankContainer
{
	/** The Sound instance for this machine. */
	@SideOnly(Side.CLIENT)
	public Sound audio;

	/** Where the crush piston should be on the model. */
	public float crushMatrix = 0;

	/** The amount of electricity this machine can produce with a unit of fuel. */
	public final int GENERATION = 50;

	/** The LiquidSlot biofuel instance for this generator. */
	public LiquidSlot bioFuelSlot = new LiquidSlot(24000, Mekanism.hooks.ForestryBiofuelID);

	/** Which fuels work on this generator. */
	public static Map<Integer, Integer> fuels = new HashMap<Integer, Integer>();

	public TileEntityBioGenerator()
	{
		super("Bio-Generator", 160000, 100);
		inventory = new ItemStack[2];

		if(Mekanism.hooks.ForestryLoaded)
		{
			fuels.put(Mekanism.hooks.ForestryBiofuelID, 16);
		}
	}

	public float getMatrix()
	{
		float matrix = 0;

		if(crushMatrix <= 2)
		{
			return crushMatrix;
		}
		else {
			return 2 - (crushMatrix-2);
		}
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(worldObj.isRemote)
		{
			if(crushMatrix < 4)
			{
				crushMatrix+=0.2F;
			}
			else {
				crushMatrix = 0;
			}
		}

		ChargeUtils.charge(1, this);

		if(inventory[0] != null)
		{
			LiquidStack liquid = LiquidContainerRegistry.getLiquidForFilledItem(inventory[0]);

			if(liquid != null)
			{
				if(fuels.containsKey(liquid.itemID))
				{
					int liquidToAdd = liquid.amount*fuels.get(liquid.itemID);

					if(bioFuelSlot.liquidStored+liquidToAdd <= bioFuelSlot.MAX_LIQUID)
					{
						bioFuelSlot.setLiquid(bioFuelSlot.liquidStored+liquidToAdd);
						if(LiquidContainerRegistry.isBucket(inventory[0]))
						{
							inventory[0] = new ItemStack(Item.bucketEmpty);
						}
						else {
							inventory[0].stackSize--;

							if(inventory[0].stackSize == 0)
							{
								inventory[0] = null;
							}
						}
					}
				}
			}
			else {
				int fuel = getFuel(inventory[0]);
				ItemStack prevStack = inventory[0].copy();
				if(fuel > 0)
				{
					int fuelNeeded = bioFuelSlot.MAX_LIQUID - bioFuelSlot.liquidStored;
					if(fuel <= fuelNeeded)
					{
						bioFuelSlot.liquidStored += fuel;
						inventory[0].stackSize--;

						if(prevStack.isItemEqual(new ItemStack(Item.bucketLava)))
						{
							inventory[0] = new ItemStack(Item.bucketEmpty);
						}
					}

					if(inventory[0].stackSize == 0)
					{
						inventory[0] = null;
					}
				}
			}
		}

		if(canOperate())
		{	
			if(!worldObj.isRemote)
			{
				setActive(true);
			}
			bioFuelSlot.setLiquid(bioFuelSlot.liquidStored - 1);
			setEnergy(electricityStored + GENERATION);
		}
		else {
			if(!worldObj.isRemote)
			{
				setActive(false);
			}
		}
	}

	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return getFuel(itemstack) > 0 || (LiquidContainerRegistry.getLiquidForFilledItem(itemstack) != null && fuels.containsKey(LiquidContainerRegistry.getLiquidForFilledItem(itemstack).itemID));
		}
		else if(slotID == 1)
		{
			return itemstack.getItem() instanceof IElectricItem || 
					(itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getReceiveRequest(itemstack).amperes != 0);
		}

		return true;
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
		return itemstack.itemID == MekanismGenerators.BioFuel.itemID ? 100 : 0;
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

	@Override
	public boolean canSetFacing(int facing)
	{
		return facing != 0 && facing != 1;
	}

	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		bioFuelSlot.liquidStored = dataStream.readInt();
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(bioFuelSlot.liquidStored);
		return data;
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