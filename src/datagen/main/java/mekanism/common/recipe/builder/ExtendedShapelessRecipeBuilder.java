package mekanism.common.recipe.builder;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;

@NothingNullByDefault
public class ExtendedShapelessRecipeBuilder extends BaseRecipeBuilder<ExtendedShapelessRecipeBuilder> {

    private final NonNullList<Ingredient> ingredients = NonNullList.create();

    private ExtendedShapelessRecipeBuilder(Holder<Item> result, int count) {
        super(result, count);
    }

    public static ExtendedShapelessRecipeBuilder shapelessRecipe(Holder<Item> result) {
        return shapelessRecipe(result, 1);
    }

    public static ExtendedShapelessRecipeBuilder shapelessRecipe(Holder<Item> result, int count) {
        return new ExtendedShapelessRecipeBuilder(result, count);
    }

    public ExtendedShapelessRecipeBuilder addIngredient(TagKey<Item> tag) {
        return addIngredient(tag, 1);
    }

    public ExtendedShapelessRecipeBuilder addIngredient(TagKey<Item> tag, int quantity) {
        return addIngredient(Ingredient.of(tag), quantity);
    }

    public ExtendedShapelessRecipeBuilder addIngredient(Item item) {
        return addIngredient(item, 1);
    }

    public ExtendedShapelessRecipeBuilder addIngredient(Item item, int quantity) {
        return addIngredient(Ingredient.of(item), quantity);
    }

    public ExtendedShapelessRecipeBuilder addIngredient(BlockRegistryObject<?, ?> block) {
        return addIngredient(block, 1);
    }

    public ExtendedShapelessRecipeBuilder addIngredient(BlockRegistryObject<?, ?> block, int quantity) {
        return addIngredient(block.getItemHolder(), quantity);
    }

    public ExtendedShapelessRecipeBuilder addIngredient(Holder<Item> item) {
        return addIngredient(item, 1);
    }

    public ExtendedShapelessRecipeBuilder addIngredient(Holder<Item> item, int quantity) {
        return addIngredient(Ingredient.of(item.value()), quantity);
    }

    public ExtendedShapelessRecipeBuilder addIngredient(Ingredient ingredient) {
        return addIngredient(ingredient, 1);
    }

    public ExtendedShapelessRecipeBuilder addIngredient(Ingredient ingredient, int quantity) {
        for (int i = 0; i < quantity; ++i) {
            ingredients.add(ingredient);
        }
        return this;
    }

    @Override
    protected void validate(ResourceLocation id) {
        if (ingredients.isEmpty()) {
            throw new IllegalStateException("Shapeless recipe '" + id + "' must have at least one ingredient!");
        }
    }

    @Override
    protected ShapelessRecipe asRecipe() {
        return new ShapelessRecipe(
              Objects.requireNonNullElse(this.group, ""),
              RecipeBuilder.determineBookCategory(this.category),
              resultStack(),
              this.ingredients
        );
    }
}