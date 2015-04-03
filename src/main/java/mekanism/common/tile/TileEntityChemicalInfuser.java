package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

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
import mekanism.common.Upgrade;
import mekanism.common.Upgrade.IUpgradeInfoHandler;
import mekanism.common.base.ITankManager;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.ChemicalPairInput;
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityChemicalInfuser extends TileEntityNoisyElectricBlock implements IGasHandler, ITubeConnection, IRedstoneControl, ISustainedData, IUpgradeTile, IUpgradeInfoHandler, ITankManager
{
	public GasTank leftTank = new GasTank(MAX_GAS);
	public GasTank rightTank = new GasTank(MAX_GAS);
	public GasTank centerTank = new GasTank(MAX_GAS);

	public static final int MAX_GAS = 10000;

	public int updateDelay;

	public int gasOutput = 256;

	public boolean isActive;

	public boolean clientActive;

	public double prevEnergy;

	public final double BASE_ENERGY_USAGE = usage.chemicalInfuserUsage;
	
	public double energyPerTick = BASE_ENERGY_USAGE;

	public ChemicalInfuserRecipe cachedRecipe;
	
	public double clientEnergyUsed;
	
	public TileComponentUpgrade upgradeComponent = new TileComponentUpgrade(this, 4);

	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType = RedstoneControl.DISABLED;

	public TileEntityChemicalInfuser()
	{
		super("machine.cheminfuser", "ChemicalInfuser", MachineType.CHEMICAL_INFUSER.baseEnergy);
		inventory = new ItemStack[5];
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
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
			
			ChemicalInfuserRecipe recipe = getRecipe();

			if(canOperate(recipe) && getEnergy() >= energyPerTick && MekanismUtils.canFunction(this))
			{
				setActive(true);
				
				int operations = operate(recipe);
				double prev = getEnergy();
				
				setEnergy(getEnergy() - energyPerTick*operations);
				clientEnergyUsed = prev-getEnergy();
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

				TileEntity tileEntity = Coord4D.get(this).getFromSide(ForgeDirection.getOrientation(facing)).getTileEntity(worldObj);

				if(tileEntity instanceof IGasHandler)
				{
					if(((IGasHandler)tileEntity).canReceiveGas(ForgeDirection.getOrientation(facing).getOpposite(), centerTank.getGas().getGas()))
					{
						centerTank.draw(((IGasHandler)tileEntity).receiveGas(ForgeDirection.getOrientation(facing).getOpposite(), toSend, true), true);
					}
				}
			}

			prevEnergy = getEnergy();
		}
	}
	
	public int getUpgradedUsage(ChemicalInfuserRecipe recipe)
	{
		int possibleProcess = (int)Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED));
		
		if(leftTank.getGasType() == recipe.recipeInput.leftGas.getGas())
		{
			possibleProcess = Math.min(leftTank.getStored()/recipe.recipeInput.leftGas.amount, possibleProcess);
			possibleProcess = Math.min(rightTank.getStored()/recipe.recipeInput.rightGas.amount, possibleProcess);
		}
		else {
			possibleProcess = Math.min(leftTank.getStored()/recipe.recipeInput.rightGas.amount, possibleProcess);
			possibleProcess = Math.min(rightTank.getStored()/recipe.recipeInput.leftGas.amount, possibleProcess);
		}
		
		possibleProcess = Math.min(centerTank.getNeeded()/recipe.recipeOutput.output.amount, possibleProcess);
		possibleProcess = Math.min((int)(getEnergy()/energyPerTick), possibleProcess);
		
		return possibleProcess;
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

	public int operate(ChemicalInfuserRecipe recipe)
	{
		int operations = getUpgradedUsage(recipe);
		
		recipe.operate(leftTank, rightTank, centerTank, operations);

		markDirty();
		
		return operations;
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
		clientEnergyUsed = dataStream.readDouble();

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
		data.add(clientEnergyUsed);

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

	public GasTank getTank(ForgeDirection side)
	{
		if(side == MekanismUtils.getLeft(facing))
		{
			return leftTank;
		}
		else if(side == MekanismUtils.getRight(facing))
		{
			return rightTank;
		}
		else if(side == ForgeDirection.getOrientation(facing))
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
	public boolean canTubeConnect(ForgeDirection side)
	{
		return side == MekanismUtils.getLeft(facing) || side == MekanismUtils.getRight(facing) || side == ForgeDirection.getOrientation(facing);
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
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
	public int receiveGas(ForgeDirection side, GasStack stack, boolean doTransfer)
	{
		if(canReceiveGas(side, stack != null ? stack.getGas() : null))
		{
			return getTank(side).receive(stack, doTransfer);
		}

		return 0;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack)
	{
		return receiveGas(side, stack, true);
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount, boolean doTransfer)
	{
		if(canDrawGas(side, null))
		{
			return getTank(side).draw(amount, doTransfer);
		}

		return null;
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount)
	{
		return drawGas(side, amount, true);
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
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
			itemStack.stackTagCompound.setTag("leftTank", leftTank.getGas().write(new NBTTagCompound()));
		}

		if(rightTank.getGas() != null)
		{
			itemStack.stackTagCompound.setTag("rightTank", rightTank.getGas().write(new NBTTagCompound()));
		}

		if(centerTank.getGas() != null)
		{
			itemStack.stackTagCompound.setTag("centerTank", centerTank.getGas().write(new NBTTagCompound()));
		}
	}

	@Override
	public void readSustainedData(ItemStack itemStack) 
	{
		leftTank.setGas(GasStack.readFromNBT(itemStack.stackTagCompound.getCompoundTag("leftTank")));
		rightTank.setGas(GasStack.readFromNBT(itemStack.stackTagCompound.getCompoundTag("rightTank")));
		centerTank.setGas(GasStack.readFromNBT(itemStack.stackTagCompound.getCompoundTag("centerTank")));
	}
	
	@Override
	public TileComponentUpgrade getComponent() 
	{
		return upgradeComponent;
	}
	
	@Override
	public void recalculateUpgradables(Upgrade upgrade)
	{
		super.recalculateUpgradables(upgrade);

		switch(upgrade)
		{
			case ENERGY:
				maxEnergy = MekanismUtils.getMaxEnergy(this, BASE_MAX_ENERGY);
				energyPerTick = MekanismUtils.getBaseEnergyPerTick(this, BASE_ENERGY_USAGE);
			default:
				break;
		}
	}
	
	@Override
	public List<String> getInfo(Upgrade upgrade) 
	{
		return upgrade == Upgrade.SPEED ? upgrade.getExpScaledInfo(this) : upgrade.getMultScaledInfo(this);
	}
	
	@Override
	public Object[] getTanks() 
	{
		return new Object[] {leftTank, rightTank, centerTank};
	}
}
