package mekanism.common.recipe.upgrade;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.registries.MekanismAttachmentTypes;
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
    public boolean applyToStack(ItemStack stack) {
        stack.setData(MekanismAttachmentTypes.SORTING, true);
        return true;
    }
}