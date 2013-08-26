package mekanism.common.tileentity;

import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;
import mekanism.api.IStorageTank;
import mekanism.api.gas.EnumGas;
import mekanism.api.gas.IGasAcceptor;
import mekanism.api.gas.IGasStorage;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.Mekanism;
import mekanism.common.RecipeHandler.Recipe;
import mekanism.common.block.BlockMachine.MachineType;

public class TileEntityPurificationChamber extends TileEntityAdvancedElectricMachine implements IGasAcceptor, IGasStorage, ITubeConnection
{
	public TileEntityPurificationChamber()
	{
		super("PurificationChamber.ogg", "Purification Chamber", new ResourceLocation("mekanism", "gui/GuiPurificationChamber.png"), Mekanism.purificationChamberUsage, 1, 200, MachineType.PURIFICATION_CHAMBER.baseEnergy, 1200);
	}
	
	@Override
	public Map getRecipes()
	{
		return Recipe.PURIFICATION_CHAMBER.get();
	}
	
	@Override
	public int getFuelTicks(ItemStack itemstack)
	{
		if(itemstack.isItemEqual(new ItemStack(Item.flint))) return 300;
		
		return 0;
	}

	@Override
	public int getGas(EnumGas type, Object... data) 
	{
		if(type == EnumGas.OXYGEN)
		{
			return secondaryEnergyStored;
		}
		
		return 0;
	}

	@Override
	public void setGas(EnumGas type, int amount, Object... data) 
	{
		if(type == EnumGas.OXYGEN)
		{
			setSecondaryEnergy(amount);
		}
	}
	
	@Override
	public int getMaxGas(EnumGas type, Object... data)
	{
		if(type == EnumGas.OXYGEN)
		{
			return MAX_SECONDARY_ENERGY;
		}
		
		return 0;
	}

	@Override
	public int transferGasToAcceptor(int amount, EnumGas type) 
	{
		if(type == EnumGas.OXYGEN)
		{
	    	int rejects = 0;
	    	int neededGas = MAX_SECONDARY_ENERGY-secondaryEnergyStored;
	    	if(amount <= neededGas)
	    	{
	    		secondaryEnergyStored += amount;
	    	}
	    	else if(amount > neededGas)
	    	{
	    		secondaryEnergyStored += neededGas;
	    		rejects = amount-neededGas;
	    	}
	    	return rejects;
		}
		
		return amount;
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, EnumGas type)
	{
		return type == EnumGas.OXYGEN;
	}
	
	@Override
	public void handleSecondaryFuel()
	{
		if(inventory[1] != null && secondaryEnergyStored < MAX_SECONDARY_ENERGY)
		{
			if(inventory[1].getItem() instanceof IStorageTank)
			{
				if(((IStorageTank)inventory[1].getItem()).getGasType(inventory[1]) == EnumGas.OXYGEN)
				{
					IStorageTank item = (IStorageTank)inventory[1].getItem();
					
					if(item.canProvideGas(inventory[1], EnumGas.OXYGEN))
					{
						int received = 0;
						int gasNeeded = MAX_SECONDARY_ENERGY - secondaryEnergyStored;
						if(item.getRate() <= gasNeeded)
						{
							received = item.removeGas(inventory[1], EnumGas.OXYGEN, item.getRate());
						}
						else if(item.getRate() > gasNeeded)
						{
							received = item.removeGas(inventory[1], EnumGas.OXYGEN, gasNeeded);
						}
						
						setGas(EnumGas.OXYGEN, secondaryEnergyStored + received);
					}
				}
			}
		}
		
		super.handleSecondaryFuel();
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return true;
	}
}
