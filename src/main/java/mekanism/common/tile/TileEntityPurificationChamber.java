package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.RecipeHandler.RecipeWrapper;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.util.Direction;

public class TileEntityPurificationChamber extends TileEntityAdvancedElectricMachine {

    public TileEntityPurificationChamber() {
        super(MekanismBlock.PURIFICATION_CHAMBER, BASE_TICKS_REQUIRED, BASE_GAS_PER_TICK);
    }

    @Nonnull
    @Override
    public RecipeWrapper<ItemStackGasToItemStackRecipe> getRecipeWrapper() {
        return RecipeWrapper.PURIFYING;
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
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