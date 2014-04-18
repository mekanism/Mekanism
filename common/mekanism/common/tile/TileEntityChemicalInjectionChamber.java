package mekanism.common.tile;

import java.util.Map;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.ITubeConnection;
import mekanism.client.gui.GuiProgress.ProgressBar;
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
		super("ChemicalInjectionChamber.ogg", "ChemicalInjectionChamber", Mekanism.chemicalInjectionChamberUsage, 1, 200, MachineType.CHEMICAL_INJECTION_CHAMBER.baseEnergy);
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
				isValidGas(((IGasItem)itemstack.getItem()).getGas(itemstack).getGas())) return new GasStack(GasRegistry.getGas("sulfuricAcid"), 1);

		return null;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack)
	{
		if(isValidGas(stack.getGas()))
		{
			return gasTank.receive(stack, true);
		}

		return 0;
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return isValidGas(type);
	}

	@Override
	public void handleSecondaryFuel()
	{
		if(inventory[1] != null && gasTank.getNeeded() > 0 && inventory[1].getItem() instanceof IGasItem)
		{
			GasStack gas = ((IGasItem)inventory[1].getItem()).getGas(inventory[1]);

			if(gas != null && isValidGas(gas.getGas()))
			{
				GasStack removed = GasTransmission.removeGas(inventory[1], gasTank.getGas() != null ? gasTank.getGas().getGas() : null, gasTank.getNeeded());
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

	@Override
	public boolean isValidGas(Gas gas)
	{
		return gas == GasRegistry.getGas("sulfuricAcid") || gas == GasRegistry.getGas("water") || gas == GasRegistry.getGas("hydrogenChloride");
	}

	@Override
	public ProgressBar getProgressType()
	{
		return ProgressBar.YELLOW;
	}
}
