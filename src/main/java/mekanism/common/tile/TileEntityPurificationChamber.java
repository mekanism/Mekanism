package mekanism.common.tile;

import java.util.Map;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.config.MekanismConfig.usage;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.machines.PurificationRecipe;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.util.GasUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class TileEntityPurificationChamber extends TileEntityAdvancedElectricMachine<PurificationRecipe> {

    public TileEntityPurificationChamber() {
        super("purification", "PurificationChamber", BlockStateMachine.MachineType.PURIFICATION_CHAMBER.baseEnergy,
              usage.purificationChamberUsage, BASE_TICKS_REQUIRED, BASE_GAS_PER_TICK);
    }

    @Override
    public Map<AdvancedMachineInput, PurificationRecipe> getRecipes() {
        return Recipe.PURIFICATION_CHAMBER.get();
    }

    @Override
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        if (canReceiveGas(side, stack.getGas())) {
            return gasTank.receive(stack, doTransfer);
        }
        return 0;
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        return gasTank.canReceive(type) && isValidGas(type);
    }

    @Override
    public void handleSecondaryFuel() {
        ItemStack itemStack = inventory.get(1);
        if (!itemStack.isEmpty() && gasTank.getNeeded() > 0 && itemStack.getItem() instanceof IGasItem) {
            GasStack gas = ((IGasItem) itemStack.getItem()).getGas(itemStack);
            if (gas != null) {
                GasStack removed = GasUtils.removeGas(itemStack, gas.getGas(), gasTank.getNeeded());
                gasTank.receive(removed, true);
                return;
            }
        }

        super.handleSecondaryFuel();
    }

    @Override
    public boolean isValidGas(Gas gas) {
        return Recipe.PURIFICATION_CHAMBER.containsRecipe(gas);
    }

    @Override
    public boolean upgradeableSecondaryEfficiency() {
        return true;
    }

    @Override
    public boolean useStatisticalMechanics() {
        return true;
    }
}
