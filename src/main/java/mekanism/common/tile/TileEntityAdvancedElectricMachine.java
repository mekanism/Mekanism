package mekanism.common.tile;

import java.util.ArrayList;

import mekanism.api.EnumColor;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.Mekanism;
import mekanism.common.MekanismItems;
import mekanism.common.SideData;
import mekanism.common.Upgrade;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional.Method;

import io.netty.buffer.ByteBuf;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;

public abstract class TileEntityAdvancedElectricMachine<RECIPE extends AdvancedMachineRecipe<RECIPE>> extends TileEntityBasicMachine<AdvancedMachineInput, ItemStackOutput, RECIPE> implements IGasHandler, ITubeConnection
{
	/** How much secondary energy (fuel) this machine uses per tick, not including upgrades. */
	public int BASE_SECONDARY_ENERGY_PER_TICK;

	/** How much secondary energy this machine uses per tick, including upgrades. */
	public int secondaryEnergyPerTick;

	public static int MAX_GAS = 200;

	public GasTank gasTank;

	/**
	 * Advanced Electric Machine -- a machine like this has a total of 4 slots. Input slot (0), fuel slot (1), output slot (2),
	 * energy slot (3), and the upgrade slot (4). The machine will not run if it does not have enough electricity, or if it doesn't have enough
	 * fuel ticks.
	 *
	 * @param soundPath - location of the sound effect
	 * @param name - full name of this machine
	 * @param perTick - how much energy this machine uses per tick.
	 * @param secondaryPerTick - how much secondary energy (fuel) this machine uses per tick.
	 * @param ticksRequired - how many ticks it takes to smelt an item.
	 * @param maxEnergy - maximum amount of energy this machine can hold.
	 */
	public TileEntityAdvancedElectricMachine(String soundPath, String name, double perTick, int secondaryPerTick, int ticksRequired, double maxEnergy)
	{
		super(soundPath, name, MekanismUtils.getResource(ResourceType.GUI, "GuiAdvancedMachine.png"), perTick, ticksRequired, maxEnergy);

		sideOutputs.add(new SideData(EnumColor.GREY, InventoryUtils.EMPTY));
		sideOutputs.add(new SideData(EnumColor.DARK_RED, new int[] {0}));
		sideOutputs.add(new SideData(EnumColor.PURPLE, new int[] {1}));
		sideOutputs.add(new SideData(EnumColor.DARK_BLUE, new int[] {2}));
		sideOutputs.add(new SideData(EnumColor.DARK_GREEN, new int[] {3}));
		sideOutputs.add(new SideData(EnumColor.ORANGE, new int[] {4}));

		sideConfig = new byte[] {2, 1, 0, 4, 5, 3};

		gasTank = new GasTank(MAX_GAS);

		inventory = new ItemStack[5];

		BASE_SECONDARY_ENERGY_PER_TICK = secondaryPerTick;
		secondaryEnergyPerTick = secondaryPerTick;

		upgradeComponent = new TileComponentUpgrade(this, 4);
		ejectorComponent = new TileComponentEjector(this, sideOutputs.get(3));
	}

	/**
	 * Gets the amount of ticks the declared itemstack can fuel this machine.
	 * @param itemstack - itemstack to check with
	 * @return fuel ticks
	 */
	public abstract GasStack getItemGas(ItemStack itemstack);

