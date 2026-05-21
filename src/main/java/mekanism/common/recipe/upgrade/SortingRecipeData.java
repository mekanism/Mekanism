package mekanism.common.recipe.upgrade;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.registries.MekanismDataComponents;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
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
    public boolean applyToStack(ItemAccess itemAccess) {
        try (Transaction transaction = Transaction.openRoot()) {
            int exchanged = itemAccess.exchange(itemAccess.getResource().with(MekanismDataComponents.SORTING, true), itemAccess.getAmount(), transaction);
            transaction.commit();
            return exchanged != 0;
        }
    }
}