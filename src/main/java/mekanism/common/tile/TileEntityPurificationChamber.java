package mekanism.common.tile;

import java.util.Map;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismFluids;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.config.MekanismConfig.usage;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.machines.PurificationRecipe;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.util.GasUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class TileEntityPurificationChamber extends TileEntityAdvancedElectricMachine<PurificationRecipe>
{
	public TileEntityPurificationChamber()
	{
		super("purification", "PurificationChamber", BlockStateMachine.MachineType.PURIFICATION_CHAMBER.baseEnergy, usage.purificationChamberUsage, 200, 1);
	}

	@Override
	public Map<AdvancedMachineInput, PurificationRecipe> getRecipes()
	{
		return Recipe.PURIFICATION_CHAMBER.get();
	}

	@Override
	public GasStack getItemGas(ItemStack itemstack)
	{
		if(itemstack.isItemEqual(new ItemStack(Items.FLINT))) return new GasStack(MekanismFluids.Oxygen, 10);
		if(Block.getBlockFromItem(itemstack.getItem()) == MekanismBlocks.GasTank && ((IGasItem)itemstack.getItem()).getGas(itemstack) != null &&
				((IGasItem)itemstack.getItem()).getGas(itemstack).getGas() == MekanismFluids.Oxygen) return new GasStack(MekanismFluids.Oxygen, 1);

		return null;
	}

	@Override
	public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer)
	{
		if(stack.getGas() == MekanismFluids.Oxygen)
		{
			return gasTank.receive(stack, doTransfer);
		}

		return 0;
	}

	@Override
	public boolean canReceiveGas(EnumFacing side, Gas type)
	{
		return type == MekanismFluids.Oxygen;
	}

	@Override
	public void handleSecondaryFuel()
	{
		if(!inventory.get(1).isEmpty() && gasTank.getNeeded() > 0 && inventory.get(1).getItem() instanceof IGasItem)
		{
			GasStack removed = GasUtils.removeGas(inventory.get(1), MekanismFluids.Oxygen, gasTank.getNeeded());
			gasTank.receive(removed, true);
			return;
		}

		super.handleSecondaryFuel();
	}

	@Override
	public boolean canTubeConnect(EnumFacing side)
	{
		return true;
	}

	@Override
	public boolean isValidGas(Gas gas)
	{
		return gas == MekanismFluids.Oxygen;
	}

	@Override
	public boolean upgradeableSecondaryEfficiency()
	{
		return true;
	}

	@Override
	public boolean useStatisticalMechanics()
	{
		return true;
	}
}
