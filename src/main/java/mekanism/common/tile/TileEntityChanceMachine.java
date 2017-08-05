package mekanism.common.tile;

import java.util.Map;

import mekanism.api.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.MekanismItems;
import mekanism.common.SideData;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.ChanceMachineRecipe;
import mekanism.common.recipe.outputs.ChanceOutput;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityBasicMachine;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public abstract class TileEntityChanceMachine<RECIPE extends ChanceMachineRecipe<RECIPE>> extends TileEntityBasicMachine<ItemStackInput, ChanceOutput, RECIPE>
{
	public TileEntityChanceMachine(String soundPath, String name, double maxEnergy, double baseEnergyUsage, int ticksRequired, ResourceLocation location)
	{
		super(soundPath, name, maxEnergy, baseEnergyUsage, 3, ticksRequired, location);

		configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);
		
		configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED, new int[] {0}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[] {1}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[] {2, 4}));

		configComponent.setConfig(TransmissionType.ITEM, new byte[] {2, 1, 0, 0, 0, 3});
		configComponent.setInputConfig(TransmissionType.ENERGY);

		inventory = NonNullList.withSize(5, ItemStack.EMPTY);
		
		ejectorComponent = new TileComponentEjector(this);
		ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(3));
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(!world.isRemote)
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
				else {
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
		if(slotID == 3)
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
		return new ItemStackInput(inventory.get(0));
	}

	@Override
	public void operate(RECIPE recipe)
	{
		recipe.operate(inventory);

		markDirty();
		ejectorComponent.outputItems();
	}

	@Override
	public boolean canOperate(RECIPE recipe)
	{
		return recipe != null && recipe.canOperate(inventory, 0, 2, 4);
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, EnumFacing side)
	{
		if(slotID == 1)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
		}
		else if(slotID == 2 || slotID == 4)
		{
			return true;
		}

		return false;
	}

	@Override
	public RECIPE getRecipe()
	{
		ItemStackInput input = getInput();
		
		if(cachedRecipe == null || !input.testEquality(cachedRecipe.getInput()))
		{
			cachedRecipe = RecipeHandler.getChanceRecipe(input, getRecipes());
		}
		
		return cachedRecipe;
	}

	@Override
	public Map<ItemStackInput, RECIPE> getRecipes()
	{
		return null;
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
