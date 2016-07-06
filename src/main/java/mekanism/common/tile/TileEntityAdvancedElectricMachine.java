package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.EnumColor;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.SideData;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Upgrade;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mekanism.common.tile.component.TileComponentAdvancedUpgrade;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.StatUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class TileEntityAdvancedElectricMachine<RECIPE extends AdvancedMachineRecipe<RECIPE>> extends TileEntityBasicMachine<AdvancedMachineInput, ItemStackOutput, RECIPE> implements IGasHandler, ITubeConnection, ITierUpgradeable
{
	/** How much secondary energy (fuel) this machine uses per tick, not including upgrades. */
	public int BASE_SECONDARY_ENERGY_PER_TICK;

	/** How much secondary energy this machine uses per tick, including upgrades. */
	public double secondaryEnergyPerTick;

	public int secondaryEnergyThisTick;

	public static int MAX_GAS = 210;

	public GasTank gasTank;
	public Gas prevGas;

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

		configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);
		
		configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED, new int[] {0}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[] {2}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[] {3}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Extra", EnumColor.PURPLE, new int[] {1}));

		configComponent.setConfig(TransmissionType.ITEM, new byte[] {4, 1, 0, 3, 0, 2});
		configComponent.setInputConfig(TransmissionType.ENERGY);

		gasTank = new GasTank(MAX_GAS);

		inventory = new ItemStack[5];

		BASE_SECONDARY_ENERGY_PER_TICK = secondaryPerTick;
		secondaryEnergyPerTick = secondaryPerTick;

		upgradeComponent = upgradeableSecondaryEfficiency() ? new TileComponentAdvancedUpgrade(this, 4) : new TileComponentUpgrade(this, 4);
		upgradeComponent.setSupported(Upgrade.MUFFLING);
		
		ejectorComponent = new TileComponentEjector(this);
		ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(2));
	}
	
	@Override
	public boolean upgrade(BaseTier upgradeTier)
	{
		if(upgradeTier != BaseTier.BASIC)
		{
			return false;
		}
		
		worldObj.setBlockToAir(xCoord, yCoord, zCoord);
		worldObj.setBlock(xCoord, yCoord, zCoord, MekanismBlocks.MachineBlock, 5, 3);
		
		TileEntityFactory factory = (TileEntityFactory)worldObj.getTileEntity(xCoord, yCoord, zCoord);
		RecipeType type = RecipeType.getFromMachine(getBlockType(), getBlockMetadata());
		
		//Basic
		factory.facing = facing;
		factory.clientFacing = clientFacing;
		factory.ticker = ticker;
		factory.redstone = redstone;
		factory.redstoneLastTick = redstoneLastTick;
		factory.doAutoSync = doAutoSync;
		
		//Electric
		factory.electricityStored = electricityStored;

		//Noisy
		factory.soundURL = soundURL;
		
		//Machine
		factory.progress[0] = operatingTicks;
		factory.updateDelay = updateDelay;
		factory.isActive = isActive;
		factory.clientActive = clientActive;
		factory.controlType = controlType;
		factory.prevEnergy = prevEnergy;
		factory.upgradeComponent.readFrom(upgradeComponent);
		factory.upgradeComponent.setUpgradeSlot(0);
		factory.ejectorComponent.readFrom(ejectorComponent);
		factory.ejectorComponent.setOutputData(TransmissionType.ITEM, factory.configComponent.getOutputs(TransmissionType.ITEM).get(2));
		factory.recipeType = type;
		factory.upgradeComponent.setSupported(Upgrade.GAS, type.fuelEnergyUpgrades());
		factory.securityComponent.readFrom(securityComponent);
		
		for(TransmissionType transmission : configComponent.transmissions)
		{
			factory.configComponent.setConfig(transmission, configComponent.getConfig(transmission));
			factory.configComponent.setEjecting(transmission, configComponent.isEjecting(transmission));
		}
		
		//Advanced Machine
		factory.gasTank.setGas(gasTank.getGas());
		
		factory.inventory[5] = inventory[0];
		factory.inventory[4] = inventory[1];
		factory.inventory[5+3] = inventory[2];
		factory.inventory[1] = inventory[3];
		factory.inventory[0] = inventory[4];
		
		for(Upgrade upgrade : factory.upgradeComponent.getSupportedTypes())
		{
			factory.recalculateUpgradables(upgrade);
		}
		
		factory.upgraded = true;
		factory.markDirty();
		
		return true;
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

			boolean inactive = false;

			RECIPE recipe = getRecipe();

			secondaryEnergyThisTick = useStatisticalMechanics() ? StatUtils.inversePoisson(secondaryEnergyPerTick) : (int)Math.ceil(secondaryEnergyPerTick);

			if(canOperate(recipe) && MekanismUtils.canFunction(this) && getEnergy() >= energyPerTick && gasTank.getStored() >= secondaryEnergyThisTick)
			{
				setActive(true);

				operatingTicks++;

				if(operatingTicks >= ticksRequired)
				{
					operate(recipe);

					operatingTicks = 0;
				}

				gasTank.draw(secondaryEnergyThisTick, true);
				electricityStored -= energyPerTick;
			}
			else {
				inactive = true;
				setActive(false);
			}

			if(inactive && getRecipe() == null)
			{
				operatingTicks = 0;
			}

			prevEnergy = getEnergy();
			
			if(!(gasTank.getGasType() == null || gasTank.getStored() == 0))
			{
				prevGas = gasTank.getGasType();
			}
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
	
	public boolean upgradeableSecondaryEfficiency()
	{
		return false;
	}

	public boolean useStatisticalMechanics()
	{
		return false;
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
			for(AdvancedMachineInput input : getRecipes().keySet())
			{
				if(input.itemStack.isItemEqual(itemstack))
				{
					return true;
				}
			}
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
		return new AdvancedMachineInput(inventory[0], prevGas);
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
		recipe.operate(inventory, 0, 2, gasTank, secondaryEnergyThisTick);

		markDirty();
		ejectorComponent.outputItems();
	}

	@Override
	public boolean canOperate(RECIPE recipe)
	{
		return recipe != null && recipe.canOperate(inventory, 0, 2, gasTank, secondaryEnergyThisTick);
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);

		if(worldObj.isRemote)
		{
			if(dataStream.readBoolean())
			{
				gasTank.setGas(new GasStack(dataStream.readInt(), dataStream.readInt()));
			}
			else {
				gasTank.setGas(null);
			}
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
		gasTank.setMaxGas(MAX_GAS);
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
	public boolean canTubeConnect(ForgeDirection side)
	{
		return false;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack, boolean doTransfer)
	{
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
		return false;
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return false;
	}

	@Override
	public void recalculateUpgradables(Upgrade upgrade)
	{
		super.recalculateUpgradables(upgrade);

		if(upgrade == Upgrade.SPEED || (upgradeableSecondaryEfficiency() && upgrade == Upgrade.GAS))
		{
			secondaryEnergyPerTick = MekanismUtils.getSecondaryEnergyPerTickMean(this, BASE_SECONDARY_ENERGY_PER_TICK);
		}
	}

	private static final String[] methods = new String[] {"getEnergy", "getSecondaryStored", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded"};

	@Override
	public String[] getMethods()
	{
		return methods;
	}

	@Override
	public Object[] invoke(int method, Object[] arguments) throws Exception
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
				throw new NoSuchMethodException();
		}
	}
}
