package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ITankManager;
import mekanism.common.base.TileNetworkList;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.OxidationRecipe;
import mekanism.common.tile.prefab.TileEntityOperationalMachine;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.GasUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.ListUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityChemicalOxidizer extends TileEntityOperationalMachine implements ITubeConnection, ISustainedData, ITankManager
{
	public GasTank gasTank = new GasTank(MAX_GAS);

	public static final int MAX_GAS = 10000;

	public int gasOutput = 256;

	public OxidationRecipe cachedRecipe;

	public TileEntityChemicalOxidizer()
	{
		super("machine.oxidizer", "ChemicalOxidizer", BlockStateMachine.MachineType.CHEMICAL_OXIDIZER.baseEnergy, MekanismConfig.current().usage.rotaryCondensentratorUsage.val(), 3, 100);
		
		inventory = NonNullList.withSize(4, ItemStack.EMPTY);
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!world.isRemote)
		{
			ChargeUtils.discharge(1, this);

			if(!inventory.get(2).isEmpty() && gasTank.getGas() != null)
			{
				gasTank.draw(GasUtils.addGas(inventory.get(2), gasTank.getGas()), true);
			}
			
			OxidationRecipe recipe = getRecipe();

			if(canOperate(recipe) && getEnergy() >= energyPerTick && MekanismUtils.canFunction(this))
			{
				setActive(true);
				setEnergy(getEnergy() - energyPerTick);

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
				gasTank.draw(GasUtils.emit(toSend, this, ListUtils.asList(MekanismUtils.getRight(facing))), true);
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
	public boolean canExtractItem(int slotID, ItemStack itemstack, EnumFacing side)
	{
		if(slotID == 2)
		{
			return !itemstack.isEmpty() && itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).canProvideGas(itemstack, null);
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
		else if(side.getAxis() == Axis.Y)
		{
			return new int[] {1};
		}
		else if(side == MekanismUtils.getRight(facing))
		{
			return new int[] {2};
		}

		return InventoryUtils.EMPTY;
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
		return new ItemStackInput(inventory.get(0));
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

		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			if(dataStream.readBoolean())
			{
				gasTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
			}
			else {
				gasTank.setGas(null);
			}
		}
	}

	@Override
	public TileNetworkList getNetworkedData(TileNetworkList data)
	{
		super.getNetworkedData(data);

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

		gasTank.read(nbtTags.getCompoundTag("gasTank"));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setTag("gasTank", gasTank.write(new NBTTagCompound()));
		
		return nbtTags;
	}

	@Override
	public boolean canSetFacing(int i)
	{
		return i != 0 && i != 1;
	}

	@Override
	public boolean canTubeConnect(EnumFacing side)
	{
		return side == MekanismUtils.getRight(facing);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing side)
	{
		return capability == Capabilities.TUBE_CONNECTION_CAPABILITY || super.hasCapability(capability, side);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing side)
	{
		if(capability == Capabilities.TUBE_CONNECTION_CAPABILITY)
		{
			return (T)this;
		}
		
		return super.getCapability(capability, side);
	}

	@Override
	public void writeSustainedData(ItemStack itemStack) 
	{
		if(gasTank.getGas() != null)
		{
			ItemDataUtils.setCompound(itemStack, "gasTank", gasTank.getGas().write(new NBTTagCompound()));
		}
	}

	@Override
	public void readSustainedData(ItemStack itemStack) 
	{
		gasTank.setGas(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "gasTank")));
	}
	
	@Override
	public Object[] getTanks() 
	{
		return new Object[] {gasTank};
	}
}
