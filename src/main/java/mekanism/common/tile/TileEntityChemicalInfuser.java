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
import mekanism.common.recipe.inputs.ChemicalPairInput;
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import io.netty.buffer.ByteBuf;

public class TileEntityChemicalInfuser extends TileEntityNoisyElectricBlock implements IGasHandler, ITubeConnection, IRedstoneControl, ISustainedData
{
	public GasTank leftTank = new GasTank(MAX_GAS);
	public GasTank rightTank = new GasTank(MAX_GAS);
	public GasTank centerTank = new GasTank(MAX_GAS);

	public static final int MAX_GAS = 10000;

	public int updateDelay;

	public int gasOutput = 16;

	public boolean isActive;

	public boolean clientActive;

	public double prevEnergy;

	public final double ENERGY_USAGE = usage.chemicalInfuserUsage;

	public ChemicalInfuserRecipe cachedRecipe;

	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType = RedstoneControl.DISABLED;

	public TileEntityChemicalInfuser()
	{
		super("machine.cheminfuser", "ChemicalInfuser", MachineBlockType.CHEMICAL_INFUSER.baseEnergy);
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
			ChemicalInfuserRecipe recipe = getRecipe();

			if(updateDelay > 0)
			{
				updateDelay--;

				if(updateDelay == 0 && clientActive != isActive)
				{
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
				}
			}

			ChargeUtils.discharge(3, this);

			if(inventory[0] != null && (leftTank.getGas() == null || leftTank.getStored() < leftTank.getMaxGas()))
			{
				leftTank.receive(GasTransmission.removeGas(inventory[0], leftTank.getGasType(), leftTank.getNeeded()), true);
			}

			if(inventory[1] != null && (rightTank.getGas() == null || rightTank.getStored() < rightTank.getMaxGas()))
			{
				rightTank.receive(GasTransmission.removeGas(inventory[1], rightTank.getGasType(), rightTank.getNeeded()), true);
			}

			if(inventory[2] != null && centerTank.getGas() != null)
			{
				centerTank.draw(GasTransmission.addGas(inventory[2], centerTank.getGas()), true);
			}

			if(canOperate(recipe) && getEnergy() >= ENERGY_USAGE && MekanismUtils.canFunction(this))
			{
				setActive(true);
				setEnergy(getEnergy() - ENERGY_USAGE);

				operate(recipe);
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

				TileEntity tileEntity = Coord4D.get(this).offset(EnumFacing.getFront(facing)).getTileEntity(worldObj);

				if(tileEntity instanceof IGasHandler)
				{
					if(((IGasHandler)tileEntity).canReceiveGas(EnumFacing.getFront(facing).getOpposite(), centerTank.getGas().getGas()))
					{
						centerTank.draw(((IGasHandler)tileEntity).receiveGas(EnumFacing.getFront(facing).getOpposite(), toSend, true), true);
					}
				}
			}

			prevEnergy = getEnergy();
		}
	}

	public ChemicalPairInput getInput()
	{
		return new ChemicalPairInput(leftTank.getGas(), rightTank.getGas());
	}

	public ChemicalInfuserRecipe getRecipe()
	{
		ChemicalPairInput input = getInput();
		if(cachedRecipe == null || !input.testEquality(cachedRecipe.getInput()))
		{
			cachedRecipe = RecipeHandler.getChemicalInfuserRecipe(getInput());
		}
		return cachedRecipe;
	}

	public boolean canOperate(ChemicalInfuserRecipe recipe)
	{
		return recipe != null && recipe.canOperate(leftTank, rightTank, centerTank);
	}

	public void operate(ChemicalInfuserRecipe recipe)
	{
		recipe.operate(leftTank, rightTank, centerTank);

		markDirty();
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(!worldObj.isRemote)
		{
			int type = dataStream.readInt();

			if(type == 0)
			{
				leftTank.setGas(null);
			}
			else if(type == 1)
			{
				rightTank.setGas(null);
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
			leftTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			leftTank.setGas(null);
		}

		if(dataStream.readBoolean())
		{
			rightTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			rightTank.setGas(null);
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

		if(leftTank.getGas() != null)
		{
			data.add(true);
			data.add(leftTank.getGas().getGas().getID());
			data.add(leftTank.getStored());
		}
		else {
			data.add(false);
		}

		if(rightTank.getGas() != null)
		{
			data.add(true);
			data.add(rightTank.getGas().getGas().getID());
			data.add(rightTank.getStored());
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

		leftTank.read(nbtTags.getCompoundTag("leftTank"));
		rightTank.read(nbtTags.getCompoundTag("rightTank"));
		centerTank.read(nbtTags.getCompoundTag("centerTank"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setBoolean("isActive", isActive);
		nbtTags.setInteger("controlType", controlType.ordinal());

		nbtTags.setTag("leftTank", leftTank.write(new NBTTagCompound()));
		nbtTags.setTag("rightTank", rightTank.write(new NBTTagCompound()));
		nbtTags.setTag("centerTank", centerTank.write(new NBTTagCompound()));
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
			return leftTank;
		}
		else if(side == MekanismUtils.getRight(facing))
		{
			return rightTank;
		}
		else if(side == EnumFacing.getFront(facing))
		{
			return centerTank;
		}

		return null;
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
		return side == MekanismUtils.getLeft(facing) || side == MekanismUtils.getRight(facing) || side == EnumFacing.getFront(facing);
	}

	@Override
	public boolean canReceiveGas(EnumFacing side, Gas type)
	{
		return getTank(side) != null && getTank(side) != centerTank ? getTank(side).canReceive(type) : false;
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
		return getTank(side) != null && getTank(side) == centerTank ? getTank(side).canDraw(type) : false;
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
		if(slotID == 0 || slotID == 2)
		{
			return itemstack != null && itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).canReceiveGas(itemstack, null);
		}
		else if(slotID == 1)
		{
			return itemstack != null && itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).canProvideGas(itemstack, null);
		}
		else if(slotID == 3)
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
	public void writeSustainedData(ItemStack itemStack) 
	{
		if(leftTank.getGas() != null)
		{
			itemStack.getTagCompound().setTag("leftTank", leftTank.getGas().write(new NBTTagCompound()));
		}

		if(rightTank.getGas() != null)
		{
			itemStack.getTagCompound().setTag("rightTank", rightTank.getGas().write(new NBTTagCompound()));
		}

		if(centerTank.getGas() != null)
		{
			itemStack.getTagCompound().setTag("centerTank", centerTank.getGas().write(new NBTTagCompound()));
		}
	}

	@Override
	public void readSustainedData(ItemStack itemStack) 
	{
		leftTank.setGas(GasStack.readFromNBT(itemStack.getTagCompound().getCompoundTag("leftTank")));
		rightTank.setGas(GasStack.readFromNBT(itemStack.getTagCompound().getCompoundTag("rightTank")));
		centerTank.setGas(GasStack.readFromNBT(itemStack.getTagCompound().getCompoundTag("centerTank")));
	}
}