	public abstract boolean isValidGas(Gas gas);

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(!worldObj.isRemote)
		{
			ChargeUtils.discharge(3, this);

			handleSecondaryFuel();

			boolean changed = false;

			RECIPE recipe = getRecipe();

			if(canOperate(recipe) && MekanismUtils.canFunction(this) && getEnergy() >= energyPerTick && gasTank.getStored() >= secondaryEnergyPerTick)
			{
				setActive(true);

				operatingTicks++;

				if(operatingTicks >= ticksRequired)
				{
					operate(recipe);

					operatingTicks = 0;
				}

				gasTank.draw(secondaryEnergyPerTick, true);
				electricityStored -= energyPerTick;
			}
			else {
				if(prevEnergy >= getEnergy())
				{
					changed = true;
					setActive(false);
				}
			}

			if(changed && !canOperate(recipe) && getRecipe() == null)
			{
				operatingTicks = 0;
			}

			prevEnergy = getEnergy();
		}
	}

	public void handleSecondaryFuel()
	{
		if(inventory[1] != null && gasTank.getNeeded() > 0)
		{
			GasStack stack = getItemGas(inventory[1]);
			int gasNeeded = gasTank.getNeeded();

			if(stack != null && gasTank.canReceive(stack.getGas()) && gasNeeded >= stack.amount)
			{
				gasTank.receive(stack, true);

				inventory[1].stackSize--;

				if(inventory[1].stackSize == 0)
				{
					inventory[1] = null;
				}
			}
		}
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 2)
		{
			return false;
		}
		else if(slotID == 4)
		{
			return itemstack.getItem() == MekanismItems.SpeedUpgrade || itemstack.getItem() == MekanismItems.EnergyUpgrade;
		}
		else if(slotID == 0)
		{
			return RecipeHandler.getRecipe(new AdvancedMachineInput(itemstack, gasTank.getGasType()), getRecipes()) != null;
		}
		else if(slotID == 3)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}
		else if(slotID == 1)
		{
			return getItemGas(itemstack) != null;
		}

		return false;
	}

	@Override
	public AdvancedMachineInput getInput()
	{
		return new AdvancedMachineInput(inventory[0], gasTank.getGasType());
	}

	@Override
	public RECIPE getRecipe()
	{
		AdvancedMachineInput input = getInput();
		if(cachedRecipe == null || !input.testEquality(cachedRecipe.getInput()))
		{
			cachedRecipe = RecipeHandler.getRecipe(input, getRecipes());
		}
		return cachedRecipe;
	}

	@Override
	public void operate(RECIPE recipe)
	{
		recipe.operate(inventory, 0, 2, gasTank, secondaryEnergyPerTick);

		markDirty();
		ejectorComponent.onOutput();
	}

	@Override
	public boolean canOperate(RECIPE recipe)
	{
		return recipe != null && recipe.canOperate(inventory, 0, 2, gasTank, secondaryEnergyPerTick);
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);

		if(dataStream.readBoolean())
		{
			gasTank.setGas(new GasStack(dataStream.readInt(), dataStream.readInt()));
		}
		else {
			gasTank.setGas(null);
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
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
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setTag("gasTank", gasTank.write(new NBTTagCompound()));
	}

	/**
	 * Gets the scaled secondary energy level for the GUI.
	 * @param i - multiplier
	 * @return scaled secondary energy
	 */
	public int getScaledGasLevel(int i)
	{
		return gasTank.getStored()*i / gasTank.getMaxGas();
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 3)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
		}
		else if(slotID == 2)
		{
			return true;
		}

		return false;
	}

	@Override
	public boolean canTubeConnect(EnumFacing side)
	{
		return false;
	}

	@Override
	public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer)
	{
		return 0;
	}

	@Override
	public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer)
	{
		return null;
	}

	@Override
	public boolean canReceiveGas(EnumFacing side, Gas type)
	{
		return false;
	}

	@Override
	public boolean canDrawGas(EnumFacing side, Gas type)
	{
		return false;
	}

	@Override
	public void recalculateUpgradables(Upgrade upgrade)
	{
		super.recalculateUpgradables(upgrade);

		if(upgrade == Upgrade.SPEED)
		{
			secondaryEnergyPerTick = MekanismUtils.getSecondaryEnergyPerTick(this, BASE_SECONDARY_ENERGY_PER_TICK);
		}
	}

	@Override
	@Method(modid = "ComputerCraft")
	public String[] getMethodNames()
	{
		return new String[] {"getStored", "getSecondaryStored", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded"};
	}

	@Override
	@Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException
	{
		switch(method)
		{
			case 0:
				return new Object[] {getEnergy()};
			case 1:
				return new Object[] {gasTank.getStored()};
			case 2:
				return new Object[] {operatingTicks};
			case 3:
				return new Object[] {isActive};
			case 4:
				return new Object[] {facing};
			case 5:
				return new Object[] {canOperate(RecipeHandler.getRecipe(getInput(), getRecipes()))};
			case 6:
				return new Object[] {maxEnergy};
			case 7:
				return new Object[] {maxEnergy-getEnergy()};
			default:
				Mekanism.logger.error("Attempted to call unknown method with computer ID " + computer.getID());
				return new Object[] {"Unknown command."};
		}
	}
}
