package mekanism.common.tileentity;

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
import mekanism.common.*;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;
import net.minecraftforge.fluids.*;

public class TileEntityChemicalCombiner extends TileEntityElectricBlock implements IActiveState, IGasHandler, ITubeConnection, IRedstoneControl, IFluidHandler
{
	public GasTank gasTank = new GasTank(MAX);
	public FluidTank fluidTank = new FluidTank(MAX);
	public GasTank centerTank = new GasTank(MAX);

	public static final int MAX = 10000;

	public int updateDelay;

	public int gasOutput = 16;

	public boolean isActive;

	public boolean clientActive;

	public double prevEnergy;

	public final double ENERGY_USAGE = Mekanism.chemicalCombinerUsage;

	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType = RedstoneControl.DISABLED;

	public TileEntityChemicalCombiner()
	{
		super("ChemicalCombiner", MachineType.CHEMICAL_COMBINER.baseEnergy);
		inventory = new ItemStack[4];
	}

	@Override
	public void onUpdate()
	{
		if(worldObj.isRemote)
		{
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
					PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Coord4D.get(this), getNetworkedData(new ArrayList())));
				}
			}

			ChargeUtils.discharge(3, this);

			if(inventory[0] != null && (gasTank.getGas() == null || gasTank.getStored() < gasTank.getMaxGas()))
			{
				gasTank.receive(GasTransmission.removeGas(inventory[0], null, gasTank.getNeeded()), true);
			}

			if(inventory[1] != null && (fluidTank.getFluid() == null || fluidTank.getFluidAmount() < fluidTank.getCapacity()))
			{
				if(inventory[1].getItem() instanceof IFluidContainerItem)
				{
					fluidTank.fill(((IFluidContainerItem) inventory[1].getItem()).drain(inventory[1], fluidTank.getCapacity()-fluidTank.getFluidAmount(), true), true);
				}
				else if(FluidContainerRegistry.isFilledContainer(inventory[1]) && FluidContainerRegistry.getFluidForFilledItem(inventory[1]).equals(fluidTank.getFluid()))
				{
					fluidTank.fill(FluidContainerRegistry.getFluidForFilledItem(inventory[1]), true);
					inventory[1] = inventory[1].getItem().getContainerItemStack(inventory[1]);
				}
			}

			if(inventory[2] != null && centerTank.getGas() != null)
			{
				centerTank.draw(GasTransmission.addGas(inventory[2], centerTank.getGas()), true);
			}

			if(canOperate() && getEnergy() >= ENERGY_USAGE && MekanismUtils.canFunction(this))
			{
				setActive(true);
				GasStack stack = RecipeHandler.getChemicalCombinerOutput(gasTank, fluidTank, true);

				centerTank.receive(stack, true);

				setEnergy(getEnergy() - ENERGY_USAGE);
			}
			else {
				if(prevEnergy >= getEnergy())
				{
					setActive(false);
				}
			}

			if(centerTank.getGas() != null)
			{
				GasStack toSend = new GasStack(centerTank.getGas().getGas(), Math.min(centerTank.getStored(), gasOutput));
				centerTank.draw(GasTransmission.emitGasToNetwork(toSend, this, ForgeDirection.getOrientation(facing)), true);

				TileEntity tileEntity = Coord4D.get(this).getFromSide(ForgeDirection.getOrientation(facing)).getTileEntity(worldObj);

				if(tileEntity instanceof IGasHandler)
				{
					if(((IGasHandler)tileEntity).canReceiveGas(ForgeDirection.getOrientation(facing).getOpposite(), centerTank.getGas().getGas()))
					{
						centerTank.draw(((IGasHandler)tileEntity).receiveGas(ForgeDirection.getOrientation(facing).getOpposite(), toSend), true);
					}
				}
			}

			prevEnergy = getEnergy();
		}
	}

	public boolean canOperate()
	{
		if(gasTank.getGas() == null || fluidTank.getFluid() == null || centerTank.getNeeded() == 0)
		{
			return false;
		}

		GasStack out = RecipeHandler.getChemicalCombinerOutput(gasTank, fluidTank, false);

		if(out == null)
		{
			return false;
		}

		if(centerTank.getNeeded() < out.amount)
		{
			return false;
		}

		return true;
	}

	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		if(!worldObj.isRemote)
		{
			int type = dataStream.readInt();

			if(type == 0)
			{
				gasTank.setGas(null);
			}
			else if(type == 1)
			{
				fluidTank.setFluid(null);
			}

			for(EntityPlayer player : playersUsing)
			{
				PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Coord4D.get(this), getNetworkedData(new ArrayList())), player);
			}

			return;
		}

		super.handlePacketData(dataStream);

		isActive = dataStream.readBoolean();
		controlType = RedstoneControl.values()[dataStream.readInt()];

		if(dataStream.readBoolean())
		{
			gasTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			gasTank.setGas(null);
		}

		if(dataStream.readBoolean())
		{
			fluidTank.setFluid(new FluidStack(FluidRegistry.getFluid(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			fluidTank.setFluid(null);
		}

		if(dataStream.readBoolean())
		{
			centerTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			centerTank.setGas(null);
		}


		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(isActive);
		data.add(controlType.ordinal());

		if(gasTank.getGas() != null)
		{
			data.add(true);
			data.add(gasTank.getGas().getGas().getID());
			data.add(gasTank.getStored());
		}
		else {
			data.add(false);
		}

		if(fluidTank.getFluid() != null)
		{
			data.add(true);
			data.add(fluidTank.getFluid().getFluid().getID());
			data.add(fluidTank.getFluidAmount());
		}
		else {
			data.add(false);
		}

		if(centerTank.getGas() != null)
		{
			data.add(true);
			data.add(centerTank.getGas().getGas().getID());
			data.add(centerTank.getStored());
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

		gasTank.read(nbtTags.getCompoundTag("gasTank"));
		fluidTank.readFromNBT(nbtTags.getCompoundTag("fluidTank"));
		centerTank.read(nbtTags.getCompoundTag("centerTank"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setBoolean("isActive", isActive);
		nbtTags.setInteger("controlType", controlType.ordinal());

		nbtTags.setCompoundTag("gasTank", gasTank.write(new NBTTagCompound()));
		nbtTags.setCompoundTag("fluidTank", fluidTank.writeToNBT(new NBTTagCompound()));
		nbtTags.setCompoundTag("centerTank", centerTank.write(new NBTTagCompound()));
	}

	@Override
	public boolean canSetFacing(int i)
	{
		return i != 0 && i != 1;
	}

	public GasTank getGasTank(ForgeDirection side)
	{
		if(side == MekanismUtils.getLeft(facing))
		{
			return gasTank;
		}
		else if(side == ForgeDirection.getOrientation(facing))
		{
			return centerTank;
		}

		return null;
	}

	public FluidTank getFluidTank(ForgeDirection side)
	{
		if(side == MekanismUtils.getRight(facing))
		{
			return fluidTank;
		}

		return null;
	}

	public int getScaledGasLevel(int i)
	{
		return gasTank != null ? gasTank.getStored()*i / MAX : 0;
	}

	public int getScaledFluidLevel(int i)
	{
		return fluidTank != null ? fluidTank.getFluidAmount()*i / MAX : 0;
	}

	public int getScaledOutputLevel(int i)
	{
		return centerTank != null ? centerTank.getStored()*i / MAX : 0;
	}

	@Override
	public void setActive(boolean active)
	{
		isActive = active;

		if(clientActive != active && updateDelay == 0)
		{
			PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Coord4D.get(this), getNetworkedData(new ArrayList())));

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
		return side == MekanismUtils.getLeft(facing) || side == MekanismUtils.getRight(facing) || side == ForgeDirection.getOrientation(facing);
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return getGasTank(side) != null && getGasTank(side) != centerTank ? getGasTank(side).canReceive(type) : false;
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
			return getGasTank(side).receive(stack, true);
		}

		return 0;
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount)
	{
		if(canDrawGas(side, null))
		{
			return getGasTank(side).draw(amount, true);
		}

		return null;
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return getGasTank(side) != null && getGasTank(side) == centerTank ? getGasTank(side).canDraw(type) : false;
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 3)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}

		return false;
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 0)
		{
			return itemstack != null && itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).canReceiveGas(itemstack, null);
		}
		if(slotID == 1)
		{
			return itemstack != null && itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).canProvideGas(itemstack, null);
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
		else if(side == facing)
		{
			return new int[] {1};
		}
		else if(side == MekanismUtils.getRight(facing).ordinal())
		{
			return new int[] {2};
		}
		else if(side == 0 || side == 1)
		{
			return new int[3];
		}

		return InventoryUtils.EMPTY;
	}

	@Override
	public int fill(ForgeDirection side, FluidStack resource, boolean doFill) {
		return getFluidTank(side) == null ? 0 : getFluidTank(side).fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection side, FluidStack resource, boolean doDrain) {
		return getFluidTank(side) == null && getFluidTank(side).getFluid().isFluidEqual(resource) ? null : getFluidTank(side).drain(resource.amount, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection side, int maxDrain, boolean doDrain) {
		return getFluidTank(side) == null ? null : getFluidTank(side).drain(maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection side, Fluid fluid) {
		return getFluidTank(side) != null && (getFluidTank(side).getFluid() == null || getFluidTank(side).getFluid().getFluid() == fluid);
	}

	@Override
	public boolean canDrain(ForgeDirection side, Fluid fluid) {
		return getFluidTank(side) != null && getFluidTank(side).getFluid().getFluid() == fluid;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection side) {
		if(getFluidTank(side) == null)
		{
			return new FluidTankInfo[0];
		}
		return new FluidTankInfo[] {new FluidTankInfo(getFluidTank(side))};
	}
}
