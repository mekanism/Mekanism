package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.EnumColor;
import mekanism.api.IConfigCardAccess;
import mekanism.api.MekanismConfig.usage;
import mekanism.api.transmitters.TransmissionType;
import mekanism.api.util.StackUtils;
import mekanism.common.PacketHandler;
import mekanism.common.SideData;
import mekanism.common.Upgrade;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.assemblicator.RecipeFormula;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityFormulaicAssemblicator extends TileEntityElectricBlock implements ISideConfiguration, IUpgradeTile, IRedstoneControl, IConfigCardAccess, ISecurityTile
{
	public InventoryCrafting dummyInv = MekanismUtils.getDummyCraftingInv();
	
	public double BASE_ENERGY_PER_TICK = usage.metallurgicInfuserUsage;

	public double energyPerTick = BASE_ENERGY_PER_TICK;

	public int BASE_TICKS_REQUIRED = 40;

	public int ticksRequired = BASE_TICKS_REQUIRED;
	
	public int operatingTicks;
	
	public boolean autoMode = false;
	
	public boolean isRecipe = false;
	
	public int pulseOperations;
	
	public RecipeFormula formula;
	
	public RedstoneControl controlType = RedstoneControl.DISABLED;
	
	public TileComponentUpgrade upgradeComponent;
	public TileComponentEjector ejectorComponent;
	public TileComponentConfig configComponent;
	public TileComponentSecurity securityComponent;
	
	public ItemStack lastFormulaStack;
	public boolean needsFormulaUpdate = false;
	public ItemStack lastOutputStack;
	
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
		
		inventory = new ItemStack[36];
		
		upgradeComponent = new TileComponentUpgrade(this, 0);
		
		ejectorComponent = new TileComponentEjector(this);
		ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(2));
		
		securityComponent = new TileComponentSecurity(this);
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
			
			RecipeFormula prev = formula;
			
			if(inventory[2] != null && inventory[2].getItem() instanceof ItemCraftingFormula)
			{
				ItemCraftingFormula item = (ItemCraftingFormula)inventory[2].getItem();
				
				if(formula == null || lastFormulaStack != inventory[2])
				{
					loadFormula();
				}
			}
			else {
				formula = null;
			}
			
			if(prev != formula)
			{
				needsFormulaUpdate = true;
			}
			
			lastFormulaStack = inventory[2];
			
			if(autoMode && formula == null)
			{
				toggleAutoMode();
			}
			
			if(autoMode && formula != null && ((controlType == RedstoneControl.PULSE && pulseOperations > 0) || MekanismUtils.canFunction(this)))
			{
				boolean canOperate = true;
				
				if(!isRecipe)
				{
					canOperate = moveItemsToGrid();
				}
				
				if(canOperate)
				{
					isRecipe = true;
					
					if(operatingTicks >= ticksRequired)
					{
						if(doSingleCraft())
						{
							operatingTicks = 0;
							
							if(pulseOperations > 0)
							{
								pulseOperations--;
							}
							
							ejectorComponent.outputItems();
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
	
	public void loadFormula()
	{
		ItemCraftingFormula item = (ItemCraftingFormula)inventory[2].getItem();
		
		if(item.getInventory(inventory[2]) != null && !item.isInvalid(inventory[2]))
		{
			RecipeFormula itemFormula = new RecipeFormula(worldObj, item.getInventory(inventory[2]));
			
			if(itemFormula.isValidFormula(worldObj))
			{
				if(formula != null && !formula.isFormulaEqual(worldObj, itemFormula))
				{
					formula = itemFormula;
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
	
	@Override
	public void markDirty()
	{
		super.markDirty();
		
		if(worldObj != null && !worldObj.isRemote)
		{
			if(formula == null)
			{
				for(int i = 0; i < 9; i++)
				{
					dummyInv.setInventorySlotContents(i, inventory[27+i]);
				}
				
				lastOutputStack = MekanismUtils.findMatchingRecipe(dummyInv, worldObj);
				isRecipe = lastOutputStack != null;
			}
			else {
				isRecipe = formula.matches(worldObj, inventory, 27);
				lastOutputStack = isRecipe ? formula.recipe.getRecipeOutput() : null;
			}
		}
	}
	
	private boolean doSingleCraft()
	{
		for(int i = 0; i < 9; i++)
		{
			dummyInv.setInventorySlotContents(i, inventory[27+i]);
		}
		
		ItemStack output = lastOutputStack;
		
		if(output != null && tryMoveToOutput(output, false))
		{
			tryMoveToOutput(output, true);
			
			for(int i = 27; i <= 35; i++)
			{
				if(inventory[i] != null)
				{
					ItemStack stack = inventory[i];
					
					inventory[i].stackSize--;
					
					if(inventory[i].stackSize == 0)
					{
						inventory[i] = null;
					}
					
					if(stack.stackSize == 0 && stack.getItem().hasContainerItem(stack))
					{
						ItemStack container = stack.getItem().getContainerItem(stack);

	                    if(container != null && container.isItemStackDamageable() && container.getItemDamage() > container.getMaxDamage())
	                    {
	                    	container = null;
	                    }

	                    if(container != null)
	                    {
                    		boolean move = tryMoveToOutput(container.copy(), false);
                    		
                    		if(move)
                    		{
                    			tryMoveToOutput(container.copy(), true);
                    		}
                    		
                    		inventory[i] = move ? null : container.copy();
	                    }
					}
				}
			}
			
			if(formula != null)
			{
				moveItemsToGrid();
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
			boolean canOperate = true;
			
			if(!formula.matches(worldObj, inventory, 27))
			{
				canOperate = moveItemsToGrid();
			}
			
			if(canOperate)
			{
				return doSingleCraft();
			}
		}
		else {
			return doSingleCraft();
		}
		
		return false;
	}
	
	private boolean moveItemsToGrid()
	{
		boolean ret = true;
		
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
					ret = false;
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
					ret = false;
				}
			}
		}
		
		return ret;
	}
	
	private void craftAll()
	{
		while(craftSingle());
	}
	
	private void moveItemsToInput(boolean forcePush)
	{
		for(int i = 27; i <= 35; i++)
		{
			if(inventory[i] != null && (forcePush || (formula != null && !formula.isIngredientInPos(worldObj, inventory[i], i-27))))
			{
				inventory[i] = tryMoveToInput(inventory[i]);
			}
		}
		
		markDirty();
	}
	
	private void toggleAutoMode()
	{
		if(autoMode)
		{
			operatingTicks = 0;
			autoMode = false;
		}
		else if(formula != null)
		{
			moveItemsToInput(false);
			autoMode = true;
		}
		
		markDirty();
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
				RecipeFormula formula = new RecipeFormula(worldObj, inventory, 27);
				
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
	public int[] getSlotsForFace(EnumFacing side)
	{
		return configComponent.getOutput(TransmissionType.ITEM, side, facing).availableSlots;
	}
	
	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, EnumFacing side)
	{
		if(slotID == 1)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
		}
		else if(slotID >= 21 && slotID <= 26)
		{
			return true;
		}

		return false;
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID >= 3 && slotID <= 20)
		{
			if(formula == null)
			{
				return true;
			}
			else {
				return formula.isIngredient(worldObj, itemstack);
			}
		}
		else if(slotID == 1)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}

		return false;
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
	public NBTTagCompound writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setBoolean("autoMode", autoMode);
		nbtTags.setInteger("operatingTicks", operatingTicks);
		nbtTags.setInteger("controlType", controlType.ordinal());
		nbtTags.setInteger("pulseOperations", pulseOperations);
		
		return nbtTags;
	}
	
	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
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
			else if(type == 4)
			{
				if(formula != null)
				{
					moveItemsToGrid();
				}
				else {
					moveItemsToInput(true);
				}
			}
			
			return;
		}
		
		super.handlePacketData(dataStream);
		
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			autoMode = dataStream.readBoolean();
			operatingTicks = dataStream.readInt();
			controlType = RedstoneControl.values()[dataStream.readInt()];
			isRecipe = dataStream.readBoolean();
			
			if(dataStream.readBoolean())
			{
				if(dataStream.readBoolean())
				{
					ItemStack[] inv = new ItemStack[9];
					
					for(int i = 0; i < 9; i++)
					{
						if(dataStream.readBoolean())
						{
							inv[i] = PacketHandler.readStack(dataStream);
						}
					}
					
					formula = new RecipeFormula(worldObj, inv);
				}
				else {
					formula = null;
				}
			}
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		data.add(autoMode);
		data.add(operatingTicks);
		data.add(controlType.ordinal());
		data.add(isRecipe);
		
		if(needsFormulaUpdate)
		{
			data.add(true);
			
			if(formula != null)
			{
				data.add(true);
				
				for(int i = 0; i < 9; i++)
				{
					if(formula.input[i] != null)
					{
						data.add(true);
						data.add(formula.input[i]);
					}
					else {
						data.add(false);
					}
				}
			}
			else {
				data.add(false);
			}
		}
		else {
			data.add(false);
		}
		
		needsFormulaUpdate = false;
		
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
	public EnumFacing getOrientation()
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
	public TileComponentSecurity getSecurity()
	{
		return securityComponent;
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
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing side)
	{
		return capability == Capabilities.CONFIG_CARD_CAPABILITY || super.hasCapability(capability, side);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing side)
	{
		if(capability == Capabilities.CONFIG_CARD_CAPABILITY)
		{
			return (T)this;
		}
		
		return super.getCapability(capability, side);
	}
}
