package mekanism.common.tile;

import java.util.ArrayList;
import java.util.Map;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.PressurizedProducts;
import mekanism.api.PressurizedRecipe;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.SideData;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.item.ItemMachineUpgrade;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.google.common.io.ByteArrayDataInput;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;

public class TileEntityPRC extends TileEntityBasicMachine implements IFluidHandler, IGasHandler, ITubeConnection
{
	public FluidTank inputFluidTank = new FluidTank(10000);
	public GasTank inputGasTank = new GasTank(10000);

	public GasTank outputGasTank = new GasTank(10000);

	public TileEntityPRC()
	{
		super("PressurizedReactionChamber.ogg", "PressurizedReactionChamber", new ResourceLocation("mekanism", "gui/GuiPRC.png"), Mekanism.pressurizedReactionBaseUsage, 100, MachineType.PRESSURIZED_REACTION_CHAMBER.baseEnergy);

		sideOutputs.add(new SideData(EnumColor.GREY, InventoryUtils.EMPTY));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, new int[] {0}));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, new int[] {1}));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, new int[] {2}));
		sideOutputs.add(new SideData(EnumColor.ORANGE, new int[] {3}));

		sideConfig = new byte[] {2, 1, 0, 0, 0, 3};

		inventory = new ItemStack[4];

		upgradeComponent = new TileComponentUpgrade(this, 3);
		ejectorComponent = new TileComponentEjector(this, sideOutputs.get(3));
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(!worldObj.isRemote)
		{
			ChargeUtils.discharge(1, this);

			if(canOperate() && MekanismUtils.canFunction(this) && getEnergy() >= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK))
			{
				PressurizedRecipe recipe = getRecipe();
				TICKS_REQUIRED = recipe.ticks;
				setActive(true);

				if((operatingTicks+1) < MekanismUtils.getTicks(getSpeedMultiplier(), TICKS_REQUIRED))
				{
					operatingTicks++;
					electricityStored -= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK);
				}
				else if((operatingTicks+1) >= MekanismUtils.getTicks(getSpeedMultiplier(), TICKS_REQUIRED) && electricityStored >= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK + recipe.extraEnergy))
				{
					operate();

					operatingTicks = 0;
					electricityStored -= MekanismUtils.getEnergyPerTick(getSpeedMultiplier(), getEnergyMultiplier(), ENERGY_PER_TICK + recipe.extraEnergy);
				}
			}
			else {
				TICKS_REQUIRED = 100;
				if(prevEnergy >= getEnergy())
				{
					setActive(false);
				}
			}

			if(!canOperate())
			{
				operatingTicks = 0;
			}

			prevEnergy = getEnergy();

			if(outputGasTank.getGas() != null)
			{
				GasStack toSend = new GasStack(outputGasTank.getGas().getGas(), Math.min(outputGasTank.getStored(), 16));
				outputGasTank.draw(GasTransmission.emitGasToNetwork(toSend, this, MekanismUtils.getLeft(facing)), true);

				TileEntity tileEntity = Coord4D.get(this).getFromSide(MekanismUtils.getLeft(facing)).getTileEntity(worldObj);

				if(tileEntity instanceof IGasHandler)
				{
					if(((IGasHandler)tileEntity).canReceiveGas(MekanismUtils.getLeft(facing).getOpposite(), outputGasTank.getGas().getGas()))
					{
						outputGasTank.draw(((IGasHandler)tileEntity).receiveGas(MekanismUtils.getLeft(facing).getOpposite(), toSend), true);
					}
				}
			}
		}
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 1)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}
		else if(slotID == 3)
		{
			return itemstack.getItem() instanceof ItemMachineUpgrade;
		}

		return false;
	}

	@Override
	public void operate()
	{
		PressurizedRecipe recipe = getRecipe();

		recipe.reactants.use(inventory[0], inputFluidTank, inputGasTank);

		if(inventory[0].stackSize <= 0)
		{
			inventory[0] = null;
		}

		recipe.products.fillTank(outputGasTank);

		recipe.products.addProducts(inventory, 2);

		onInventoryChanged();
		ejectorComponent.onOutput();
	}

	@Override
	public boolean canOperate()
	{
		PressurizedRecipe recipe = getRecipe();

		if(recipe == null)
		{
			return false;
		}

		PressurizedProducts products = recipe.products;

		if(products.getOptionalOutput() != null)
		{
			if(inventory[2] != null)
			{
				if(!inventory[2].isItemEqual(products.getOptionalOutput()))
				{
					return false;
				}
				else {
					if(inventory[2].stackSize + products.getOptionalOutput().stackSize > inventory[2].getMaxStackSize())
					{
						return false;
					}
				}
			}
		}

		if(products.getGasOutput() != null)
		{
			products.getGasOutput().isGasEqual(outputGasTank.getGas());
		}

		return true;
	}

	public PressurizedRecipe getRecipe()
	{
		if(inventory[0] == null)
		{
			return null;
		}

		return RecipeHandler.getPRCOutput(inventory[0], inputFluidTank, inputGasTank);
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 1)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
		}
		else if(slotID == 2 || slotID == 4)
		{
			return true;
		}

		return false;
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		if(inputFluidTank.getFluid() != null)
		{
			data.add(true);
			data.add(inputFluidTank.getFluid().getFluid().getID());
			data.add(inputFluidTank.getFluidAmount());
		}
		else {
			data.add(false);
		}

		if(inputGasTank.getGas() != null)
		{
			data.add(true);
			data.add(inputGasTank.getGas().getGas().getID());
			data.add(inputGasTank.getStored());
		}
		else {
			data.add(false);
		}

		if(outputGasTank.getGas() != null)
		{
			data.add(true);
			data.add(outputGasTank.getGas().getGas().getID());
			data.add(outputGasTank.getStored());
		}
		else {
			data.add(false);
		}

		return data;
	}

	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);

		if(dataStream.readBoolean())
		{
			inputFluidTank.setFluid(new FluidStack(FluidRegistry.getFluid(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			inputFluidTank.setFluid(null);
		}

		if(dataStream.readBoolean())
		{
			inputGasTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			inputGasTank.setGas(null);
		}

		if(dataStream.readBoolean())
		{
			outputGasTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			outputGasTank.setGas(null);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);
/*
		if(nbtTags.hasKey("fluidTank"))
		{
			fluidTank.readFromNBT(nbtTags.getCompoundTag("fluidTank"));
		}

		leftTank.read(nbtTags.getCompoundTag("leftTank"));
		rightTank.read(nbtTags.getCompoundTag("rightTank"));

		dumpLeft = nbtTags.getBoolean("dumpLeft");
		dumpRight = nbtTags.getBoolean("dumpRight");
*/	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);
/*
		if(fluidTank.getFluid() != null)
		{
			nbtTags.setTag("fluidTank", fluidTank.writeToNBT(new NBTTagCompound()));
		}

		nbtTags.setCompoundTag("leftTank", leftTank.write(new NBTTagCompound()));
		nbtTags.setCompoundTag("rightTank", rightTank.write(new NBTTagCompound()));

		nbtTags.setBoolean("dumpLeft", dumpLeft);
		nbtTags.setBoolean("dumpRight", dumpRight);
*/	}

	@Override
	public Map getRecipes()
	{
		return null;
	}

	@Override
	public String[] getMethodNames()
	{
		return null;
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception
	{
		return null;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		return inputFluidTank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if(inputFluidTank.getFluid() != null && inputFluidTank.getFluid().isFluidEqual(resource))
		{
			return inputFluidTank.drain(resource.amount, doDrain);
		}
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return inputFluidTank.drain(maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return inputFluidTank.getFluid() == null || inputFluidTank.getFluid().getFluid() == fluid;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return inputFluidTank.getFluid() != null && inputFluidTank.getFluid().getFluid() == fluid;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[] {new FluidTankInfo(inputFluidTank)};
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack)
	{
		return inputGasTank.receive(stack, true);
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount)
	{
		return outputGasTank.draw(amount, true);
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return inputGasTank.getGas() == null || inputGasTank.getGas().getGas() == type;
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return outputGasTank.getGas() != null && outputGasTank.getGas().getGas() == type;
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return true;
	}
}
