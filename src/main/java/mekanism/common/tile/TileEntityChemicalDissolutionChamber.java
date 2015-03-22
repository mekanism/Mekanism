package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

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
import mekanism.common.Upgrade;
import mekanism.common.base.ITankManager;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.DissolutionRecipe;
import mekanism.common.tile.component.TileComponentAdvancedUpgrade;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StatUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityChemicalDissolutionChamber extends TileEntityNoisyElectricBlock implements ITubeConnection, IRedstoneControl, IGasHandler, IUpgradeTile, ISustainedData, ITankManager
{
	public GasTank injectTank = new GasTank(MAX_GAS);
	public GasTank outputTank = new GasTank(MAX_GAS);

	public static final int MAX_GAS = 10000;

	public static final int BASE_INJECT_USAGE = 1;

	public double injectUsage = 1;

	public int injectUsageThisTick;

	public int updateDelay;

	public int gasOutput = 256;

	public boolean isActive;

	public boolean clientActive;

	public double prevEnergy;

	public int operatingTicks = 0;

	public int BASE_TICKS_REQUIRED = 100;

	public int ticksRequired = 100;

	public final double BASE_ENERGY_USAGE = usage.chemicalDissolutionChamberUsage;

	public double energyUsage = usage.chemicalDissolutionChamberUsage;

	public DissolutionRecipe cachedRecipe;
	
	public TileComponentUpgrade upgradeComponent = new TileComponentAdvancedUpgrade(this, 4);

	public RedstoneControl controlType = RedstoneControl.DISABLED;

	public TileEntityChemicalDissolutionChamber()
	{
		super("machine.dissolution", "ChemicalDissolutionChamber", MachineType.CHEMICAL_DISSOLUTION_CHAMBER.baseEnergy);
		inventory = new ItemStack[5];
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
			if(updateDelay > 0)
			{
				updateDelay--;

				if(updateDelay == 0 && clientActive != isActive)
				{
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
				}
			}

			ChargeUtils.discharge(3, this);

			if(inventory[0] != null && injectTank.getNeeded() > 0)
			{
				injectTank.receive(GasTransmission.removeGas(inventory[0], GasRegistry.getGas("sulfuricAcid"), injectTank.getNeeded()), true);
			}

			if(inventory[2] != null && outputTank.getGas() != null)
			{
				outputTank.draw(GasTransmission.addGas(inventory[2], outputTank.getGas()), true);
			}

			boolean changed = false;
			
			DissolutionRecipe recipe = getRecipe();

			injectUsageThisTick = StatUtils.inversePoisson(injectUsage);

			if(canOperate(recipe) && getEnergy() >= energyUsage && injectTank.getStored() >= injectUsageThisTick && MekanismUtils.canFunction(this))
			{
				setActive(true);
				setEnergy(getEnergy() - energyUsage);
				minorOperate();

				if((operatingTicks+1) < ticksRequired)
				{
					operatingTicks++;
				}
				else {
					operate(recipe);
					operatingTicks = 0;
				}
			}
			else {
				if(prevEnergy >= getEnergy())
				{
					changed = true;
					setActive(false);
				}
			}

			if(changed && !canOperate(recipe))
			{
				operatingTicks = 0;
			}

			prevEnergy = getEnergy();

			if(outputTank.getGas() != null)
			{
				GasStack toSend = new GasStack(outputTank.getGas().getGas(), Math.min(outputTank.getStored(), gasOutput));

				TileEntity tileEntity = Coord4D.get(this).getFromSide(MekanismUtils.getRight(facing)).getTileEntity(worldObj);

				if(tileEntity instanceof IGasHandler)
				{
					if(((IGasHandler)tileEntity).canReceiveGas(MekanismUtils.getRight(facing).getOpposite(), outputTank.getGas().getGas()))
					{
						outputTank.draw(((IGasHandler)tileEntity).receiveGas(MekanismUtils.getRight(facing).getOpposite(), toSend, true), true);
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
			return RecipeHandler.getDissolutionRecipe(new ItemStackInput(itemstack)) != null;
		}
		else if(slotID == 3)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}

		return false;
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 2)
		{
			return itemstack != null && itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).canProvideGas(itemstack, null);
		}

		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(side == MekanismUtils.getLeft(facing).ordinal() || side == 1)
		{
			return new int[] {1};
		}
		else if(side == 0)
		{
			return new int[] {0};
		}
		else if(side == MekanismUtils.getRight(facing).ordinal())
		{
			return new int[] {2};
		}

		return InventoryUtils.EMPTY;
	}

	public double getScaledProgress()
	{
		return ((double)operatingTicks) / ((double)ticksRequired);
	}

	public DissolutionRecipe getRecipe()
	{
		ItemStackInput input = getInput();
		
		if(cachedRecipe == null || !input.testEquality(cachedRecipe.getInput()))
		{
			cachedRecipe = RecipeHandler.getDissolutionRecipe(getInput());
		}
		 
		return cachedRecipe;
	}

	public ItemStackInput getInput()
	{
		return new ItemStackInput(inventory[1]);
	}

	public boolean canOperate(DissolutionRecipe recipe)
	{
		return recipe != null && recipe.canOperate(inventory, outputTank);
	}

	public void operate(DissolutionRecipe recipe)
	{
		recipe.operate(inventory, outputTank);

		markDirty();
	}

	public void minorOperate()
	{
		injectTank.draw(injectUsageThisTick, true);
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);

		isActive = dataStream.readBoolean();
		controlType = RedstoneControl.values()[dataStream.readInt()];
		operatingTicks = dataStream.readInt();

		if(dataStream.readBoolean())
		{
			injectTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			injectTank.setGas(null);
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
		data.add(operatingTicks);

		if(injectTank.getGas() != null)
		{
			data.add(true);
			data.add(injectTank.getGas().getGas().getID());
			data.add(injectTank.getStored());
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
		operatingTicks = nbtTags.getInteger("operatingTicks");
		injectTank.read(nbtTags.getCompoundTag("injectTank"));
		outputTank.read(nbtTags.getCompoundTag("gasTank"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setBoolean("isActive", isActive);
		nbtTags.setInteger("controlType", controlType.ordinal());
		nbtTags.setInteger("operatingTicks", operatingTicks);
		nbtTags.setTag("injectTank", injectTank.write(new NBTTagCompound()));
		nbtTags.setTag("gasTank", outputTank.write(new NBTTagCompound()));
	}

	@Override
	public boolean canSetFacing(int i)
	{
		return i != 0 && i != 1;
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
		return side == MekanismUtils.getLeft(facing) || side == MekanismUtils.getRight(facing);
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
		if(canReceiveGas(side, stack.getGas()))
		{
			return injectTank.receive(stack, doTransfer);
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
		return null;
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount)
	{
		return drawGas(side, amount, true);
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return side == MekanismUtils.getLeft(facing) && type == GasRegistry.getGas("sulfuricAcid");
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return false;
	}

	@Override
	public TileComponentUpgrade getComponent() 
	{
		return upgradeComponent;
	}

	@Override
	public void writeSustainedData(ItemStack itemStack) 
	{
		if(injectTank.getGas() != null)
		{
			itemStack.stackTagCompound.setTag("injectTank", injectTank.getGas().write(new NBTTagCompound()));
		}
		
		if(outputTank.getGas() != null)
		{
			itemStack.stackTagCompound.setTag("outputTank", outputTank.getGas().write(new NBTTagCompound()));
		}
	}

	@Override
	public void readSustainedData(ItemStack itemStack) 
	{
		injectTank.setGas(GasStack.readFromNBT(itemStack.stackTagCompound.getCompoundTag("injectTank")));
		outputTank.setGas(GasStack.readFromNBT(itemStack.stackTagCompound.getCompoundTag("outputTank")));
	}

	@Override
	public void recalculateUpgradables(Upgrade upgrade)
	{
		super.recalculateUpgradables(upgrade);

		switch(upgrade)
		{
			case GAS:
				injectUsage = MekanismUtils.getSecondaryEnergyPerTickMean(this, BASE_INJECT_USAGE);
				break;
			case SPEED:
				ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
				energyUsage = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_USAGE);
				injectUsage = MekanismUtils.getSecondaryEnergyPerTickMean(this, BASE_INJECT_USAGE);
				break;
			case ENERGY:
				energyUsage = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_USAGE);
				maxEnergy = MekanismUtils.getMaxEnergy(this, BASE_MAX_ENERGY);
				break;
			default:
				break;
		}
	}
	
	@Override
	public Object[] getTanks() 
	{
		return new Object[] {injectTank, outputTank};
	}
}
