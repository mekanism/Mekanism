package mekanism.common.recipe.builder;

import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.jetbrains.annotations.Contract;

//TODO - 26.3: Rename methods in this and shaped recipe builder to match mojmap names for them in their builders
public class ExtendedShapelessRecipeBuilder extends BaseRecipeBuilder<CraftingBookCategory, CraftingBookInfo, ExtendedShapelessRecipeBuilder> {

    private final NonNullList<Ingredient> ingredients = NonNullList.create();

    private ExtendedShapelessRecipeBuilder(Holder<Item> result, int count) {
        super(CraftingBookInfo::new, CraftingBookCategory.MISC, result, count);
    }

    @Override
    @Contract("_ -> this")
    public ExtendedShapelessRecipeBuilder category(RecipeCategory category) {
        return super.category(category).category(RecipeBuilder.determineCraftingBookCategory(category));
    }

    public static ExtendedShapelessRecipeBuilder shapelessRecipe(HolderGetter<Item> lookup, ResourceKey<Item> result) {
        return shapelessRecipe(lookup, result, 1);
    }

    public static ExtendedShapelessRecipeBuilder shapelessRecipe(HolderGetter<Item> lookup, ResourceKey<Item> result, int count) {
        return shapelessRecipe(lookup.getOrThrow(result), count);
    }

    public static ExtendedShapelessRecipeBuilder shapelessRecipe(Holder<Item> result) {
        return shapelessRecipe(result, 1);
    }

    public static ExtendedShapelessRecipeBuilder shapelessRecipe(Holder<Item> result, int count) {
        return new ExtendedShapelessRecipeBuilder(result, count);
    }

    public ExtendedShapelessRecipeBuilder addIngredient(HolderGetter<Item> lookup, TagKey<Item> tag) {
        return addIngredient(lookup, tag, 1);
    }

    public ExtendedShapelessRecipeBuilder addIngredient(HolderGetter<Item> lookup, TagKey<Item> tag, int quantity) {
        return addIngredient(lookup.getOrThrow(tag), quantity);
    }

    public ExtendedShapelessRecipeBuilder addIngredient(HolderSet<Item> tag) {
        return addIngredient(tag, 1);
    }

    public ExtendedShapelessRecipeBuilder addIngredient(HolderSet<Item> tag, int quantity) {
        return addIngredient(Ingredient.of(tag), quantity);
    }

    public ExtendedShapelessRecipeBuilder addIngredient(HolderGetter<Item> lookup, ResourceKey<Item> id) {
        return addIngredient(lookup, id, 1);
    }

    public ExtendedShapelessRecipeBuilder addIngredient(HolderGetter<Item> lookup, ResourceKey<Item> id, int quantity) {
        return addIngredient(lookup.getOrThrow(id), quantity);
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
        return addIngredient(ingredient(item), quantity);
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
    protected ShapelessRecipe asRecipe() {
        return new ShapelessRecipe(
              commonInfo(),
              bookInfo(),
              this.result,
              this.ingredients
        );
    }
}