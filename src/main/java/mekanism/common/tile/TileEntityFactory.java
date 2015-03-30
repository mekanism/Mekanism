package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.MekanismConfig.general;
import mekanism.api.MekanismConfig.usage;
import mekanism.api.Range4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.ITubeConnection;
import mekanism.api.transmitters.TransmissionType;
import mekanism.api.util.StackUtils;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.SideData;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.Upgrade;
import mekanism.common.base.IEjector;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.base.SoundWrapper;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.recipe.machines.BasicMachineRecipe;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StatUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

@Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")
public class TileEntityFactory extends TileEntityNoisyElectricBlock implements IPeripheral, ISideConfiguration, IUpgradeTile, IRedstoneControl, IGasHandler, ITubeConnection
{
	/** This Factory's tier. */
	public FactoryTier tier;

	/** An int[] used to track all current operations' progress. */
	public int[] progress;

	/** How many ticks it takes, by default, to run an operation. */
	public int BASE_TICKS_REQUIRED = 200;

	/** How many ticks it takes, with upgrades, to run an operation */
	public int ticksRequired = 200;

	/** How much energy each operation consumes per tick, without upgrades. */
	public double BASE_ENERGY_PER_TICK = usage.factoryUsage;

	/** How much energy each operation consumes per tick. */
	public double energyPerTick = usage.factoryUsage;

	/** How much secondary energy each operation consumes per tick */
	public double secondaryEnergyPerTick = 0;

	public int secondaryEnergyThisTick;

	/** How long it takes this factory to switch recipe types. */
	public int RECIPE_TICKS_REQUIRED = 40;

	/** How many recipe ticks have progressed. */
	public int recipeTicks;

	/** The client's current active state. */
	public boolean clientActive;

	/** This machine's active state. */
	public boolean isActive;

	/** How many ticks must pass until this block's active state can sync with the client. */
	public int updateDelay;

	/** This machine's recipe type. */
	public RecipeType recipeType = RecipeType.SMELTING;

	/** This machine's previous amount of energy. */
	public double prevEnergy;

	public GasTank gasTank;

	public boolean sorting;
	
	public boolean upgraded;

	@SideOnly(Side.CLIENT)
	public SoundWrapper[] sounds;

	/** This machine's current RedstoneControl type. */
	public RedstoneControl controlType = RedstoneControl.DISABLED;

	public TileComponentUpgrade upgradeComponent;
	public TileComponentEjector ejectorComponent;
	public TileComponentConfig configComponent;

	public TileEntityFactory()
	{
		this(FactoryTier.BASIC, MachineType.BASIC_FACTORY);
		
		configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.GAS);

		configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[] {1}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Extra", EnumColor.PURPLE, new int[] {4}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED, new int[] {5, 6, 7}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[] {8, 9, 10}));
		configComponent.setConfig(TransmissionType.ITEM, new byte[] {4, 3, 0, 2, 1, 0});
		
