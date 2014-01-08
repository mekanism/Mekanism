package mekanism.common.tile;

import java.util.Map;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityPurificationChamber extends TileEntityAdvancedElectricMachine implements IGasHandler, ITubeConnection
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
		if(itemstack.itemID == Mekanism.GasTank.blockID && ((IGasItem)itemstack.getItem()).getGas(itemstack) != null &&
				((IGasItem)itemstack.getItem()).getGas(itemstack).getGas() == GasRegistry.getGas("oxygen")) return 1;
		
		return 0;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack) 
	{
		if(stack.getGas() == GasRegistry.getGas("oxygen"))
		{
			int toUse = Math.min(MAX_SECONDARY_ENERGY-secondaryEnergyStored, stack.amount);
			secondaryEnergyStored += toUse;
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
		if(inventory[1] != null && secondaryEnergyStored < MAX_SECONDARY_ENERGY && inventory[1].getItem() instanceof IGasItem)
		{
			GasStack removed = GasTransmission.removeGas(inventory[1], GasRegistry.getGas("oxygen"), MAX_SECONDARY_ENERGY-secondaryEnergyStored);
			setSecondaryEnergy(secondaryEnergyStored + (removed != null ? removed.amount : 0));
			return;
		}
		
		super.handleSecondaryFuel();
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return true;
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount)
	{
		return null;
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return false;
	}
}
