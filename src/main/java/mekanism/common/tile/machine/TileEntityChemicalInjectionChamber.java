package mekanism.common.tile.machine;

import javax.annotation.Nonnull;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;

public class TileEntityChemicalInjectionChamber extends TileEntityAdvancedElectricMachine {

    public TileEntityChemicalInjectionChamber() {
        super(MekanismBlocks.CHEMICAL_INJECTION_CHAMBER, BASE_TICKS_REQUIRED);
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ItemStackGasToItemStackRecipe> getRecipeType() {
        return MekanismRecipeType.INJECTING;
    }

    @Override
    public boolean useStatisticalMechanics() {
        return true;
    }
}