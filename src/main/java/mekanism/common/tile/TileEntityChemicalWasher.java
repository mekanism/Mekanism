package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.ITubeConnection;
import mekanism.client.sound.IHasSound;
import mekanism.common.IActiveState;
import mekanism.common.IRedstoneControl;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityChemicalWasher extends TileEntityElectricBlock implements IActiveState, IGasHandler, ITubeConnection, IRedstoneControl, IHasSound, IFluidHandler
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

	public final double ENERGY_USAGE = Mekanism.chemicalWasherUsage;

	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType = RedstoneControl.DISABLED;

	public TileEntityChemicalWasher()
	{
		super("ChemicalWasher", MachineType.CHEMICAL_WASHER.baseEnergy);
		inventory = new ItemStack[4];
	}

	@Override
	public void onUpdate()
	{
		if(worldObj.isRemote)
		{
			Mekanism.proxy.registerSound(this);

			if(updateDelay > 0)
			{
				updateDelay--;

				if(updateDelay == 0 && clientActive != isActive)
				{
					isActive = clientActive;
					MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
				}
			}
		}

		if(!worldObj.isRemote)
		{
			if(updateDelay > 0)
			{
				updateDelay--;

				if(updateDelay == 0 && clientActive != isActive)
				{
					Mekanism.packetPipeline.sendToAll(new PacketTileEntity(Coord4D.get(this), getNetworkedData(new ArrayList())));
				}
			}

			ChargeUtils.discharge(3, this);
			manageBuckets();

			if(inventory[2] != null && outputTank.getGas() != null)
			{
				outputTank.draw(GasTransmission.addGas(inventory[2], outputTank.getGas()), true);
			}

			if(canOperate() && getEnergy() >= ENERGY_USAGE && MekanismUtils.canFunction(this))
			{
				setActive(true);
				GasStack stack = RecipeHandler.getChemicalWasherOutput(inputTank, true);

				outputTank.receive(stack, true);
				fluidTank.drain(WATER_USAGE, true);

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

				TileEntity tileEntity = Coord4D.get(this).getFromSide(MekanismUtils.getRight(facing)).getTileEntity(worldObj);

				if(tileEntity instanceof IGasHandler)
				{
					if(((IGasHandler)tileEntity).canReceiveGas(MekanismUtils.getRight(facing).getOpposite(), outputTank.getGas().getGas()))
					{
						outputTank.draw(((IGasHandler)tileEntity).receiveGas(MekanismUtils.getRight(facing).getOpposite(), toSend), true);
					}
				}
			}

			prevEnergy = getEnergy();
		}
	}

	public boolean canOperate()
	{
		if(fluidTank.getFluidAmount() < WATER_USAGE || inputTank.getGas() == null || outputTank.getNeeded() == 0)
		{
			return false;
		}

		GasStack out = RecipeHandler.getChemicalWasherOutput(inputTank, false);

		if(out == null || (outputTank.getGas() != null && outputTank.getGas().getGas() != out.getGas()))
		{
			return false;
		}

		if(outputTank.getNeeded() < out.amount)
		{
			return false;
		}

		return true;
	}

	private void manageBuckets()
	{
		if(FluidContainerRegistry.isFilledContainer(inventory[0]))
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
				Mekanism.packetPipeline.sendTo(new PacketTileEntity(Coord4D.get(this), getNetworkedData(new ArrayList())), (EntityPlayerMP)player);
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

	public GasTank getTank(ForgeDirection side)
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
			Mekanism.packetPipeline.sendToAll(new PacketTileEntity(Coord4D.get(this), getNetworkedData(new ArrayList())));

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
	public boolean canTubeConnect(ForgeDirection side)
	{
		return side == MekanismUtils.getLeft(facing) || side == MekanismUtils.getRight(facing);
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
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
	public int receiveGas(ForgeDirection side, GasStack stack)
	{
		if(canReceiveGas(side, stack != null ? stack.getGas() : null))
		{
			return getTank(side).receive(stack, true);
		}

		return 0;
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount)
	{
		if(canDrawGas(side, null))
		{
			return getTank(side).draw(amount, true);
		}

		return null;
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
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
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
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
	public int[] getAccessibleSlotsFromSide(int side)
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
	public String getSoundPath()
	{
		return "ChemicalWasher.ogg";
	}

	@Override
	public float getVolumeMultiplier()
	{
		return 1;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(canFill(from, resource.getFluid()))
		{
			return fluidTank.fill(resource, doFill);
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
		return from == ForgeDirection.UP && fluid == FluidRegistry.WATER;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if(from == ForgeDirection.UP)
		{
			return new FluidTankInfo[] {fluidTank.getInfo()};
		}

		return PipeUtils.EMPTY;
	}
}
