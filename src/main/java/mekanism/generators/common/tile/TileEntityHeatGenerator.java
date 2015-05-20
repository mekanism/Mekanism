package mekanism.generators.common.tile;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.MekanismConfig.generators;
import mekanism.common.Mekanism;
import mekanism.common.base.ISustainedData;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import cpw.mods.fml.common.Optional.Method;

import io.netty.buffer.ByteBuf;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;

public class TileEntityHeatGenerator extends TileEntityGenerator implements IFluidHandler, ISustainedData, IHeatTransfer
{
	/** The FluidTank for this generator. */
	public FluidTank lavaTank = new FluidTank(24000);

	public double temperature = 0;

	public double thermalEfficiency = 0.5D;

	public double invHeatCapacity = 1;

	public double heatToAbsorb = 0;
	
	public double producingEnergy;

	public TileEntityHeatGenerator()
	{
		super("heat", "HeatGenerator", 160000, generators.heatGeneration*2);
		inventory = new ItemStack[2];
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(!worldObj.isRemote)
		{
			ChargeUtils.charge(1, this);

			if(inventory[0] != null)
			{
				FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(inventory[0]);
				
				if(inventory[0].getItem() instanceof IFluidContainerItem)
				{
					lavaTank.fill(FluidContainerUtils.extractFluid(lavaTank, inventory[0], FluidRegistry.LAVA), true);
				}
				else if(fluid != null)
				{
					if(fluid != null && fluid.fluidID == FluidRegistry.LAVA.getID())
					{
						if(lavaTank.getFluid() == null || lavaTank.getFluid().amount+fluid.amount <= lavaTank.getCapacity())
						{
							lavaTank.fill(fluid, true);
	
							if(inventory[0].getItem().getContainerItem(inventory[0]) != null)
							{
								inventory[0] = inventory[0].getItem().getContainerItem(inventory[0]);
							}
							else {
								inventory[0].stackSize--;
							}
	
							if(inventory[0].stackSize == 0)
							{
								inventory[0] = null;
							}
						}
					}
				}
				else {
					int fuel = getFuel(inventory[0]);

					if(fuel > 0)
					{
						int fuelNeeded = lavaTank.getCapacity() - (lavaTank.getFluid() != null ? lavaTank.getFluid().amount : 0);

						if(fuel <= fuelNeeded)
						{
							lavaTank.fill(new FluidStack(FluidRegistry.LAVA, fuel), true);

							if(inventory[0].getItem().getContainerItem(inventory[0]) != null)
							{
								inventory[0] = inventory[0].getItem().getContainerItem(inventory[0]);
							}
							else {
								inventory[0].stackSize--;
							}

							if(inventory[0].stackSize == 0)
							{
								inventory[0] = null;
							}
						}
					}
				}
			}
			
			double prev = getEnergy();

			transferHeatTo(getBoost());

			if(canOperate())
			{
				setActive(true);

				lavaTank.drain(10, true);
				transferHeatTo(generators.heatGeneration);
			}
			else {
				setActive(false);
			}
			
			simulateHeat();
			applyTemperatureChange();
			
			producingEnergy = getEnergy()-prev;
		}
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return getFuel(itemstack) > 0 || (FluidContainerRegistry.getFluidForFilledItem(itemstack) != null && FluidContainerRegistry.getFluidForFilledItem(itemstack).fluidID == FluidRegistry.LAVA.getID());
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
		return electricityStored < BASE_MAX_ENERGY && lavaTank.getFluid() != null && lavaTank.getFluid().amount >= 10 && MekanismUtils.canFunction(this);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		if(nbtTags.hasKey("lavaTank"))
		{
			lavaTank.readFromNBT(nbtTags.getCompoundTag("lavaTank"));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		if(lavaTank.getFluid() != null)
		{
			nbtTags.setTag("lavaTank", lavaTank.writeToNBT(new NBTTagCompound()));
		}
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 1)
		{
			return ChargeUtils.canBeOutputted(itemstack, true);
		}
		else if(slotID == 0)
		{
			return FluidContainerRegistry.isEmptyContainer(itemstack);
		}

		return false;
	}

	public double getBoost()
	{
		int lavaBoost = 0;
		double netherBoost = 0D;

		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			Coord4D coord = Coord4D.get(this).getFromSide(side);
			
			if(isLava(coord.xCoord, coord.yCoord, coord.zCoord))
			{
				lavaBoost++;
			}
		}

		if(worldObj.provider.dimensionId == -1)
		{
			netherBoost = generators.heatGenerationNether;
		}

