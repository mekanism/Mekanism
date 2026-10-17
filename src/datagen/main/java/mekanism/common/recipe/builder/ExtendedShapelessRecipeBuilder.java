package mekanism.common.recipe.builder;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
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

public class ExtendedShapelessRecipeBuilder extends BaseRecipeBuilder<CraftingBookCategory, CraftingBookInfo, ExtendedShapelessRecipeBuilder> {

    private final List<Ingredient> ingredients = new ArrayList<>();

    private ExtendedShapelessRecipeBuilder(Holder<Item> result, int count) {
        super(CraftingBookInfo::new, CraftingBookCategory.MISC, result, count);
    }

    @Override
    @Contract("_ -> this")
    public ExtendedShapelessRecipeBuilder category(RecipeCategory category) {
        return super.category(category).category(RecipeBuilder.determineCraftingBookCategory(category));
    }

    public static ExtendedShapelessRecipeBuilder shapeless(HolderGetter<Item> lookup, ResourceKey<Item> result) {
        return shapeless(lookup, result, 1);
    }

    public static ExtendedShapelessRecipeBuilder shapeless(HolderGetter<Item> lookup, ResourceKey<Item> result, int count) {
        return shapeless(lookup.getOrThrow(result), count);
    }

    public static ExtendedShapelessRecipeBuilder shapeless(Holder<Item> result) {
        return shapeless(result, 1);
    }

    public static ExtendedShapelessRecipeBuilder shapeless(Holder<Item> result, int count) {
        return new ExtendedShapelessRecipeBuilder(result, count);
    }

    public ExtendedShapelessRecipeBuilder requires(HolderGetter<Item> lookup, TagKey<Item> tag) {
        return requires(lookup, tag, 1);
    }

    public ExtendedShapelessRecipeBuilder requires(HolderGetter<Item> lookup, TagKey<Item> tag, int quantity) {
        return requires(lookup.getOrThrow(tag), quantity);
    }

    public ExtendedShapelessRecipeBuilder requires(HolderSet<Item> tag) {
        return requires(tag, 1);
    }

    public ExtendedShapelessRecipeBuilder requires(HolderSet<Item> tag, int quantity) {
        return requires(Ingredient.of(tag), quantity);
    }

    public ExtendedShapelessRecipeBuilder requires(HolderGetter<Item> lookup, ResourceKey<Item> id) {
        return requires(lookup, id, 1);
    }

    public ExtendedShapelessRecipeBuilder requires(HolderGetter<Item> lookup, ResourceKey<Item> id, int quantity) {
        return requires(lookup.getOrThrow(id), quantity);
    }

    public ExtendedShapelessRecipeBuilder requires(BlockRegistryObject<?, ?> block) {
        return requires(block, 1);
    }

    public ExtendedShapelessRecipeBuilder requires(BlockRegistryObject<?, ?> block, int quantity) {
        return requires(block.getItemHolder(), quantity);
    }

    public ExtendedShapelessRecipeBuilder requires(Holder<Item> item) {
        return requires(item, 1);
    }

    public ExtendedShapelessRecipeBuilder requires(Holder<Item> item, int quantity) {
        return requires(ingredient(item), quantity);
    }

    public ExtendedShapelessRecipeBuilder requires(Ingredient ingredient) {
        return requires(ingredient, 1);
    }

    public ExtendedShapelessRecipeBuilder requires(Ingredient ingredient, int quantity) {
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