package mekanism.common.tile;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.usage;
import mekanism.api.Range4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.Mekanism;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISustainedData;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.GasInput;
import mekanism.common.recipe.machines.WasherRecipe;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;

import io.netty.buffer.ByteBuf;

public class TileEntityChemicalWasher extends TileEntityNoisyElectricBlock implements IGasHandler, ITubeConnection, IRedstoneControl, IFluidHandler, ISustainedData
{
	public FluidTank fluidTank = new FluidTank(MAX_FLUID);
	public GasTank inputTank = new GasTank(MAX_GAS);
	public GasTank outputTank = new GasTank(MAX_GAS);

	public static final int MAX_GAS = 10000;
	public static final int MAX_FLUID = 10000;

	public static int WATER_USAGE = 5;

	public int updateDelay;

	public int gasOutput = 16;

	public boolean isActive;

	public boolean clientActive;

	public double prevEnergy;

	public final double ENERGY_USAGE = usage.chemicalWasherUsage;

	public WasherRecipe cachedRecipe;

	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType = RedstoneControl.DISABLED;

	public TileEntityChemicalWasher()
	{
		super("washer", "ChemicalWasher", MachineBlockType.CHEMICAL_WASHER.baseEnergy);
		inventory = new ItemStack[4];
	}

	@Override
	public void onUpdate()
	{
		if(worldObj.isRemote && updateDelay > 0)
		{
			updateDelay--;

			if(updateDelay == 0 && clientActive != isActive)
			{
				isActive = clientActive;
				MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
			}
		}

		if(!worldObj.isRemote)
		{
			WasherRecipe recipe = getRecipe();

			if(updateDelay > 0)
			{
				updateDelay--;

				if(updateDelay == 0 && clientActive != isActive)
				{
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
				}
			}

			ChargeUtils.discharge(3, this);
			manageBuckets();

			if(inventory[2] != null && outputTank.getGas() != null)
			{
				outputTank.draw(GasTransmission.addGas(inventory[2], outputTank.getGas()), true);
			}

			if(canOperate(recipe) && getEnergy() >= ENERGY_USAGE && MekanismUtils.canFunction(this))
			{
				setActive(true);

				operate(recipe);

				setEnergy(getEnergy() - ENERGY_USAGE);
			}
			else {
				if(prevEnergy >= getEnergy())
				{
					setActive(false);
				}
			}

			if(outputTank.getGas() != null)
			{
				GasStack toSend = new GasStack(outputTank.getGas().getGas(), Math.min(outputTank.getStored(), gasOutput));

				TileEntity tileEntity = Coord4D.get(this).offset(MekanismUtils.getRight(facing)).getTileEntity(worldObj);

				if(tileEntity instanceof IGasHandler)
				{
					if(((IGasHandler)tileEntity).canReceiveGas(MekanismUtils.getRight(facing).getOpposite(), outputTank.getGas().getGas()))
					{
						outputTank.draw(((IGasHandler)tileEntity).receiveGas(MekanismUtils.getRight(facing).getOpposite(), toSend, true), true);
					}
				}
			}

			prevEnergy = getEnergy();
		}
	}

	public WasherRecipe getRecipe()
	{
		GasInput input = getInput();
		if(cachedRecipe == null || !input.testEquality(cachedRecipe.getInput()))
		{
			cachedRecipe = RecipeHandler.getChemicalWasherRecipe(getInput());
		}
		return cachedRecipe;
	}

	public GasInput getInput()
	{
		return new GasInput(inputTank.getGas());
	}

	public boolean canOperate(WasherRecipe recipe)
	{
		return recipe != null && recipe.canOperate(inputTank, fluidTank, outputTank);
	}

	public void operate(WasherRecipe recipe)
	{
		recipe.operate(inputTank, fluidTank, outputTank);
	}

