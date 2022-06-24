package mekanism.common.recipe.upgrade;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.world.item.ItemStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
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
        ItemDataUtils.setBoolean(stack, NBTConstants.SORTING, true);
        return true;
    }
}