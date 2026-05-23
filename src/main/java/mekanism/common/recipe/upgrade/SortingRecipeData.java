package mekanism.common.recipe.upgrade;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.ItemAccessUtils;
import net.neoforged.neoforge.transfer.access.ItemAccess;
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
        return ItemAccessUtils.exchange(itemAccess, itemAccess.getResource().with(MekanismDataComponents.SORTING, true),  null);
    }
}