	private void manageBuckets()
	{
		if(inventory[0] != null)
		{
			if(inventory[0].getItem() instanceof IFluidContainerItem)
			{
				fluidTank.fill(FluidContainerUtils.extractFluid(fluidTank, inventory[0], FluidRegistry.WATER), true);
				
				if(((IFluidContainerItem)inventory[0].getItem()).getFluid(inventory[0]) == null || fluidTank.getFluidAmount() == fluidTank.getCapacity())
				{
					if(inventory[1] == null)
					{
						inventory[1] = inventory[0].copy();
						inventory[0] = null;
						
						markDirty();
					}
				}
			}
			else if(FluidContainerRegistry.isFilledContainer(inventory[0]))
			{
				FluidStack itemFluid = FluidContainerRegistry.getFluidForFilledItem(inventory[0]);
	
				if((fluidTank.getFluid() == null && itemFluid.amount <= MAX_FLUID) || fluidTank.getFluid().amount+itemFluid.amount <= MAX_FLUID)
				{
					if(itemFluid.getFluid() != FluidRegistry.WATER || (fluidTank.getFluid() != null && !fluidTank.getFluid().isFluidEqual(itemFluid)))
					{
						return;
					}
	
					ItemStack containerItem = inventory[0].getItem().getContainerItem(inventory[0]);
	
					boolean filled = false;
	
					if(containerItem != null)
					{
						if(inventory[1] == null || (inventory[1].isItemEqual(containerItem) && inventory[1].stackSize+1 <= containerItem.getMaxStackSize()))
						{
							inventory[0] = null;
	
							if(inventory[1] == null)
							{
								inventory[1] = containerItem;
							}
							else {
								inventory[1].stackSize++;
							}
	
							filled = true;
						}
					}
					else {
						inventory[0].stackSize--;
	
						if(inventory[0].stackSize == 0)
						{
							inventory[0] = null;
						}
	
						filled = true;
					}
	
					if(filled)
					{
						fluidTank.fill(itemFluid, true);
						markDirty();
					}
				}
			}
		}
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(!worldObj.isRemote)
		{
			int type = dataStream.readInt();

			if(type == 0)
			{
				inputTank.setGas(null);
			}

			for(EntityPlayer player : playersUsing)
			{
				Mekanism.packetHandler.sendTo(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), (EntityPlayerMP)player);
			}

			return;
		}

		super.handlePacketData(dataStream);

		isActive = dataStream.readBoolean();
		controlType = RedstoneControl.values()[dataStream.readInt()];

		if(dataStream.readBoolean())
		{
			fluidTank.setFluid(new FluidStack(FluidRegistry.getFluid(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			fluidTank.setFluid(null);
		}

		if(dataStream.readBoolean())
		{
			inputTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			inputTank.setGas(null);
		}

		if(dataStream.readBoolean())
		{
			outputTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			outputTank.setGas(null);
		}


		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(isActive);
		data.add(controlType.ordinal());

		if(fluidTank.getFluid() != null)
		{
			data.add(true);
			data.add(fluidTank.getFluid().getFluid().getID());
			data.add(fluidTank.getFluidAmount());
		}
		else {
			data.add(false);
		}

		if(inputTank.getGas() != null)
		{
			data.add(true);
			data.add(inputTank.getGas().getGas().getID());
			data.add(inputTank.getStored());
		}
		else {
			data.add(false);
		}

		if(outputTank.getGas() != null)
		{
			data.add(true);
			data.add(outputTank.getGas().getGas().getID());
			data.add(outputTank.getStored());
		}
		else {
			data.add(false);
		}

		return data;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		isActive = nbtTags.getBoolean("isActive");
		controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];

		fluidTank.readFromNBT(nbtTags.getCompoundTag("leftTank"));
		inputTank.read(nbtTags.getCompoundTag("rightTank"));
		outputTank.read(nbtTags.getCompoundTag("centerTank"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setBoolean("isActive", isActive);
		nbtTags.setInteger("controlType", controlType.ordinal());

		nbtTags.setTag("leftTank", fluidTank.writeToNBT(new NBTTagCompound()));
		nbtTags.setTag("rightTank", inputTank.write(new NBTTagCompound()));
		nbtTags.setTag("centerTank", outputTank.write(new NBTTagCompound()));
	}

	@Override
	public boolean canSetFacing(int i)
	{
		return i != 0 && i != 1;
	}

	public GasTank getTank(EnumFacing side)
	{
		if(side == MekanismUtils.getLeft(facing))
		{
			return inputTank;
		}
		else if(side == MekanismUtils.getRight(facing))
		{
			return outputTank;
		}

		return null;
	}

	public int getScaledFluidLevel(int i)
	{
		return fluidTank != null ? fluidTank.getFluidAmount()*i / MAX_FLUID : 0;
	}

	public int getScaledInputGasLevel(int i)
	{
		return inputTank != null ? inputTank.getStored()*i / MAX_GAS : 0;
	}

	public int getScaledOutputGasLevel(int i)
	{
		return outputTank != null ? outputTank.getStored()*i / MAX_GAS : 0;
	}

	@Override
	public void setActive(boolean active)
	{
		isActive = active;

		if(clientActive != active && updateDelay == 0)
		{
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));

			updateDelay = 10;
			clientActive = active;
		}
	}

	@Override
	public boolean getActive()
	{
		return isActive;
	}

	@Override
	public boolean renderUpdate()
	{
		return false;
	}

	@Override
	public boolean lightUpdate()
	{
		return true;
	}

	@Override
	public boolean canTubeConnect(EnumFacing side)
	{
		return side == MekanismUtils.getLeft(facing) || side == MekanismUtils.getRight(facing);
	}

	@Override
	public boolean canReceiveGas(EnumFacing side, Gas type)
	{
		return getTank(side) != null && getTank(side) != outputTank ? getTank(side).canReceive(type) : false;
	}

	@Override
	public RedstoneControl getControlType()
	{
		return controlType;
	}

	@Override
	public void setControlType(RedstoneControl type)
	{
		controlType = type;
		MekanismUtils.saveChunk(this);
	}

	@Override
	public boolean canPulse()
	{
		return false;
	}

	@Override
	public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer)
	{
		if(canReceiveGas(side, stack != null ? stack.getGas() : null))
		{
			return getTank(side).receive(stack, doTransfer);
		}

		return 0;
	}

