package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
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
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityChemicalOxidizer extends TileEntityElectricBlock implements IActiveState, ITubeConnection, IRedstoneControl, IHasSound
{
	public GasTank gasTank = new GasTank(MAX_GAS);

	public static final int MAX_GAS = 10000;

	public int updateDelay;

	public int gasOutput = 16;

	public boolean isActive;

	public boolean clientActive;

	public double prevEnergy;

	public int operatingTicks = 0;

	public int TICKS_REQUIRED = 100;

	public final double ENERGY_USAGE = Mekanism.rotaryCondensentratorUsage;

	public RedstoneControl controlType = RedstoneControl.DISABLED;

	public TileEntityChemicalOxidizer()
	{
		super("ChemicalOxidizer", MachineType.CHEMICAL_OXIDIZER.baseEnergy);
		inventory = new ItemStack[3];
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
					Mekanism.packetHandler.sendToAll(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())));
				}
			}

			ChargeUtils.discharge(1, this);

			if(inventory[2] != null && gasTank.getGas() != null)
			{
				gasTank.draw(GasTransmission.addGas(inventory[2], gasTank.getGas()), true);
			}

			if(canOperate() && getEnergy() >= ENERGY_USAGE && MekanismUtils.canFunction(this))
			{
				setActive(true);
				setEnergy(getEnergy() - ENERGY_USAGE);

				if(operatingTicks < TICKS_REQUIRED)
				{
					operatingTicks++;
				}
				else {
					GasStack stack = RecipeHandler.getItemToGasOutput(inventory[0], true, Recipe.CHEMICAL_OXIDIZER.get());

					gasTank.receive(stack, true);
					operatingTicks = 0;

					if(inventory[0].stackSize <= 0)
					{
						inventory[0] = null;
					}

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
						gasTank.draw(((IGasHandler)tileEntity).receiveGas(MekanismUtils.getRight(facing).getOpposite(), toSend), true);
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
			return RecipeHandler.getItemToGasOutput(itemstack, false, Recipe.CHEMICAL_OXIDIZER.get()) != null;
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

	public int getScaledProgress(int i)
	{
		return operatingTicks*i / TICKS_REQUIRED;
	}

	public double getScaledProgress()
	{
		return ((double)operatingTicks) / ((double)TICKS_REQUIRED);
	}

	public boolean canOperate()
	{
		if(inventory[0] == null)
		{
			return false;
		}

		GasStack stack = RecipeHandler.getItemToGasOutput(inventory[0], false, Recipe.CHEMICAL_OXIDIZER.get());

		if(stack == null || (gasTank.getGas() != null && (gasTank.getGas().getGas() != stack.getGas() || gasTank.getNeeded() < stack.amount)))
		{
			return false;
		}

		return true;
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

	public int getScaledGasLevel(int i)
	{
		return gasTank.getGas() != null ? gasTank.getStored()*i / MAX_GAS : 0;
	}

	@Override
	public void setActive(boolean active)
	{
		isActive = active;

		if(clientActive != active && updateDelay == 0)
		{
			Mekanism.packetHandler.sendToAll(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())));

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
	public String getSoundPath()
	{
		return "ChemicalInfuser.ogg";
	}

	@Override
	public float getVolumeMultiplier()
	{
		return 1;
	}
}
