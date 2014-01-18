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
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityChemicalInjectionChamber extends TileEntityAdvancedElectricMachine implements IGasHandler, ITubeConnection
{
	public TileEntityChemicalInjectionChamber()
	{
		super("ChemicalInjectionChamber.ogg", "ChemicalInjectionChamber", new ResourceLocation("mekanism", "gui/GuiChemicalInjectionChamber.png"), Mekanism.chemicalInjectionChamberUsage, 1, 200, MachineType.CHEMICAL_INJECTION_CHAMBER.baseEnergy);
	}
	
	@Override
	public Map getRecipes()
	{
		return Recipe.CHEMICAL_INJECTION_CHAMBER.get();
	}
	
	@Override
	public GasStack getItemGas(ItemStack itemstack)
	{
		if(MekanismUtils.getOreDictName(itemstack).contains("dustSulfur")) return new GasStack(GasRegistry.getGas("sulfuricAcid"), 2);
		if(itemstack.itemID == Mekanism.GasTank.blockID && ((IGasItem)itemstack.getItem()).getGas(itemstack) != null &&
				(((IGasItem)itemstack.getItem()).getGas(itemstack).getGas() == GasRegistry.getGas("sulfuricAcid") ||
						((IGasItem)itemstack.getItem()).getGas(itemstack).getGas() == GasRegistry.getGas("water"))) return new GasStack(GasRegistry.getGas("sulfuricAcid"), 1);
		
		return null;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack) 
	{
		if(stack.getGas() == GasRegistry.getGas("sulfuricAcid") || stack.getGas() == GasRegistry.getGas("water"))
		{
			return gasTank.receive(stack, true);
		}
		
		return 0;
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return type == GasRegistry.getGas("sulfuricAcid") || type == GasRegistry.getGas("water");
	}
	
	@Override
	public void handleSecondaryFuel()
	{
		if(inventory[1] != null && gasTank.getNeeded() > 0 && inventory[1].getItem() instanceof IGasItem)
		{
			Gas gas = ((IGasItem)inventory[1].getItem()).getGas(inventory[1]).getGas();
			
			if(gas == GasRegistry.getGas("sulfuricAcid") || gas == GasRegistry.getGas("water"))
			{
				GasStack removed = GasTransmission.removeGas(inventory[1], gas, gasTank.getNeeded());
				gasTank.receive(removed, true);
			}
			
			return;
		}
		
		super.handleSecondaryFuel();
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return true;
	}
}