		return (generators.heatGenerationLava * lavaBoost) + netherBoost;
	}
	
	private boolean isLava(int x, int y, int z)
	{
		return worldObj.getBlock(x, y, z) == Blocks.lava;
	}

	public int getFuel(ItemStack itemstack)
	{
		if(itemstack.getItem() == Items.lava_bucket)
		{
			return 1000;
		}

		return TileEntityFurnace.getItemBurnTime(itemstack)/2;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return ForgeDirection.getOrientation(side) == MekanismUtils.getRight(facing) ? new int[] {1} : new int[] {0};
	}

	/**
	 * Gets the scaled fuel level for the GUI.
	 * @param i - multiplier
	 * @return
	 */
	public int getScaledFuelLevel(int i)
	{
		return (lavaTank.getFluid() != null ? lavaTank.getFluid().amount : 0)*i / lavaTank.getCapacity();
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);
		
		producingEnergy = dataStream.readDouble();

		int amount = dataStream.readInt();

		if(amount != 0)
		{
			lavaTank.setFluid(new FluidStack(FluidRegistry.LAVA, amount));
		}
		else {
			lavaTank.setFluid(null);
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(producingEnergy);

		if(lavaTank.getFluid() != null)
		{
			data.add(lavaTank.getFluid().amount);
		}
		else {
			data.add(0);
		}

		return data;
	}

	@Override
	@Method(modid = "ComputerCraft")
	public String[] getMethodNames()
	{
		return new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getFuel", "getFuelNeeded"};
	}

	@Override
	@Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				return new Object[] {output};
			case 2:
				return new Object[] {BASE_MAX_ENERGY};
			case 3:
				return new Object[] {(BASE_MAX_ENERGY -electricityStored)};
			case 4:
				return new Object[] {lavaTank.getFluid() != null ? lavaTank.getFluid().amount : 0};
			case 5:
				return new Object[] {lavaTank.getCapacity()-(lavaTank.getFluid() != null ? lavaTank.getFluid().amount : 0)};
			default:
				Mekanism.logger.error("Attempted to call unknown method with computer ID " + computer.getID());
				return null;
		}
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(resource.fluidID == FluidRegistry.LAVA.getID() && from != ForgeDirection.getOrientation(facing))
		{
			return lavaTank.fill(resource, doFill);
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
		return fluid == FluidRegistry.LAVA && from != ForgeDirection.getOrientation(facing);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if(from == ForgeDirection.getOrientation(facing))
		{
			return PipeUtils.EMPTY;
		}
		
		return new FluidTankInfo[] {lavaTank.getInfo()};
	}

	@Override
	public void writeSustainedData(ItemStack itemStack) 
	{
		if(lavaTank.getFluid() != null)
		{
			itemStack.stackTagCompound.setTag("lavaTank", lavaTank.getFluid().writeToNBT(new NBTTagCompound()));
		}
	}

	@Override
	public void readSustainedData(ItemStack itemStack) 
	{
		lavaTank.setFluid(FluidStack.loadFluidStackFromNBT(itemStack.stackTagCompound.getCompoundTag("lavaTank")));
	}

	@Override
	public double getTemp()
	{
		return temperature;
	}

	@Override
	public double getInverseConductionCoefficient()
	{
		return 1;
	}

	@Override
	public double getInsulationCoefficient(ForgeDirection side)
	{
		return canConnectHeat(side) ? 0 : 10000;
	}

	@Override
	public void transferHeatTo(double heat)
	{
		heatToAbsorb += heat;
	}

	@Override
	public double[] simulateHeat()
	{
		if(getTemp() > 0)
		{
			double carnotEfficiency = getTemp() / (getTemp() + IHeatTransfer.AMBIENT_TEMP);
			double heatLost = thermalEfficiency * getTemp();
			double workDone = heatLost * carnotEfficiency;
			transferHeatTo(-heatLost);
			setEnergy(getEnergy() + workDone);
		}
		
		return HeatUtils.simulate(this);
	}

	@Override
	public double applyTemperatureChange()
	{
		temperature += invHeatCapacity * heatToAbsorb;
		heatToAbsorb = 0;
		return temperature;
	}

	@Override
	public boolean canConnectHeat(ForgeDirection side)
	{
		return side == ForgeDirection.DOWN;
	}

	@Override
	public IHeatTransfer getAdjacent(ForgeDirection side)
	{
		if(canConnectHeat(side))
		{
			TileEntity adj = Coord4D.get(this).getFromSide(side).getTileEntity(worldObj);
			
			if(adj instanceof IHeatTransfer)
			{
				return (IHeatTransfer)adj;
			}
		}
		
		return null;
	}
}
