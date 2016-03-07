package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.EnumColor;
import mekanism.api.IConfigCardAccess;
import mekanism.api.MekanismConfig.usage;
import mekanism.api.transmitters.TransmissionType;
import mekanism.api.util.StackUtils;
import mekanism.common.SideData;
import mekanism.common.Upgrade;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.content.assemblicator.RecipeFormula;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityFormulaicAssemblicator extends TileEntityElectricBlock implements ISideConfiguration, IUpgradeTile, IRedstoneControl, IConfigCardAccess
{
	public InventoryCrafting dummyInv = MekanismUtils.getDummyCraftingInv();
	
	public double BASE_ENERGY_PER_TICK = usage.metallurgicInfuserUsage;

	public double energyPerTick = BASE_ENERGY_PER_TICK;

	public int BASE_TICKS_REQUIRED = 100;

	public int ticksRequired = BASE_TICKS_REQUIRED;
	
	public int operatingTicks;
	
	public boolean autoMode = false;
	
	public int pulseOperations;
	
	public RecipeFormula formula;
	
	public RedstoneControl controlType = RedstoneControl.DISABLED;
	
	public TileComponentUpgrade upgradeComponent;
	public TileComponentEjector ejectorComponent;
	public TileComponentConfig configComponent;
	
	public TileEntityFormulaicAssemblicator() 
	{
		super("FormulaicAssemblicator", MachineType.FORMULAIC_ASSEMBLICATOR.baseEnergy);
		
		configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);
		
		configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED, 
				new int[] {3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, new int[] {21, 22, 23, 24, 25, 26}));
		configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[] {1}));
		
		configComponent.setConfig(TransmissionType.ITEM, new byte[] {0, 0, 0, 3, 1, 2});
		configComponent.setInputConfig(TransmissionType.ENERGY);
		
		inventory = new ItemStack[35];
		
		upgradeComponent = new TileComponentUpgrade(this, 0);
		
		ejectorComponent = new TileComponentEjector(this);
		ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(2));
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!worldObj.isRemote)
		{
			ChargeUtils.discharge(1, this);
			
			if(controlType != RedstoneControl.PULSE)
			{
				pulseOperations = 0;
			}
			else if(MekanismUtils.canFunction(this))
			{
				pulseOperations++;
			}
			
			if(inventory[2] != null && inventory[2].getItem() instanceof ItemCraftingFormula)
			{
				ItemCraftingFormula item = (ItemCraftingFormula)inventory[2].getItem();
				
				if(item.getInventory(inventory[2]) != null && !item.isInvalid(inventory[2]))
				{
					RecipeFormula itemFormula = new RecipeFormula(item.getInventory(inventory[2]));
					
					if(itemFormula.isValidFormula(worldObj))
					{
						if(formula != null && !formula.isFormulaEqual(worldObj, itemFormula))
						{
							itemFormula = formula;
							operatingTicks = 0;
						}
						else if(formula == null)
						{
							formula = itemFormula;
						}
					}
					else {
						formula = null;
						item.setInvalid(inventory[2], true);
					}
				}
				else {
					formula = null;
				}
			}
			else {
				formula = null;
			}
			
			if(autoMode && formula != null && ((controlType == RedstoneControl.PULSE && pulseOperations > 0) || MekanismUtils.canFunction(this)))
			{
				boolean canOperate = true;
				
				if(!formula.matches(worldObj, inventory, 27))
				{
					canOperate = moveItemsToGrid();
				}
				
				if(canOperate)
				{
					if(operatingTicks == ticksRequired)
					{
						if(doSingleCraft())
						{
							operatingTicks = 0;
							
							if(pulseOperations > 0)
							{
								pulseOperations--;
							}
						}
					}
					else {
						if(getEnergy() >= energyPerTick)
						{
							operatingTicks++;
							setEnergy(getEnergy() - energyPerTick);
						}
					}
				}
				else {
					operatingTicks = 0;
				}
			}
			else {
				operatingTicks = 0;
			}
		}
	}
	
	private boolean doSingleCraft()
	{
		for(int i = 0; i < 9; i++)
		{
			dummyInv.setInventorySlotContents(i, inventory[27+i]);
		}
		
		ItemStack output = MekanismUtils.findMatchingRecipe(dummyInv, worldObj);
		
		if(output != null && tryMoveToOutput(output, false))
		{
			tryMoveToOutput(output, true);
			
			for(int i = 27; i <= 35; i++)
			{
				inventory[i].stackSize--;
				
				if(inventory[i].stackSize == 0)
				{
					inventory[i] = null;
				}
			}
			
			markDirty();
			
			return true;
		}
		
		return false;
	}
	
	private boolean craftSingle()
	{
		if(formula != null)
		{
			boolean canOperate = false;
			
			if(!formula.matches(worldObj, inventory, 27))
			{
				canOperate = moveItemsToGrid();
			}
			
			if(canOperate)
			{
				doSingleCraft();
			}
		}
		else {
			doSingleCraft();
		}
		
		return false;
	}
	
	private boolean moveItemsToGrid()
	{
		for(int i = 27; i <= 35; i++)
		{
			if(formula.isIngredientInPos(worldObj, inventory[i], i-27))
			{
				continue;
			}
			
			if(inventory[i] != null)
			{
				inventory[i] = tryMoveToInput(inventory[i]);
				markDirty();
				
				if(inventory[i] != null)
				{
					return false;
				}
			}
			else {
				boolean found = false;
				
				for(int j = 3; j <= 20; j++)
				{
					if(inventory[j] != null && formula.isIngredientInPos(worldObj, inventory[j], i-27))
					{
						inventory[i] = StackUtils.size(inventory[j], 1);
						inventory[j].stackSize--;
						
						if(inventory[j].stackSize == 0)
						{
							inventory[j] = null;
						}
						
						markDirty();
						found = true;
						
						break;
					}
				}
				
				if(!found)
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	private void craftAll()
	{
		while(craftSingle());
	}
	
	private void toggleAutoMode()
	{
		if(autoMode)
		{
			operatingTicks = 0;
			autoMode = false;
		}
		else {
			for(int i = 27; i <= 35; i++)
			{
				inventory[i] = tryMoveToInput(inventory[i]);
			}
			
			markDirty();
			autoMode = true;
		}
	}
	
	private ItemStack tryMoveToInput(ItemStack stack)
	{
		stack = stack.copy();
		
		for(int i = 3; i <= 20; i++)
		{
			if(inventory[i] == null)
			{
				inventory[i] = stack;
				
				return null;
			}
			else if(InventoryUtils.areItemsStackable(stack, inventory[i]) && inventory[i].stackSize < inventory[i].getMaxStackSize())
			{
				int toUse = Math.min(stack.stackSize, inventory[i].getMaxStackSize()-inventory[i].stackSize);
				
				inventory[i].stackSize += toUse;
				stack.stackSize -= toUse;
				
				if(stack.stackSize == 0)
				{
					return null;
				}
			}
		}
		
		return stack;
	}
	
	private boolean tryMoveToOutput(ItemStack stack, boolean doMove)
	{
		stack = stack.copy();
		
		for(int i = 21; i <= 26; i++)
		{
			if(inventory[i] == null)
			{
				if(doMove)
				{
					inventory[i] = stack;
				}
				
				return true;
			}
			else if(InventoryUtils.areItemsStackable(stack, inventory[i]) && inventory[i].stackSize < inventory[i].getMaxStackSize())
			{
				int toUse = Math.min(stack.stackSize, inventory[i].getMaxStackSize()-inventory[i].stackSize);
				
				if(doMove)
				{
					inventory[i].stackSize += toUse;
				}
				
				stack.stackSize -= toUse;
				
				if(stack.stackSize == 0)
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	private void encodeFormula()
	{
		if(inventory[2] != null && inventory[2].getItem() instanceof ItemCraftingFormula)
		{
			ItemCraftingFormula item = (ItemCraftingFormula)inventory[2].getItem();
			
			if(item.getInventory(inventory[2]) == null)
			{
				RecipeFormula formula = new RecipeFormula(inventory, 27);
				
				if(formula.isValidFormula(worldObj))
				{
					item.setInventory(inventory[2], formula.input);
				}
			}
		}
	}
	
	@Override
	public boolean canSetFacing(int side)
	{
		return side != 0 && side != 1;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);
		
		autoMode = nbtTags.getBoolean("autoMode");
		operatingTicks = nbtTags.getInteger("operatingTicks");
		controlType = RedstoneControl.values()[nbtTags.getInteger("controlType")];
		pulseOperations = nbtTags.getInteger("pulseOperations");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setBoolean("autoMode", autoMode);
		nbtTags.setInteger("operatingTicks", operatingTicks);
		nbtTags.setInteger("controlType", controlType.ordinal());
		nbtTags.setInteger("pulseOperations", pulseOperations);
	}
	
	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(!worldObj.isRemote)
		{
			int type = dataStream.readInt();
			
			if(type == 0)
			{
				toggleAutoMode();
			}
			else if(type == 1)
			{
				encodeFormula();
			}
			else if(type == 2)
			{
				craftSingle();
			}
			else if(type == 3)
			{
				craftAll();
			}
			
			return;
		}
		
		super.handlePacketData(dataStream);
		
		autoMode = dataStream.readBoolean();
		operatingTicks = dataStream.readInt();
		controlType = RedstoneControl.values()[dataStream.readInt()];
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(autoMode);
		data.add(operatingTicks);
		data.add(controlType.ordinal());
		
		return data;
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
		return true;
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
	public TileComponentUpgrade getComponent()
	{
		return upgradeComponent;
	}
	
	@Override
	public TileComponentEjector getEjector()
	{
		return ejectorComponent;
	}
	
	@Override
	public void recalculateUpgradables(Upgrade upgrade)
	{
		super.recalculateUpgradables(upgrade);

		switch(upgrade)
		{
			case SPEED:
				ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
			case ENERGY:
				energyPerTick = MekanismUtils.getEnergyPerTick(this, BASE_ENERGY_PER_TICK);
				maxEnergy = MekanismUtils.getMaxEnergy(this, BASE_MAX_ENERGY);
			default:
				break;
		}
	}
}
