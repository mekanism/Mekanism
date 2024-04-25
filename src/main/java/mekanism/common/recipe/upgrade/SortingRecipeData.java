package mekanism.common.recipe.upgrade;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
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
    public boolean applyToStack(HolderLookup.Provider provider, ItemStack stack) {
        stack.set(MekanismDataComponents.SORTING, true);
        return true;
    }
}