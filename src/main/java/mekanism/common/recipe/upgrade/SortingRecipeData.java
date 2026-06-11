package mekanism.common.recipe.upgrade;

import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.ItemAccessUtils;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class SortingRecipeData implements RecipeUpgradeData<SortingRecipeData> {

    static final SortingRecipeData SORTING = new SortingRecipeData();

    private SortingRecipeData() {
    }

    @Nullable
    @Override
    public SortingRecipeData merge(SortingRecipeData other) {
        return this;
    }

    @Override
    public boolean applyToStack(ItemAccess itemAccess, TransactionContext transaction) {
        return ItemAccessUtils.exchange(itemAccess, itemAccess.getResource().with(MekanismDataComponents.SORTING, true),  transaction);
    }
}