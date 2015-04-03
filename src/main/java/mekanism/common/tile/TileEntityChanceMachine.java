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
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.common.Optional.Method;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;

public abstract class TileEntityChanceMachine<RECIPE extends ChanceMachineRecipe<RECIPE>> extends TileEntityBasicMachine<ItemStackInput, ChanceOutput, RECIPE>
{
	public TileEntityChanceMachine(String soundPath, String name, ResourceLocation location, double perTick, int ticksRequired, double maxEnergy)
	{
		super(soundPath, name, location, perTick, ticksRequired, maxEnergy);

		configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);
		
		configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED, new int[] {0}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[] {1}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[] {2, 4}));

		configComponent.setConfig(TransmissionType.ITEM, new byte[] {2, 1, 0, 0, 0, 3});
		configComponent.setInputEnergyConfig();

		inventory = new ItemStack[5];

		upgradeComponent = new TileComponentUpgrade(this, 3);
		ejectorComponent = new TileComponentEjector(this);
		ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(3));
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
		return new ItemStackInput(inventory[0]);
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
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
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

	@Override
	@Method(modid = "ComputerCraft")
	public String[] getMethodNames()
	{
		return null;
	}

	@Override
	@Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException
	{
		return null;
	}
}
