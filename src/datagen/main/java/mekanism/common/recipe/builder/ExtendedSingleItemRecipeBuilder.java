package mekanism.common.recipe.builder;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;

@NothingNullByDefault
public class ExtendedSingleItemRecipeBuilder extends BaseRecipeBuilder<ExtendedSingleItemRecipeBuilder> {

    private final SingleItemRecipe.Factory<?> factory;
    private final Ingredient ingredient;

    public ExtendedSingleItemRecipeBuilder(Ingredient ingredient, Holder<Item> result, int count, SingleItemRecipe.Factory<?> factory) {
        super(result, count);
        this.ingredient = ingredient;
        this.factory = factory;
    }

    public static ExtendedSingleItemRecipeBuilder stonecutting(Ingredient ingredient, Holder<Item> result) {
        return stonecutting(ingredient, result, 1);
    }

    public static ExtendedSingleItemRecipeBuilder stonecutting(Ingredient ingredient, Holder<Item> result, int count) {
        return new ExtendedSingleItemRecipeBuilder(ingredient, result, count, StonecutterRecipe::new);
    }

    @Override
    protected SingleItemRecipe asRecipe() {
        return this.factory.create(
              Objects.requireNonNullElse(this.group, ""),
              this.ingredient,
              resultStack()
        );
    }
}