package mekanism.common.recipe.builder;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe.BookInfo;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;

public class ExtendedSmithingRecipeBuilder extends BaseRecipeBuilder<Void, BookInfo<Void>, ExtendedSmithingRecipeBuilder> {

    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;

    @SuppressWarnings("DataFlowIssue")//Smithing recipes don't have recipe books
    private ExtendedSmithingRecipeBuilder(Ingredient template, Ingredient base, Ingredient addition, Holder<Item> result) {
        super(null, null, result, 1);
        this.template = template;
        this.base = base;
        this.addition = addition;
    }

    @Override
    protected BookInfo<Void> bookInfo() {
        throw new UnsupportedOperationException("Smithing recipes don't have recipe books");
    }

    public static ExtendedSmithingRecipeBuilder smithing(HolderGetter<Item> lookup, ResourceKey<Item> template, Holder<Item> base, ResourceKey<Item> addition, Holder<Item> result) {
        return smithing(ingredient(lookup, template), ingredient(base), ingredient(lookup, addition), result);
    }

    public static ExtendedSmithingRecipeBuilder smithing(Ingredient template, Ingredient base, Ingredient addition, Holder<Item> result) {
        return new ExtendedSmithingRecipeBuilder(template, base, addition, result);
    }

    @Override
    protected SmithingRecipe asRecipe() {
        return new SmithingTransformRecipe(
              commonInfo(),
              Optional.of(template),
              base,
              Optional.of(addition),
              this.result
        );
    }
}