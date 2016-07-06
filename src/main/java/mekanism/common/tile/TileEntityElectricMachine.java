package mekanism.common.tile;

import mekanism.api.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.SideData;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Upgrade;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.BasicMachineRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.item.ItemStack;

public abstract class TileEntityElectricMachine<RECIPE extends BasicMachineRecipe<RECIPE>> extends TileEntityBasicMachine<ItemStackInput, ItemStackOutput, RECIPE> implements ITierUpgradeable
{
	/**
	 * A simple electrical machine. This has 3 slots - the input slot (0), the energy slot (1),
	 * output slot (2), and the upgrade slot (3). It will not run if it does not have enough energy.
	 *
	 * @param soundPath - location of the sound effect
	 * @param name - full name of this machine
	 * @param perTick - energy used per tick.
	 * @param ticksRequired - ticks required to operate -- or smelt an item.
	 * @param maxEnergy - maximum energy this machine can hold.
	 */
	public TileEntityElectricMachine(String soundPath, String name, double perTick, int ticksRequired, double maxEnergy)
	{
		super(soundPath, name, MekanismUtils.getResource(ResourceType.GUI, "GuiBasicMachine.png"), perTick, ticksRequired, maxEnergy);

		configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);
		
		configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED, new int[] {0}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[] {2}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[] {1}));

		configComponent.setConfig(TransmissionType.ITEM, new byte[] {3, 1, 0, 0, 0, 2});
		configComponent.setInputConfig(TransmissionType.ENERGY);

		inventory = new ItemStack[4];

		upgradeComponent = new TileComponentUpgrade(this, 3);
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
		factory.clientActive = clientActive;
		factory.isActive = isActive;
		factory.updateDelay = updateDelay;
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

		factory.inventory[5] = inventory[0];
		factory.inventory[1] = inventory[1];
		factory.inventory[5+3] = inventory[2];
		factory.inventory[0] = inventory[3];
		
		for(Upgrade upgrade : factory.upgradeComponent.getSupportedTypes())
		{
			factory.recalculateUpgradables(upgrade);
		}
		
		factory.upgraded = true;
		factory.markDirty();
		
		return true;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(!worldObj.isRemote)
		{
			ChargeUtils.discharge(1, this);

			RECIPE recipe = getRecipe();

			if(canOperate(recipe) && MekanismUtils.canFunction(this) && getEnergy() >= energyPerTick)
			{
				setActive(true);
				electricityStored -= energyPerTick;

				if((operatingTicks+1) < ticksRequired)
				{
					operatingTicks++;
				}
				else if((operatingTicks+1) >= ticksRequired)
				{
					operate(recipe);

					operatingTicks = 0;
				}
			}
			else {
				if(prevEnergy >= getEnergy())
				{
					setActive(false);
				}
			}

			if(!canOperate(recipe))
			{
				operatingTicks = 0;
			}

			prevEnergy = getEnergy();
		}
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 2)
		{
			return false;
		}
		else if(slotID == 3)
		{
			return itemstack.getItem() == MekanismItems.SpeedUpgrade || itemstack.getItem() == MekanismItems.EnergyUpgrade;
		}
		else if(slotID == 0)
		{
			return RecipeHandler.isInRecipe(itemstack, getRecipes());
		}
		else if(slotID == 1)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}

		return false;
	}

	@Override
	public ItemStackInput getInput()
	{
		return new ItemStackInput(inventory[0]);
	}

	@Override
	public RECIPE getRecipe()
	{
		ItemStackInput input = getInput();
		
		if(cachedRecipe == null || !input.testEquality(cachedRecipe.getInput()))
		{
			cachedRecipe = RecipeHandler.getRecipe(input, getRecipes());
		}
		
		return cachedRecipe;
	}

	@Override
	public void operate(RECIPE recipe)
	{
		recipe.operate(inventory, 0, 2);

		markDirty();
		ejectorComponent.outputItems();
	}

	@Override
	public boolean canOperate(RECIPE recipe)
	{
		return recipe != null && recipe.canOperate(inventory, 0, 2);
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 1)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
		}
		else if(slotID == 2)
		{
			return true;
		}

		return false;
	}

	private static final String[] methods = new String[] {"getEnergy", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded"};

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
				return new Object[] {operatingTicks};
			case 2:
				return new Object[] {isActive};
			case 3:
				return new Object[] {facing};
			case 4:
				return new Object[] {canOperate(getRecipe())};
			case 5:
				return new Object[] {getMaxEnergy()};
			case 6:
				return new Object[] {getMaxEnergy()-getEnergy()};
			default:
				throw new NoSuchMethodException();
		}
	}
}