		configComponent.addOutput(TransmissionType.GAS, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
		configComponent.addOutput(TransmissionType.GAS, new SideData("Gas", EnumColor.DARK_RED, new int[] {0}));
		configComponent.fillConfig(TransmissionType.GAS, 1);
		configComponent.setCanEject(TransmissionType.GAS, false);
		
		configComponent.setInputEnergyConfig();

		upgradeComponent = new TileComponentUpgrade(this, 0);
		ejectorComponent = new TileComponentEjector(this);
		ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(4));
	}

	public TileEntityFactory(FactoryTier type, MachineType machine)
	{
		super("null", machine.name, machine.baseEnergy);

		tier = type;
		inventory = new ItemStack[5+type.processes*2];
		progress = new int[type.processes];
		isActive = false;

		gasTank = new GasTank(TileEntityAdvancedElectricMachine.MAX_GAS*tier.processes);
	}
	
	public void upgrade()
	{
		worldObj.setBlockToAir(xCoord, yCoord, zCoord);
		worldObj.setBlock(xCoord, yCoord, zCoord, MekanismBlocks.MachineBlock, 5+tier.ordinal()+1, 3);
		
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
		
		//Factory
		
		for(int i = 0; i < tier.processes; i++)
		{
			factory.progress[i] = progress[i];
		}
		
		factory.recipeTicks = recipeTicks;
		factory.clientActive = clientActive;
		factory.isActive = isActive;
		factory.updateDelay = updateDelay;
		factory.prevEnergy = prevEnergy;
		factory.gasTank.setGas(gasTank.getGas());
		factory.sorting = sorting;
		factory.controlType = controlType;
		factory.upgradeComponent.readFrom(upgradeComponent);
		factory.ejectorComponent.readFrom(ejectorComponent);
		factory.configComponent.readFrom(configComponent);
		factory.ejectorComponent.setOutputData(TransmissionType.ITEM, factory.configComponent.getOutputs(TransmissionType.ITEM).get(4));
		factory.recipeType = recipeType;
		factory.upgradeComponent.setSupported(Upgrade.GAS, recipeType.fuelEnergyUpgrades());
		
		for(int i = 0; i < tier.processes+5; i++)
		{
			factory.inventory[i] = inventory[i];
		}
		
		for(int i = 0; i < tier.processes; i++)
		{
			int output = getOutputSlot(i);
			
			if(inventory[output] != null)
			{
				int newOutput = 5+factory.tier.processes+i;
				
				factory.inventory[newOutput] = inventory[output];
			}
		}
		
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
			if(ticker == 1)
			{
				worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
			}

			if(updateDelay > 0)
			{
				updateDelay--;

				if(updateDelay == 0 && clientActive != isActive)
				{
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
				}
			}

			ChargeUtils.discharge(1, this);

			handleSecondaryFuel();
			sortInventory();

			if(inventory[2] != null && inventory[3] == null)
			{
				RecipeType toSet = null;

				for(RecipeType type : RecipeType.values())
				{
					if(inventory[2].isItemEqual(type.getStack()))
					{
						toSet = type;
						break;
					}
				}

				if(toSet != null && recipeType != toSet)
				{
					if(recipeTicks < RECIPE_TICKS_REQUIRED)
					{
						recipeTicks++;
					}
					else {
						recipeTicks = 0;
						
						ItemStack returnStack = getMachineStack();
						
						if(returnStack.stackTagCompound == null)
						{
							returnStack.setTagCompound(new NBTTagCompound());
						}
						
						upgradeComponent.write(returnStack.stackTagCompound);
						upgradeComponent.setSupported(Upgrade.GAS, toSet.fuelEnergyUpgrades());
						upgradeComponent.read(inventory[2].stackTagCompound);

						inventory[2] = null;
						inventory[3] = returnStack;

						recipeType = toSet;
						gasTank.setGas(null);

						secondaryEnergyPerTick = getSecondaryEnergyPerTick(recipeType);

						worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());

						MekanismUtils.saveChunk(this);
					}
				}
				else {
					recipeTicks = 0;
				}
			}
			else {
				recipeTicks = 0;
			}

			secondaryEnergyThisTick = recipeType.fuelEnergyUpgrades() ? StatUtils.inversePoisson(secondaryEnergyPerTick) : (int)Math.ceil(secondaryEnergyPerTick);
			
			for(int process = 0; process < tier.processes; process++)
			{
				if(MekanismUtils.canFunction(this) && canOperate(getInputSlot(process), getOutputSlot(process)) && getEnergy() >= energyPerTick && gasTank.getStored() >= secondaryEnergyThisTick)
				{
					if((progress[process]+1) < ticksRequired)
					{
						progress[process]++;
						gasTank.draw(secondaryEnergyThisTick, true);
						electricityStored -= energyPerTick;
					}
					else if((progress[process]+1) >= ticksRequired)
					{
						operate(getInputSlot(process), getOutputSlot(process));

						progress[process] = 0;
						gasTank.draw(secondaryEnergyThisTick, true);
						electricityStored -= energyPerTick;
					}
				}

				if(!canOperate(getInputSlot(process), getOutputSlot(process)))
				{
					if(!(recipeType.usesFuel() && recipeType.hasRecipe(inventory[getInputSlot(process)])))
					{
						progress[process] = 0;
					}
				}
			}

			boolean hasOperation = false;

			for(int i = 0; i < tier.processes; i++)
			{
				if(canOperate(getInputSlot(i), getOutputSlot(i)))
				{
					hasOperation = true;
					break;
				}
			}

			if(MekanismUtils.canFunction(this) && hasOperation && getEnergy() >= energyPerTick && gasTank.getStored() >= secondaryEnergyThisTick)
			{
				setActive(true);
			}
			else {
				if(prevEnergy >= getEnergy())
				{
					setActive(false);
				}
			}

			prevEnergy = getEnergy();
		}
	}
	
	@Override
	public EnumSet<ForgeDirection> getConsumingSides()
	{
		return configComponent.getSidesForData(TransmissionType.ENERGY, facing, 1);
	}

	public void sortInventory()
	{
		if(sorting)
		{
			boolean didOp = false;

			int[] inputSlots = null;

			List<InvID> invStacks = new ArrayList<InvID>();

			if(tier == FactoryTier.BASIC)
			{
				inputSlots = new int[] {5, 6, 7};
			}
			else if(tier == FactoryTier.ADVANCED)
			{
				inputSlots = new int[] {5, 6, 7, 8, 9};
			}
			else if(tier == FactoryTier.ELITE)
			{
				inputSlots = new int[] {5, 6, 7, 8, 9, 10, 11};
			}

			for(int id : inputSlots)
			{
				invStacks.add(InvID.get(id, inventory));
			}

			for(InvID invID1 : invStacks)
			{
				for(InvID invID2 : invStacks)
				{
					if(invID1.ID == invID2.ID || StackUtils.diffIgnoreNull(invID1.stack, invID2.stack) || Math.abs(invID1.size()-invID2.size()) < 2) continue;

					List<ItemStack> evened = StackUtils.even(inventory[invID1.ID], inventory[invID2.ID]);
					inventory[invID1.ID] = evened.get(0);
					inventory[invID2.ID] = evened.get(1);

					didOp = true;
					break;
				}

				if(didOp)
				{
					markDirty();
					break;
				}
			}
		}
	}

	public static class InvID
	{
		public ItemStack stack;
		public int ID;

		public InvID(ItemStack s, int i)
		{
			stack = s;
			ID = i;
		}

		public int size()
		{
			return stack != null ? stack.stackSize : 0;
		}

		public Item item()
		{
			return stack != null ? stack.getItem() : null;
		}

		public static InvID get(int id, ItemStack[] inv)
		{
			return new InvID(inv[id], id);
		}
	}

	public double getSecondaryEnergyPerTick(RecipeType type)
	{
		return MekanismUtils.getSecondaryEnergyPerTickMean(this, type.getSecondaryEnergyPerTick());
	}

	public void handleSecondaryFuel()
	{
		if(inventory[4] != null && recipeType.usesFuel() && gasTank.getNeeded() > 0)
		{
			if(inventory[4].getItem() instanceof IGasItem)
			{
				GasStack gas = ((IGasItem)inventory[4].getItem()).getGas(inventory[4]);

				if(gas != null && recipeType.isValidGas(gas.getGas()))
				{
					GasStack removed = GasTransmission.removeGas(inventory[4], gasTank.getGasType(), gasTank.getNeeded());
					gasTank.receive(removed, true);
				}

				return;
			}

			GasStack stack = recipeType.getItemGas(inventory[4]);
			int gasNeeded = gasTank.getNeeded();

			if(stack != null && stack.amount <= gasNeeded)
			{
				gasTank.receive(stack, true);

				inventory[4].stackSize--;

				if(inventory[4].stackSize == 0)
				{
					inventory[4] = null;
				}
			}
		}
	}

	public ItemStack getMachineStack()
	{
		return recipeType.getStack();
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 1)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
		}
		else if(tier == FactoryTier.BASIC && slotID >= 8 && slotID <= 10)
		{
			return true;
		}
		else if(tier == FactoryTier.ADVANCED && slotID >= 10 && slotID <= 14)
		{
			return true;
		}
		else if(tier == FactoryTier.ELITE && slotID >= 12 && slotID <= 18)
		{
			return true;
		}

		return false;
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(tier == FactoryTier.BASIC)
		{
			if(slotID >= 8 && slotID <= 10)
			{
				return false;
			}
			else if(slotID >= 5 && slotID <= 7)
			{
				return recipeType.getAnyRecipe(itemstack, gasTank.getGasType()) != null;
			}
		}
		else if(tier == FactoryTier.ADVANCED)
		{
			if(slotID >= 10 && slotID <= 14)
			{
				return false;
			}
			else if(slotID >= 5 && slotID <= 9)
			{
				return recipeType.getAnyRecipe(itemstack, gasTank.getGasType()) != null;
			}
		}
		else if(tier == FactoryTier.ELITE)
		{
			if(slotID >= 12 && slotID <= 18)
			{
				return false;
			}
			else if(slotID >= 5 && slotID <= 11)
			{
				return recipeType.getAnyRecipe(itemstack, gasTank.getGasType()) != null;
			}
		}

		if(slotID == 0)
		{
			return itemstack.getItem() == MekanismItems.SpeedUpgrade || itemstack.getItem() == MekanismItems.EnergyUpgrade;
		}
		else if(slotID == 1)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}
		else if(slotID == 4)
		{
			return recipeType.getItemGas(itemstack) != null;
		}

		return false;
	}

	public int getScaledProgress(int i, int process)
	{
		return progress[process]*i / ticksRequired;
	}

	public int getScaledGasLevel(int i)
	{
		return gasTank.getStored()*i / gasTank.getMaxGas();
	}

	public int getScaledRecipeProgress(int i)
	{
		return recipeTicks*i / RECIPE_TICKS_REQUIRED;
	}

	public boolean canOperate(int inputSlot, int outputSlot)
	{
		if(inventory[inputSlot] == null)
		{
			return false;
		}

		if(recipeType.usesFuel())
		{
			AdvancedMachineRecipe<?> recipe = recipeType.getRecipe(inventory[inputSlot], gasTank.getGasType());

			if(recipe == null)
			{
				return false;
			}

			return recipe.canOperate(inventory, inputSlot, outputSlot, gasTank, secondaryEnergyThisTick);
		}

		BasicMachineRecipe<?> recipe = recipeType.getRecipe(inventory[inputSlot]);

		if(recipe == null)
		{
			return false;
		}

		return recipe.canOperate(inventory, inputSlot, outputSlot);
	}

	public void operate(int inputSlot, int outputSlot)
	{
		if(!canOperate(inputSlot, outputSlot))
		{
			return;
		}

		if(recipeType.usesFuel())
		{
			AdvancedMachineRecipe<?> recipe = recipeType.getRecipe(inventory[inputSlot], gasTank.getGasType());

			recipe.operate(inventory, inputSlot, outputSlot, gasTank, secondaryEnergyThisTick);
		}
		else {
			BasicMachineRecipe<?> recipe = recipeType.getRecipe(inventory[inputSlot]);

			recipe.operate(inventory, inputSlot, outputSlot);
		}

		markDirty();
		ejectorComponent.outputItems();
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(!worldObj.isRemote)
		{
			int type = dataStream.readInt();

			if(type == 0)
			{
				sorting = !sorting;
			}

			return;
		}

		super.handlePacketData(dataStream);

		clientActive = dataStream.readBoolean();
		RecipeType oldRecipe = recipeType;
		recipeType = RecipeType.values()[dataStream.readInt()];
		upgradeComponent.setSupported(Upgrade.GAS, recipeType.fuelEnergyUpgrades());

		if(recipeType != oldRecipe)
		{
			secondaryEnergyPerTick = getSecondaryEnergyPerTick(recipeType);
		}
		
		recipeTicks = dataStream.readInt();
		controlType = RedstoneControl.values()[dataStream.readInt()];
		sorting = dataStream.readBoolean();
		upgraded = dataStream.readBoolean();

		for(int i = 0; i < tier.processes; i++)
		{
			progress[i] = dataStream.readInt();
		}

		if(dataStream.readBoolean())
		{
			gasTank.setGas(new GasStack(dataStream.readInt(), dataStream.readInt()));
		}
		else {
			gasTank.setGas(null);
		}

		if(updateDelay == 0 && clientActive != isActive)
		{
			updateDelay = general.UPDATE_DELAY;
			isActive = clientActive;
			MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
		}
		
		if(upgraded)
		{
			markDirty();
			MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
			upgraded = false;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		clientActive = isActive = nbtTags.getBoolean("isActive");
		RecipeType oldRecipe = recipeType;
		recipeType = RecipeType.values()[nbtTags.getInteger("recipeType")];
		upgradeComponent.setSupported(Upgrade.GAS, recipeType.fuelEnergyUpgrades());

		if(recipeType != oldRecipe)
		{
			secondaryEnergyPerTick = getSecondaryEnergyPerTick(recipeType);
		}

		recipeTicks = nbtTags.getInteger("recipeTicks");
		controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
		sorting = nbtTags.getBoolean("sorting");

		for(int i = 0; i < tier.processes; i++)
		{
			progress[i] = nbtTags.getInteger("progress" + i);
		}

		gasTank.read(nbtTags.getCompoundTag("gasTank"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setBoolean("isActive", isActive);
		nbtTags.setInteger("recipeType", recipeType.ordinal());
		nbtTags.setInteger("recipeTicks", recipeTicks);
		nbtTags.setInteger("controlType", controlType.ordinal());
		nbtTags.setBoolean("sorting", sorting);

		for(int i = 0; i < tier.processes; i++)
		{
			nbtTags.setInteger("progress" + i, progress[i]);
		}

		nbtTags.setTag("gasTank", gasTank.write(new NBTTagCompound()));
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(isActive);
		data.add(recipeType.ordinal());
		data.add(recipeTicks);
		data.add(controlType.ordinal());
		data.add(sorting);
		data.add(upgraded);
		data.add(progress);

		if(gasTank.getGas() != null)
		{
			data.add(true);
			data.add(gasTank.getGas().getGas().getID());
			data.add(gasTank.getStored());
		}
		else {
			data.add(false);
		}
		
		upgraded = false;

		return data;
	}

	public int getInputSlot(int operation)
	{
		return 5+operation;
	}

	public int getOutputSlot(int operation)
	{
		return 5+tier.processes+operation;
	}
	
	@Override
	public String getInventoryName()
	{
		return tier.getBaseTier().getLocalizedName() + " " + recipeType.getLocalizedName() + " " + super.getInventoryName();
	}

	@Override
	@Method(modid = "ComputerCraft")
	public String getType()
	{
		return getInventoryName();
	}

	@Override
	@Method(modid = "ComputerCraft")
	public String[] getMethodNames()
	{
		return new String[] {"getStored", "getProgress", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded"};
	}

	@Override
	@Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				if(arguments[0] == null)
				{
					return new Object[] {"Please provide a target operation."};
				}

				if(!(arguments[0] instanceof Double) && !(arguments[0] instanceof Integer))
				{
					return new Object[] {"Invalid characters."};
				}

				if((Integer)arguments[0] < 0 || (Integer)arguments[0] > progress.length)
				{
					return new Object[] {"No such operation found."};
				}

				return new Object[] {progress[(Integer)arguments[0]]};
			case 2:
				return new Object[] {facing};
			case 3:
				if(arguments[0] == null)
				{
					return new Object[] {"Please provide a target operation."};
				}

				if(!(arguments[0] instanceof Double) && !(arguments[0] instanceof Integer))
				{
					return new Object[] {"Invalid characters."};
				}

				if((Integer)arguments[0] < 0 || (Integer)arguments[0] > progress.length)
				{
					return new Object[] {"No such operation found."};
				}

				return new Object[] {canOperate(getInputSlot((Integer)arguments[0]), getOutputSlot((Integer)arguments[0]))};
			case 4:
				return new Object[] {getMaxEnergy()};
			case 5:
				return new Object[] {getMaxEnergy()-getEnergy()};
			default:
				Mekanism.logger.error("Attempted to call unknown method with computer ID " + computer.getID());
				return new Object[] {"Unknown command."};
		}
	}

	@Override
	@Method(modid = "ComputerCraft")
	public void attach(IComputerAccess computer) {}

	@Override
	@Method(modid = "ComputerCraft")
	public void detach(IComputerAccess computer) {}

	@Override
	@Method(modid = "ComputerCraft")
	public boolean equals(IPeripheral other)
	{
		return this == other;
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
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return configComponent.getOutput(TransmissionType.ITEM, side, facing).availableSlots;
	}

	@Override
	public boolean canSetFacing(int side)
	{
		return side != 0 && side != 1;
	}

	@Override
	public TileComponentConfig getConfig()
	{
		return configComponent;
	}

	@Override
	public int getOrientation()
	{
		return facing;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public SoundWrapper getSound()
	{
		return sounds[recipeType.ordinal()];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void initSounds()
	{
		sounds = new SoundWrapper[RecipeType.values().length];
		
		for(RecipeType type : RecipeType.values())
		{
			sounds[type.ordinal()] = new SoundWrapper(this, this, type.getSound());
		}
	}

	@Override
	public boolean renderUpdate()
	{
		return true;
	}

	@Override
	public boolean lightUpdate()
	{
		return true;
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
	public IEjector getEjector()
	{
		return ejectorComponent;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack, boolean doTransfer)
	{
		if(canReceiveGas(side, stack.getGas()))
		{
			return gasTank.receive(stack, doTransfer);
		}

		return 0;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack)
	{
		return receiveGas(side, stack, true);
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		if(configComponent.getOutput(TransmissionType.GAS, side.ordinal(), facing).hasSlot(0))
		{
			return recipeType.canReceiveGas(side, type);
		}
		
		return false;
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		if(recipeType.canTubeConnect(side))
		{
			return configComponent.getOutput(TransmissionType.GAS, side.ordinal(), facing).hasSlot(0);
		}
		
		return false;
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
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return false;
	}

	@Override
	public void recalculateUpgradables(Upgrade upgrade)
	{
		super.recalculateUpgradables(upgrade);

		switch(upgrade)
		{
			case GAS:
				secondaryEnergyPerTick = getSecondaryEnergyPerTick(recipeType);
				break;
			case SPEED:
				ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
				energyPerTick = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_PER_TICK);
				secondaryEnergyPerTick = getSecondaryEnergyPerTick(recipeType);
				break;
			case ENERGY:
				energyPerTick = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_PER_TICK);
				maxEnergy = MekanismUtils.getMaxEnergy(this, BASE_MAX_ENERGY);
				break;
			default:
				break;
		}
	}
}