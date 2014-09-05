package mekanism.common.tile;

import java.util.ArrayList;
import java.util.Map;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.MekanismConfig.usage;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.recipe.inputs.PressurizedInput;
import mekanism.common.recipe.outputs.PressurizedProducts;
import mekanism.common.recipe.machines.PressurizedRecipe;
import mekanism.common.SideData;
import mekanism.common.base.ISustainedData;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.item.ItemUpgrade;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import cpw.mods.fml.common.Optional.Method;

import io.netty.buffer.ByteBuf;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;

public class TileEntityPRC extends TileEntityBasicMachine<PressurizedInput, PressurizedProducts, PressurizedRecipe> implements IFluidHandler, IGasHandler, ITubeConnection, ISustainedData
{
	public FluidTank inputFluidTank = new FluidTank(10000);
	public GasTank inputGasTank = new GasTank(10000);
	
	public GasTank outputGasTank = new GasTank(10000);

	public TileEntityPRC()
	{
		super("prc", "PressurizedReactionChamber", new ResourceLocation("mekanism", "gui/GuiPRC.png"), usage.pressurizedReactionBaseUsage, 100, MachineType.PRESSURIZED_REACTION_CHAMBER.baseEnergy);

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
			PressurizedRecipe recipe = getRecipe();

			ChargeUtils.discharge(1, this);

			if(canOperate(recipe) && MekanismUtils.canFunction(this) && getEnergy() >= MekanismUtils.getEnergyPerTick(this, ENERGY_PER_TICK))
			{
				TICKS_REQUIRED = recipe.ticks;
				setActive(true);

				if((operatingTicks+1) < MekanismUtils.getTicks(this, TICKS_REQUIRED))
				{
					operatingTicks++;
					electricityStored -= MekanismUtils.getEnergyPerTick(this, ENERGY_PER_TICK);
				}
				else if((operatingTicks+1) >= MekanismUtils.getTicks(this, TICKS_REQUIRED) && electricityStored >= MekanismUtils.getEnergyPerTick(this, ENERGY_PER_TICK + recipe.extraEnergy))
				{
					operate(recipe);

					operatingTicks = 0;
					electricityStored -= MekanismUtils.getEnergyPerTick(this, ENERGY_PER_TICK + recipe.extraEnergy);
				}
			}
			else {
				TICKS_REQUIRED = 100;
				
				if(prevEnergy >= getEnergy())
				{
					setActive(false);
				}
			}

			if(!canOperate(recipe))
			{
				operatingTicks = 0;
			}

			prevEnergy = getEnergy();

			if(outputGasTank.getGas() != null)
			{
				GasStack toSend = new GasStack(outputGasTank.getGas().getGas(), Math.min(outputGasTank.getStored(), 16));

				TileEntity tileEntity = Coord4D.get(this).getFromSide(MekanismUtils.getRight(facing)).getTileEntity(worldObj);

				if(tileEntity instanceof IGasHandler)
				{
					if(((IGasHandler)tileEntity).canReceiveGas(MekanismUtils.getLeft(facing), outputGasTank.getGas().getGas()))
					{
						outputGasTank.draw(((IGasHandler)tileEntity).receiveGas(MekanismUtils.getLeft(facing), toSend, true), true);
					}
				}
			}
		}
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return RecipeHandler.isInPressurizedRecipe(itemstack);
		}
		else if(slotID == 1)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}
		else if(slotID == 3)
		{
			return itemstack.getItem() instanceof ItemUpgrade;
		}

		return false;
	}

	@Override
	public PressurizedRecipe getRecipe()
	{
		return RecipeHandler.getPRCRecipe(getInput());
	}

	@Override
	public PressurizedInput getInput()
	{
		return new PressurizedInput(inventory[0], inputFluidTank.getFluid(), inputGasTank.getGas());
	}

	@Override
	public void operate(PressurizedRecipe recipe)
	{
		recipe.operate(inventory, inputFluidTank, inputGasTank, outputGasTank);

		markDirty();
		ejectorComponent.onOutput();
	}

	@Override
	public boolean canOperate(PressurizedRecipe recipe)
	{
		return recipe != null && recipe.canOperate(inventory, inputFluidTank, inputGasTank, outputGasTank);
	}
	
	@Override
	public double getMaxEnergy()
	{
		return MekanismUtils.getMaxEnergy(this, MAX_ELECTRICITY);
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
	public void handlePacketData(ByteBuf dataStream)
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

		inputFluidTank.readFromNBT(nbtTags.getCompoundTag("inputFluidTank"));
		inputGasTank.read(nbtTags.getCompoundTag("inputGasTank"));
		outputGasTank.read(nbtTags.getCompoundTag("outputGasTank"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setTag("inputFluidTank", inputFluidTank.writeToNBT(new NBTTagCompound()));
		nbtTags.setTag("inputGasTank", inputGasTank.write(new NBTTagCompound()));
		nbtTags.setTag("outputGasTank", outputGasTank.write(new NBTTagCompound()));
	}

	@Override
	public String getInventoryName()
	{
		return MekanismUtils.localize(getBlockType().getUnlocalizedName() + "." + fullName + ".short.name");
	}

	@Override
	public Map getRecipes()
	{
		return null;
	}

	@Override
	@Method(modid = "ComputerCraft")
	public String[] getMethodNames()
	{
		return null;
	}

	@Override
	@Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception
	{
		return null;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(from == ForgeDirection.getOrientation(facing).getOpposite())
		{
			return inputFluidTank.fill(resource, doFill);
		}
		
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		if(from == ForgeDirection.getOrientation(facing).getOpposite())
		{
			return inputFluidTank.getFluid() == null || inputFluidTank.getFluid().getFluid() == fluid;
		}
		
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if(from == ForgeDirection.getOrientation(facing).getOpposite())
		{
			return new FluidTankInfo[] {new FluidTankInfo(inputFluidTank)};
		}
		
		return PipeUtils.EMPTY;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack, boolean doTransfer)
	{
		if(side == MekanismUtils.getLeft(facing))
		{
			return inputGasTank.receive(stack, doTransfer);
		}
		
		return 0;
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount, boolean doTransfer)
	{
		if(side == MekanismUtils.getRight(facing))
		{
			return outputGasTank.draw(amount, doTransfer);
		}
		
		return null;
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return side == MekanismUtils.getLeft(facing) && (inputGasTank.getGas() == null || inputGasTank.getGas().getGas() == type);
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return side == MekanismUtils.getRight(facing) && outputGasTank.getGas() != null && outputGasTank.getGas().getGas() == type;
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return side == MekanismUtils.getLeft(facing) || side == MekanismUtils.getRight(facing);
	}

	@Override
	public void writeSustainedData(ItemStack itemStack) 
	{
		if(inputFluidTank.getFluid() != null)
		{
			itemStack.stackTagCompound.setTag("inputFluidTank", inputFluidTank.getFluid().writeToNBT(new NBTTagCompound()));
		}
		
		if(inputGasTank.getGas() != null)
		{
			itemStack.stackTagCompound.setTag("inputGasTank", inputGasTank.getGas().write(new NBTTagCompound()));
		}
		
		if(outputGasTank.getGas() != null)
		{
			itemStack.stackTagCompound.setTag("outputGasTank", outputGasTank.getGas().write(new NBTTagCompound()));
		}
	}

	@Override
	public void readSustainedData(ItemStack itemStack) 
	{
		inputFluidTank.setFluid(FluidStack.loadFluidStackFromNBT(itemStack.stackTagCompound.getCompoundTag("inputFluidTank")));
		inputGasTank.setGas(GasStack.readFromNBT(itemStack.stackTagCompound.getCompoundTag("inputGasTank")));
		outputGasTank.setGas(GasStack.readFromNBT(itemStack.stackTagCompound.getCompoundTag("outputGasTank")));
	}
}
