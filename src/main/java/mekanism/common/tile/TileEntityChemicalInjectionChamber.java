package mekanism.common.tile;

import java.util.Map;

import mekanism.api.EnumColor;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismFluids;
import mekanism.common.SideData;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.config.MekanismConfig.usage;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.machines.InjectionRecipe;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.util.GasUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class TileEntityChemicalInjectionChamber extends TileEntityAdvancedElectricMachine<InjectionRecipe>
{
	public TileEntityChemicalInjectionChamber()
	{
		super("injection", "ChemicalInjectionChamber", BlockStateMachine.MachineType.CHEMICAL_INJECTION_CHAMBER.baseEnergy, usage.chemicalInjectionChamberUsage, 200, 1);
		
		configComponent.addSupported(TransmissionType.GAS);
		configComponent.addOutput(TransmissionType.GAS, new SideData("None", EnumColor.GREY, InventoryUtils.EMPTY));
		configComponent.addOutput(TransmissionType.GAS, new SideData("Gas", EnumColor.DARK_RED, new int[] {0}));
		configComponent.fillConfig(TransmissionType.GAS, 1);
		configComponent.setCanEject(TransmissionType.GAS, false);
	}

	@Override
	public Map<AdvancedMachineInput, InjectionRecipe> getRecipes()
	{
		return Recipe.CHEMICAL_INJECTION_CHAMBER.get();
	}

	@Override
	public GasStack getItemGas(ItemStack itemstack)
	{
		if(MekanismUtils.getOreDictName(itemstack).contains("dustSulfur")) return new GasStack(MekanismFluids.SulfuricAcid, 2);
		if(MekanismUtils.getOreDictName(itemstack).contains("dustSalt")) return new GasStack(MekanismFluids.HydrogenChloride, 2);
		if(Block.getBlockFromItem(itemstack.getItem()) == MekanismBlocks.GasTank && ((IGasItem)itemstack.getItem()).getGas(itemstack) != null &&
				isValidGas(((IGasItem)itemstack.getItem()).getGas(itemstack).getGas())) return new GasStack(MekanismFluids.SulfuricAcid, 1);

		return null;
	}

	@Override
	public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer)
	{
		if(canReceiveGas(side, stack.getGas()))
		{
			return gasTank.receive(stack, doTransfer);
		}

		return 0;
	}

	@Override
	public boolean canReceiveGas(EnumFacing side, Gas type)
	{
		if(configComponent.getOutput(TransmissionType.GAS, side, facing).hasSlot(0))
		{
			return isValidGas(type);
		}
		
		return false;
	}

	@Override
	public void handleSecondaryFuel()
	{
		if(!inventory.get(1).isEmpty() && gasTank.getNeeded() > 0 && inventory.get(1).getItem() instanceof IGasItem)
		{
			GasStack gas = ((IGasItem)inventory.get(1).getItem()).getGas(inventory.get(1));

			if(gas != null && isValidGas(gas.getGas()))
			{
				GasStack removed = GasUtils.removeGas(inventory.get(1), gasTank.getGasType(), gasTank.getNeeded());
				gasTank.receive(removed, true);
			}

			return;
		}

		super.handleSecondaryFuel();
	}

	@Override
	public boolean canTubeConnect(EnumFacing side)
	{
		return configComponent.getOutput(TransmissionType.GAS, side, facing).hasSlot(0);
	}

	@Override
	public boolean isValidGas(Gas gas)
	{
		return gas == MekanismFluids.SulfuricAcid || gas == MekanismFluids.Water || gas == MekanismFluids.HydrogenChloride;
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
