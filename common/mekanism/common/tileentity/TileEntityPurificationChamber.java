package mekanism.common.tileentity;

import java.util.Map;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasAcceptor;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.IGasStorage;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.Mekanism;
import mekanism.common.RecipeHandler.Recipe;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityPurificationChamber extends TileEntityAdvancedElectricMachine implements IGasAcceptor, IGasStorage, ITubeConnection
{
	public TileEntityPurificationChamber()
	{
		super("PurificationChamber.ogg", "PurificationChamber", new ResourceLocation("mekanism", "gui/GuiPurificationChamber.png"), Mekanism.purificationChamberUsage, 1, 200, MachineType.PURIFICATION_CHAMBER.baseEnergy, 1200);
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
		if(itemstack.isItemEqual(new ItemStack(Mekanism.GasTank)) && ((IGasItem)itemstack.getItem()).getGas(itemstack) != null &&
				((IGasItem)itemstack.getItem()).getGas(itemstack).getGas() == GasRegistry.getGas("oxygen")) return 1;
		
		return 0;
	}

	@Override
	public GasStack getGas(Object... data) 
	{
		if(secondaryEnergyStored == 0)
		{
			return null;
		}
		
		return new GasStack(GasRegistry.getGas("oxygen"), secondaryEnergyStored);
	}

	@Override
	public void setGas(GasStack stack, Object... data) 
	{
		if(stack == null)
		{
			setSecondaryEnergy(0);
		}
		else if(stack.getGas() == GasRegistry.getGas("oxygen"))
		{
			setSecondaryEnergy(stack.amount);
		}
		
		MekanismUtils.saveChunk(this);
	}
	
	@Override
	public int getMaxGas(Object... data)
	{
		return MAX_SECONDARY_ENERGY;
	}

	@Override
	public int receiveGas(GasStack stack) 
	{
		if(stack.getGas() == GasRegistry.getGas("oxygen"))
		{
			int stored = getGas() != null ? getGas().amount : 0;
			int toUse = Math.min(getMaxGas()-stored, stack.amount);
			
			setGas(new GasStack(stack.getGas(), stored + toUse));
	    	
	    	return toUse;
		}
		
		return 0;
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return type == GasRegistry.getGas("oxygen");
	}
	
	@Override
	public void handleSecondaryFuel()
	{
		if(inventory[1] != null && secondaryEnergyStored < MAX_SECONDARY_ENERGY)
		{
			GasStack removed = GasTransmission.removeGas(inventory[1], GasRegistry.getGas("oxygen"), MAX_SECONDARY_ENERGY-secondaryEnergyStored);
			setSecondaryEnergy(secondaryEnergyStored + (removed != null ? removed.amount : 0));
		}
		
		super.handleSecondaryFuel();
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return true;
	}
}
