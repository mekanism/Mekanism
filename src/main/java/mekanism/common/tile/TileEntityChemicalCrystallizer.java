package mekanism.common.tile;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
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
import mekanism.common.IEjector;
import mekanism.common.IInvConfiguration;
import mekanism.common.IRedstoneControl;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.SideData;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityChemicalCrystallizer extends TileEntityElectricBlock implements IActiveState, IGasHandler, ITubeConnection, IRedstoneControl, IHasSound, IInvConfiguration
{
	public static final int MAX_GAS = 10000;
	public static final int MAX_FLUID = 10000;

	public byte[] sideConfig = new byte[] {0, 3, 0, 0, 1, 2};

	public ArrayList<SideData> sideOutputs = new ArrayList<SideData>();

	public GasTank inputTank = new GasTank(MAX_GAS);

	public static int WATER_USAGE = 5;

	public int updateDelay;

	public int gasOutput = 16;

	public int operatingTicks;

	public int TICKS_REQUIRED = 200;

	public boolean isActive;

	public boolean clientActive;

	public double prevEnergy;

	public float spinSpeed;

	public float spin;

	public final double ENERGY_USAGE = Mekanism.chemicalCrystallizerUsage;

	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType = RedstoneControl.DISABLED;

	public TileComponentEjector ejectorComponent;

	public TileEntityChemicalCrystallizer()
	{
		super("ChemicalCrystallizer", MachineType.CHEMICAL_CRYSTALLIZER.baseEnergy);

		sideOutputs.add(new SideData(EnumColor.GREY, InventoryUtils.EMPTY));
		sideOutputs.add(new SideData(EnumColor.PURPLE, new int[] {0}));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, new int[] {1}));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, new int[] {2}));

		inventory = new ItemStack[3];
		ejectorComponent = new TileComponentEjector(this, sideOutputs.get(2));
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

			ChargeUtils.discharge(2, this);

			if(inventory[0] != null && (inputTank.getGas() == null || inputTank.getStored() < inputTank.getMaxGas()))
			{
				inputTank.receive(GasTransmission.removeGas(inventory[0], null, inputTank.getNeeded()), true);
			}

			if(canOperate() && MekanismUtils.canFunction(this) && getEnergy() >= ENERGY_USAGE)
			{
				setActive(true);

				if((operatingTicks+1) < TICKS_REQUIRED)
				{
					operatingTicks++;
					setEnergy(getEnergy() - ENERGY_USAGE);
				}
				else if((operatingTicks+1) >= TICKS_REQUIRED)
				{
					operate();

					operatingTicks = 0;
					setEnergy(getEnergy() - ENERGY_USAGE);
				}
			}
			else {
				if(prevEnergy >= getEnergy())
				{
					setActive(false);
				}
			}

			if(!canOperate())
			{
				operatingTicks = 0;
			}

			prevEnergy = getEnergy();
		}
	}

	public boolean canOperate()
	{
		if(inputTank.getGas() == null)
		{
			return false;
		}

		ItemStack itemstack = RecipeHandler.getChemicalCrystallizerOutput(inputTank, false);

		if(itemstack == null)
		{
			return false;
		}

		if(inventory[1] == null)
		{
			return true;
		}

		if(!inventory[1].isItemEqual(itemstack))
		{
			return false;
		}
		else {
			return inventory[1].stackSize + itemstack.stackSize <= inventory[1].getMaxStackSize();
		}
	}

	public void operate()
	{
		ItemStack itemstack = RecipeHandler.getChemicalCrystallizerOutput(inputTank, true);

		if(inventory[1] == null)
		{
			inventory[1] = itemstack;
		}
		else {
			inventory[1].stackSize += itemstack.stackSize;
		}

		markDirty();
		ejectorComponent.onOutput();
	}

	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
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
				Mekanism.packetPipeline.sendTo(new PacketTileEntity(Coord4D.get(this), getNetworkedData(new ArrayList())), player);
			}

			return;
		}

		super.handlePacketData(dataStream);

		isActive = dataStream.readBoolean();
		operatingTicks = dataStream.readInt();

		for(int i = 0; i < 6; i++)
		{
			sideConfig[i] = dataStream.readByte();
		}

		controlType = RedstoneControl.values()[dataStream.readInt()];

		if(dataStream.readBoolean())
		{
			inputTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			inputTank.setGas(null);
		}


		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(isActive);
		data.add(operatingTicks);
		data.add(sideConfig);
		data.add(controlType.ordinal());

		if(inputTank.getGas() != null)
		{
			data.add(true);
			data.add(inputTank.getGas().getGas().getID());
			data.add(inputTank.getStored());
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
		operatingTicks = nbtTags.getInteger("operatingTicks");
		controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];

		inputTank.read(nbtTags.getCompoundTag("rightTank"));

		if(nbtTags.hasKey("sideDataStored"))
		{
			for(int i = 0; i < 6; i++)
			{
				sideConfig[i] = nbtTags.getByte("config"+i);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setBoolean("isActive", isActive);
		nbtTags.setInteger("operatingTicks", operatingTicks);
		nbtTags.setInteger("controlType", controlType.ordinal());

		nbtTags.setTag("rightTank", inputTank.write(new NBTTagCompound()));

		nbtTags.setBoolean("sideDataStored", true);

		for(int i = 0; i < 6; i++)
		{
			nbtTags.setByte("config"+i, sideConfig[i]);
		}
	}

	@Override
	public boolean canSetFacing(int i)
	{
		return i != 0 && i != 1;
	}

	public int getScaledInputGasLevel(int i)
	{
		return inputTank != null ? inputTank.getStored()*i / MAX_GAS : 0;
	}

	public int getScaledProgress(int i)
	{
		return operatingTicks*i / TICKS_REQUIRED;
	}

	public double getScaledProgress()
	{
		return ((double)operatingTicks) / ((double)TICKS_REQUIRED);
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
		return side == MekanismUtils.getLeft(facing);
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return side == MekanismUtils.getLeft(facing) && inputTank.canReceive(type);
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
		if(canReceiveGas(side, stack.getGas()))
		{
			return inputTank.receive(stack, true);
		}

		return 0;
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount)
	{
		return null;
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return false;
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
		if(slotID == 0)
		{
			return itemstack != null && itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).canProvideGas(itemstack, null);
		}
		else if(slotID == 1)
		{
			return true;
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
		return "ChemicalCrystallizer.ogg";
	}

	@Override
	public float getVolumeMultiplier()
	{
		return 1;
	}

	@Override
	public ArrayList<SideData> getSideData()
	{
		return sideOutputs;
	}

	@Override
	public byte[] getConfiguration()
	{
		return sideConfig;
	}

	@Override
	public int getOrientation()
	{
		return facing;
	}

	@Override
	public IEjector getEjector()
	{
		return ejectorComponent;
	}
}
