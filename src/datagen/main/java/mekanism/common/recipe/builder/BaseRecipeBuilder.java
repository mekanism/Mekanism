package mekanism.common.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class BaseRecipeBuilder<BUILDER extends BaseRecipeBuilder<BUILDER>> extends MekanismRecipeBuilder<BUILDER> {

    protected final Item result;
    protected final int count;
    protected RecipeCategory category = RecipeCategory.MISC;
    @Nullable
    protected String group;

    protected BaseRecipeBuilder(ItemLike result, int count) {
        this.result = result.asItem();
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
}