package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.util.EnumFacing;

public class TileEntityPurificationChamber extends TileEntityAdvancedElectricMachine {

    public TileEntityPurificationChamber() {
        super("purifier", MachineType.PURIFICATION_CHAMBER, BASE_TICKS_REQUIRED, BASE_GAS_PER_TICK);
    }

    @Nonnull
    @Override
    public Recipe<ItemStackGasToItemStackRecipe> getRecipes() {
        return Recipe.PURIFICATION_CHAMBER;
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
    public boolean upgradeableSecondaryEfficiency() {
        return true;
    }

    @Override
    public boolean useStatisticalMechanics() {
        return true;
    }
}