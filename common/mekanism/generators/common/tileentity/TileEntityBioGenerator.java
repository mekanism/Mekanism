package mekanism.generators.common.tileentity;

import java.util.ArrayList;

import mekanism.client.sound.Sound;
import mekanism.common.FluidSlot;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computer.api.IComputerAccess;

public class TileEntityBioGenerator extends TileEntityGenerator implements IFluidHandler
{
	/** The Sound instance for this machine. */
	@SideOnly(Side.CLIENT)
	public Sound audio;

	/** Where the crush piston should be on the model. */
	public float crushMatrix = 0;

	/** The FluidSlot biofuel instance for this generator. */
	public FluidSlot bioFuelSlot = new FluidSlot(24000, -1);

	public TileEntityBioGenerator()
	{
		super("Bio-Generator", 160000, MekanismGenerators.bioGeneration*2);
		inventory = new ItemStack[2];
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
			FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(inventory[0]);

			if(fluid != null && FluidRegistry.isFluidRegistered("bioethanol"))
			{
				if(fluid.getFluid() == FluidRegistry.getFluid("bioethanol"))
				{
					int fluidToAdd = fluid.amount;

					if(bioFuelSlot.fluidStored+fluidToAdd <= bioFuelSlot.MAX_FLUID)
					{
						bioFuelSlot.setFluid(bioFuelSlot.fluidStored+fluidToAdd);
						
						if(FluidContainerRegistry.isBucket(inventory[0]))
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
					int fuelNeeded = bioFuelSlot.MAX_FLUID - bioFuelSlot.fluidStored;
					if(fuel <= fuelNeeded)
					{
						bioFuelSlot.fluidStored += fuel;
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
			
			bioFuelSlot.setFluid(bioFuelSlot.fluidStored - 1);
			setEnergy(electricityStored + MekanismGenerators.bioGeneration);
		}
		else {
			if(!worldObj.isRemote)
			{
				setActive(false);
			}
		}
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			if(getFuel(itemstack ) > 0)
			{
				return true;
			}
			else {
				if(FluidRegistry.isFluidRegistered("bioethanol"))
				{
					if(FluidContainerRegistry.getFluidForFilledItem(itemstack) != null)
					{
						if(FluidContainerRegistry.getFluidForFilledItem(itemstack).getFluid() == FluidRegistry.getFluid("bioethanol"))
						{
							return true;
						}
					}
				}
				
				return false;
			}
		}
		else if(slotID == 1)
		{
			return ChargeUtils.canBeCharged(itemstack);
		}

		return true;
	}

	@Override
	public boolean canOperate()
	{
		return electricityStored < MAX_ELECTRICITY && bioFuelSlot.fluidStored > 0 && MekanismUtils.canFunction(this);
	}

	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        
        bioFuelSlot.fluidStored = nbtTags.getInteger("bioFuelStored");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("bioFuelStored", bioFuelSlot.fluidStored);
    }

	@Override
	public double getEnvironmentBoost()
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
		return bioFuelSlot.fluidStored*i / bioFuelSlot.MAX_FLUID;
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return ForgeDirection.getOrientation(side) == MekanismUtils.getRight(facing) ? new int[] {1} : new int[] {0};
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
		bioFuelSlot.fluidStored = dataStream.readInt();
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(bioFuelSlot.fluidStored);
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
				return new Object[] {bioFuelSlot.fluidStored};
			case 5:
				return new Object[] {bioFuelSlot.MAX_FLUID-bioFuelSlot.fluidStored};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return null;
		}
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) 
	{
		if(FluidRegistry.isFluidRegistered("bioethanol") && from != ForgeDirection.getOrientation(facing))
		{
			if(resource.getFluid() == FluidRegistry.getFluid("bioethanol"))
			{
				int fuelTransfer = 0;
				int fuelNeeded = bioFuelSlot.MAX_FLUID - bioFuelSlot.fluidStored;
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
					bioFuelSlot.setFluid(bioFuelSlot.fluidStored + fuelTransfer);
				}

				return fuelTransfer;
			}
		}

		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) 
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) 
	{
		return FluidRegistry.isFluidRegistered("bioethanol") && fluid == FluidRegistry.getFluid("bioethanol");
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) 
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) 
	{
		return null;
	}
}