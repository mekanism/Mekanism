package mekanism.common.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import net.minecraft.core.Holder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class BaseRecipeBuilder<BUILDER extends BaseRecipeBuilder<BUILDER>> extends MekanismRecipeBuilder<BUILDER> {

    private final Holder<Item> result;
    private final int count;
    protected RecipeCategory category = RecipeCategory.MISC;
    @Nullable
    protected String group;

    protected BaseRecipeBuilder(Holder<Item> result, int count) {
        this.result = result;
        this.count = count;
    }

    @SuppressWarnings("unchecked")
    private BUILDER self() {
        return (BUILDER) this;
    }

    public BUILDER group(String group) {
        this.group = group;
        return self();
    }

    public BUILDER category(RecipeCategory category) {
        this.category = category;
        return self();
    }

    public void build(RecipeOutput recipeOutput) {
        build(recipeOutput, result);
    }

    protected ItemStack resultStack() {
        return new ItemStack(result.value(), count);
    }
}