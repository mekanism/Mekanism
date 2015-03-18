package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.usage;
import mekanism.api.Range4D;
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
import mekanism.common.recipe.machines.OxidationRecipe;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityChemicalOxidizer extends TileEntityNoisyElectricBlock implements ITubeConnection, IRedstoneControl, IUpgradeTile, ISustainedData, ITankManager
{
	public GasTank gasTank = new GasTank(MAX_GAS);

	public static final int MAX_GAS = 10000;

	public int updateDelay;

	public int gasOutput = 256;

	public boolean isActive;

	public boolean clientActive;

	public double prevEnergy;

	public int operatingTicks = 0;

	public int BASE_TICKS_REQUIRED = 100;

	public int ticksRequired = BASE_TICKS_REQUIRED;

	public final double BASE_ENERGY_USAGE = usage.rotaryCondensentratorUsage;

	public double energyUsage = BASE_ENERGY_USAGE;

	public OxidationRecipe cachedRecipe;

	public RedstoneControl controlType = RedstoneControl.DISABLED;
	
	public TileComponentUpgrade upgradeComponent = new TileComponentUpgrade(this, 3);

	public TileEntityChemicalOxidizer()
	{
		super("machine.oxidizer", "ChemicalOxidizer", MachineType.CHEMICAL_OXIDIZER.baseEnergy);
		inventory = new ItemStack[4];
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

			ChargeUtils.discharge(1, this);

			if(inventory[2] != null && gasTank.getGas() != null)
			{
				gasTank.draw(GasTransmission.addGas(inventory[2], gasTank.getGas()), true);
			}
			
			OxidationRecipe recipe = getRecipe();

			if(canOperate(recipe) && getEnergy() >= energyUsage && MekanismUtils.canFunction(this))
			{
				setActive(true);
				setEnergy(getEnergy() - energyUsage);

				if(operatingTicks < ticksRequired)
				{
					operatingTicks++;
				}
				else {
					operate(recipe);

					operatingTicks = 0;
					markDirty();
				}
			}
			else {
				if(prevEnergy >= getEnergy())
				{
					setActive(false);
				}
			}

			prevEnergy = getEnergy();

			if(gasTank.getGas() != null)
			{
				GasStack toSend = new GasStack(gasTank.getGas().getGas(), Math.min(gasTank.getStored(), gasOutput));

				TileEntity tileEntity = Coord4D.get(this).getFromSide(MekanismUtils.getRight(facing)).getTileEntity(worldObj);

				if(tileEntity instanceof IGasHandler)
				{
					if(((IGasHandler)tileEntity).canReceiveGas(MekanismUtils.getRight(facing).getOpposite(), gasTank.getGas().getGas()))
					{
						gasTank.draw(((IGasHandler)tileEntity).receiveGas(MekanismUtils.getRight(facing).getOpposite(), toSend, true), true);
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
			return RecipeHandler.getOxidizerRecipe(new ItemStackInput(itemstack)) != null;
		}
		else if(slotID == 1)
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
		if(side == MekanismUtils.getLeft(facing).ordinal())
		{
			return new int[] {0};
		}
		else if(side == 0 || side == 1)
		{
			return new int[] {1};
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

	public OxidationRecipe getRecipe()
	{
		ItemStackInput input = getInput();
		
		if(cachedRecipe == null || !input.testEquality(cachedRecipe.getInput()))
		{
			cachedRecipe = RecipeHandler.getOxidizerRecipe(getInput());
		}
		
		return cachedRecipe;
	}

	public ItemStackInput getInput()
	{
		return new ItemStackInput(inventory[0]);
	}

	public boolean canOperate(OxidationRecipe recipe)
	{
		return recipe != null && recipe.canOperate(inventory, gasTank);
	}

	public void operate(OxidationRecipe recipe)
	{
		recipe.operate(inventory, gasTank);

		markDirty();
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
			gasTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			gasTank.setGas(null);
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

		if(gasTank.getGas() != null)
		{
			data.add(true);
			data.add(gasTank.getGas().getGas().getID());
			data.add(gasTank.getStored());
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
		gasTank.read(nbtTags.getCompoundTag("gasTank"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setBoolean("isActive", isActive);
		nbtTags.setInteger("controlType", controlType.ordinal());
		nbtTags.setInteger("operatingTicks", operatingTicks);
		nbtTags.setTag("gasTank", gasTank.write(new NBTTagCompound()));
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
		return side == MekanismUtils.getRight(facing);
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
	public TileComponentUpgrade getComponent() 
	{
		return upgradeComponent;
	}

	@Override
	public void writeSustainedData(ItemStack itemStack) 
	{
		if(gasTank.getGas() != null)
		{
			itemStack.stackTagCompound.setTag("gasTank", gasTank.getGas().write(new NBTTagCompound()));
		}
	}

	@Override
	public void readSustainedData(ItemStack itemStack) 
	{
		gasTank.setGas(GasStack.readFromNBT(itemStack.stackTagCompound.getCompoundTag("gasTank")));
	}

	@Override
	public void recalculateUpgradables(Upgrade upgrade)
	{
		super.recalculateUpgradables(upgrade);

		switch(upgrade)
		{
			case SPEED:
				ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
			case ENERGY:
				energyUsage = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_USAGE);
				maxEnergy = MekanismUtils.getMaxEnergy(this, BASE_MAX_ENERGY);
			default:
				break;
		}
	}
	
	@Override
	public Object[] getTanks() 
	{
		return new Object[] {gasTank};
	}
}
