package mekanism.common.tile;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Range4D;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.SideData;
import mekanism.common.Upgrade;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
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
import cpw.mods.fml.common.Optional.Method;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;

public abstract class TileEntityElectricMachine<RECIPE extends BasicMachineRecipe<RECIPE>> extends TileEntityBasicMachine<ItemStackInput, ItemStackOutput, RECIPE>
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
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[] {1}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[] {2}));

		configComponent.setConfig(TransmissionType.ITEM, new byte[] {2, 1, 0, 0, 0, 3});
		configComponent.setInputEnergyConfig();

		inventory = new ItemStack[4];

		upgradeComponent = new TileComponentUpgrade(this, 3);
		ejectorComponent = new TileComponentEjector(this);
		ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(3));
	}
	
	public void upgrade(RecipeType type)
	{
		worldObj.setBlockToAir(xCoord, yCoord, zCoord);
		worldObj.setBlock(xCoord, yCoord, zCoord, MekanismBlocks.MachineBlock, 5, 3);
		
		TileEntityFactory factory = (TileEntityFactory)worldObj.getTileEntity(xCoord, yCoord, zCoord);
		
		//Basic
		factory.facing = facing;
		factory.clientFacing = clientFacing;
		factory.ticker = ticker;
		factory.redstone = redstone;
		factory.redstoneLastTick = redstoneLastTick;
		factory.doAutoSync = doAutoSync;
		
		//Electric
		factory.electricityStored = electricityStored;
		factory.ic2Registered = ic2Registered;
		
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
		factory.ejectorComponent.setOutputData(TransmissionType.ITEM, factory.configComponent.getOutputs(TransmissionType.ITEM).get(4));
		factory.recipeType = type;
		factory.upgradeComponent.setSupported(Upgrade.GAS, type.fuelEnergyUpgrades());

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
		Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(factory), factory.getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(factory)));
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

	@Override
	@Method(modid = "ComputerCraft")
	public String[] getMethodNames()
	{
		return new String[] {"getStored", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded"};
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
				Mekanism.logger.error("Attempted to call unknown method with computer ID " + computer.getID());
				return new Object[] {"Unknown command."};
		}
	}
}
