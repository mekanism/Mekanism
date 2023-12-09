package mekanism.common.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.level.ItemLike;

@NothingNullByDefault
public class ExtendedSmithingRecipeBuilder extends BaseRecipeBuilder<ExtendedSmithingRecipeBuilder> {

    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;

    private ExtendedSmithingRecipeBuilder(Ingredient template, Ingredient base, Ingredient addition, ItemLike result) {
        super(result, 1);
        this.template = template;
        this.base = base;
        this.addition = addition;
    }

    public static ExtendedSmithingRecipeBuilder smithing(ItemLike template, ItemLike base, ItemLike addition, ItemLike result) {
        return smithing(Ingredient.of(template), Ingredient.of(base), Ingredient.of(addition), result);
    }

    public static ExtendedSmithingRecipeBuilder smithing(Ingredient template, Ingredient base, Ingredient addition, ItemLike result) {
        return new ExtendedSmithingRecipeBuilder(template, base, addition, result);
    }

    @Override
    protected SmithingRecipe asRecipe() {
        return new SmithingTransformRecipe(template, base, addition, new ItemStack(result));
    }
}