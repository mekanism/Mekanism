package mekanism.common.item.block.machine;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemChemical;

public class ItemBlockChemicalInjectionChamber extends ItemBlockAdvancedElectricMachine {

    public ItemBlockChemicalInjectionChamber(BlockTile<?, ?> block) {
        super(block);
    }

    @Override
    protected IMekanismRecipeTypeProvider<ItemStackGasToItemStackRecipe, ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> getRecipeType() {
        return MekanismRecipeType.INJECTING;
    }
}