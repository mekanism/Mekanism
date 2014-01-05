package mekanism.common.tileentity;

import java.util.Map;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.Mekanism;
import mekanism.common.RecipeHandler.Recipe;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityChemicalInjectionChamber extends TileEntityAdvancedElectricMachine implements IGasHandler, ITubeConnection
{
	public TileEntityChemicalInjectionChamber()
	{
		super("ChemicalInjectionChamber.ogg", "ChemicalInjectionChamber", new ResourceLocation("mekanism", "gui/GuiChemicalInjectionChamber.png"), Mekanism.chemicalInjectionChamberUsage, 1, 200, MachineType.CHEMICAL_INJECTION_CHAMBER.baseEnergy, 1200);
	}
	
	@Override
	public Map getRecipes()
	{
		return Recipe.CHEMICAL_INJECTION_CHAMBER.get();
	}
	
	@Override
	public int getFuelTicks(ItemStack itemstack)
	{
		if(MekanismUtils.getOreDictName(itemstack).contains("dustSalt")) return 5;
		if(itemstack.itemID == Mekanism.GasTank.blockID && ((IGasItem)itemstack.getItem()).getGas(itemstack) != null &&
				((IGasItem)itemstack.getItem()).getGas(itemstack).getGas() == GasRegistry.getGas("hydrogenChloride")) return 1;
		
		return 0;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack) 
	{
		if(stack.getGas() == GasRegistry.getGas("hydrogenChloride"))
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
		return type == GasRegistry.getGas("hydrogenChloride");
	}
	
	@Override
	public void handleSecondaryFuel()
	{
		if(inventory[1] != null && secondaryEnergyStored < MAX_SECONDARY_ENERGY && inventory[1].getItem() instanceof IGasItem)
		{
			GasStack removed = GasTransmission.removeGas(inventory[1], GasRegistry.getGas("hydrogenChloride"), MAX_SECONDARY_ENERGY-secondaryEnergyStored);
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