	@Override
	public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer)
	{
		if(canDrawGas(side, null))
		{
			return getTank(side).draw(amount, doTransfer);
		}

		return null;
	}

	@Override
	public boolean canDrawGas(EnumFacing side, Gas type)
	{
		return getTank(side) == outputTank ? getTank(side).canDraw(type) : false;
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return FluidContainerRegistry.getFluidForFilledItem(itemstack) != null && FluidContainerRegistry.getFluidForFilledItem(itemstack).getFluid() == FluidRegistry.WATER;
		}
		else if(slotID == 2)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}

		return false;
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, EnumFacing side)
	{
		if(slotID == 1)
		{
			return itemstack != null && itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).canProvideGas(itemstack, null);
		}
		else if(slotID == 2)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
		}

		return false;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side)
	{
		if(side == MekanismUtils.getLeft(facing).ordinal())
		{
			return new int[] {0};
		}
		else if(side == MekanismUtils.getRight(facing).ordinal())
		{
			return new int[] {1};
		}
		else if(side == 0 || side == 1)
		{
			return new int[2];
		}

		return InventoryUtils.EMPTY;
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill)
	{
		if(canFill(from, resource.getFluid()))
		{
			return fluidTank.fill(resource, doFill);
		}

		return 0;
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		return from == EnumFacing.UP && fluid == FluidRegistry.WATER;
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		if(from == EnumFacing.UP)
		{
			return new FluidTankInfo[] {fluidTank.getInfo()};
		}

		return PipeUtils.EMPTY;
	}

	@Override
	public void writeSustainedData(ItemStack itemStack) 
	{
		if(fluidTank.getFluid() != null)
		{
			itemStack.getTagCompound().setTag("fluidTank", fluidTank.getFluid().writeToNBT(new NBTTagCompound()));
		}
		
		if(inputTank.getGas() != null)
		{
			itemStack.getTagCompound().setTag("inputTank", inputTank.getGas().write(new NBTTagCompound()));
		}
		
		if(outputTank.getGas() != null)
		{
			itemStack.getTagCompound().setTag("outputTank", outputTank.getGas().write(new NBTTagCompound()));
		}
	}

	@Override
	public void readSustainedData(ItemStack itemStack) 
	{
		fluidTank.setFluid(FluidStack.loadFluidStackFromNBT(itemStack.getTagCompound().getCompoundTag("fluidTank")));
		inputTank.setGas(GasStack.readFromNBT(itemStack.getTagCompound().getCompoundTag("inputTank")));
		outputTank.setGas(GasStack.readFromNBT(itemStack.getTagCompound().getCompoundTag("outputTank")));
	}
}
