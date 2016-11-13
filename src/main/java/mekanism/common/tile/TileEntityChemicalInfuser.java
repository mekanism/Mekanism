package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
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
import mekanism.api.util.ListUtils;
import mekanism.common.Mekanism;
import mekanism.common.Upgrade;
import mekanism.common.Upgrade.IUpgradeInfoHandler;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITankManager;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.ChemicalPairInput;
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityChemicalInfuser extends TileEntityNoisyElectricBlock implements IGasHandler, ITubeConnection, IRedstoneControl, ISustainedData, IUpgradeTile, IUpgradeInfoHandler, ITankManager, ISecurityTile
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
	public TileComponentSecurity securityComponent = new TileComponentSecurity(this);

	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType = RedstoneControl.DISABLED;

	public TileEntityChemicalInfuser()
	{
		super("machine.cheminfuser", "ChemicalInfuser", BlockStateMachine.MachineType.CHEMICAL_INFUSER.baseEnergy);
		
		inventory = new ItemStack[5];
		upgradeComponent.setSupported(Upgrade.MUFFLING);
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
				MekanismUtils.updateBlock(worldObj, getPos());
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
				centerTank.draw(GasTransmission.emit(toSend, this, ListUtils.asList(facing)), true);
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
		super.handlePacketData(dataStream);

		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
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
	
			if(updateDelay == 0 && clientActive != isActive)
			{
				updateDelay = general.UPDATE_DELAY;
				isActive = clientActive;
				MekanismUtils.updateBlock(worldObj, getPos());
			}
		}
	}

	@Override
	public ArrayList<Object> getNetworkedData(ArrayList<Object> data)
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
	public NBTTagCompound writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setBoolean("isActive", isActive);
		nbtTags.setInteger("controlType", controlType.ordinal());

		nbtTags.setTag("leftTank", leftTank.write(new NBTTagCompound()));
		nbtTags.setTag("rightTank", rightTank.write(new NBTTagCompound()));
		nbtTags.setTag("centerTank", centerTank.write(new NBTTagCompound()));
		
		return nbtTags;
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
		else if(side == facing)
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
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList<Object>())), new Range4D(Coord4D.get(this)));

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
		return side == MekanismUtils.getLeft(facing) || side == MekanismUtils.getRight(facing) || side == facing;
	}

	@Override
	public boolean canReceiveGas(EnumFacing side, Gas type)
	{
		return (getTank(side) != null && getTank(side) != centerTank) && getTank(side).canReceive(type);
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
		return (getTank(side) != null && getTank(side) == centerTank) && getTank(side).canDraw(type);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing side)
	{
		return capability == Capabilities.GAS_HANDLER_CAPABILITY || capability == Capabilities.TUBE_CONNECTION_CAPABILITY 
				|| super.hasCapability(capability, side);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing side)
	{
		if(capability == Capabilities.GAS_HANDLER_CAPABILITY || capability == Capabilities.TUBE_CONNECTION_CAPABILITY)
		{
			return (T)this;
		}
		
		return super.getCapability(capability, side);
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		return slotID == 3 && ChargeUtils.canBeDischarged(itemstack);
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, EnumFacing side)
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
	public int[] getSlotsForFace(EnumFacing side)
	{
		if(side == MekanismUtils.getLeft(facing))
		{
			return new int[] {0};
		}
		else if(side == facing)
		{
			return new int[] {1};
		}
		else if(side == MekanismUtils.getRight(facing))
		{
			return new int[] {2};
		}
		else if(side.getAxis() == Axis.Y)
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
			ItemDataUtils.setCompound(itemStack, "leftTank", leftTank.getGas().write(new NBTTagCompound()));
		}

		if(rightTank.getGas() != null)
		{
			ItemDataUtils.setCompound(itemStack, "rightTank", rightTank.getGas().write(new NBTTagCompound()));
		}

		if(centerTank.getGas() != null)
		{
			ItemDataUtils.setCompound(itemStack, "centerTank", centerTank.getGas().write(new NBTTagCompound()));
		}
	}

	@Override
	public void readSustainedData(ItemStack itemStack) 
	{
		leftTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "leftTank")));
		rightTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "rightTank")));
		centerTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "centerTank")));
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

	@Override
	public TileComponentSecurity getSecurity() 
	{
		return securityComponent;
	}
}
