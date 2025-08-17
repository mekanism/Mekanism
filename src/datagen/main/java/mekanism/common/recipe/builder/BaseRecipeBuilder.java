package mekanism.common.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import net.minecraft.core.Holder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public abstract class BaseRecipeBuilder<BUILDER extends BaseRecipeBuilder<BUILDER>> extends MekanismRecipeBuilder<BUILDER> {

    private final Holder<Item> result;
    private final int count;
    protected RecipeCategory category = RecipeCategory.MISC;

    protected BaseRecipeBuilder(Holder<Item> result, int count) {
        this.result = result;
        this.count = count;
    }

    @SuppressWarnings("unchecked")
    public BUILDER category(RecipeCategory category) {
        this.category = category;
        return (BUILDER) this;
    }

    @Override
    public Item getResult() {
        return result.value();
    }

    protected ItemStack resultStack() {
        return new ItemStack(result, count);
    }
}