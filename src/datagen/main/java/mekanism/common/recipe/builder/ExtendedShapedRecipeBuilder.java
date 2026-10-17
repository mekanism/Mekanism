package mekanism.common.recipe.builder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.jetbrains.annotations.Contract;

public class ExtendedShapedRecipeBuilder extends BaseRecipeBuilder<CraftingBookCategory, CraftingBookInfo, ExtendedShapedRecipeBuilder> {

    private final List<String> rows = new ArrayList<>();
    private final Map<Character, Ingredient> key = new LinkedHashMap<>();

    protected ExtendedShapedRecipeBuilder(Holder<Item> result, int count) {
        super(CraftingBookInfo::new, CraftingBookCategory.MISC, result, count);
    }

    protected ExtendedShapedRecipeBuilder(ItemStackTemplate result) {
        super(CraftingBookInfo::new, CraftingBookCategory.MISC, result);
    }

    @Override
    @Contract("_ -> this")
    public ExtendedShapedRecipeBuilder category(RecipeCategory category) {
        return super.category(category).category(RecipeBuilder.determineCraftingBookCategory(category));
    }

    public static ExtendedShapedRecipeBuilder shaped(BlockRegistryObject<?, ?> result) {
        return shaped(result, 1);
    }

    public static ExtendedShapedRecipeBuilder shaped(BlockRegistryObject<?, ?> result, int count) {
        return shaped(result.getItemHolder(), count);
    }

    public static ExtendedShapedRecipeBuilder shaped(HolderGetter<Item> lookup, ResourceKey<Item> result) {
        return shaped(lookup, result, 1);
    }

    public static ExtendedShapedRecipeBuilder shaped(HolderGetter<Item> lookup, ResourceKey<Item> result, int count) {
        return shaped(lookup.getOrThrow(result), count);
    }

    public static ExtendedShapedRecipeBuilder shaped(Holder<Item> result) {
        return shaped(result, 1);
    }

    public static ExtendedShapedRecipeBuilder shaped(Holder<Item> result, int count) {
        return new ExtendedShapedRecipeBuilder(result, count);
    }

    public static ExtendedShapedRecipeBuilder shaped(ItemStackTemplate result) {
        return new ExtendedShapedRecipeBuilder(result);
    }

    public ExtendedShapedRecipeBuilder pattern(RecipePattern pattern) {
        if (!this.rows.isEmpty()) {
            throw new IllegalArgumentException("Recipe pattern has already been set!");
        }
        this.rows.add(pattern.row1);
        if (pattern.row2 != null) {
            this.rows.add(pattern.row2);
            if (pattern.row3 != null) {
                this.rows.add(pattern.row3);
            }
        }
        return this;
    }

    public ExtendedShapedRecipeBuilder define(Character symbol, HolderGetter<Item> lookup, BlockItemId id) {
        return define(symbol, lookup, id.item());
    }

    public ExtendedShapedRecipeBuilder define(Character symbol, HolderGetter<Item> lookup, ResourceKey<Item> id) {
        return define(symbol, ingredient(lookup, id));
    }

    public ExtendedShapedRecipeBuilder define(Character symbol, HolderGetter<Item> lookup, BlockItemTagId id) {
        return define(symbol, lookup, id.item());
    }

    public ExtendedShapedRecipeBuilder define(Character symbol, HolderGetter<Item> lookup, TagKey<Item> tag) {
        return define(symbol, lookup.getOrThrow(tag));
    }

    public ExtendedShapedRecipeBuilder define(Character symbol, HolderSet<Item> tag) {
        return define(symbol, Ingredient.of(tag));
    }

    public ExtendedShapedRecipeBuilder define(Character symbol, BlockRegistryObject<?, ?> block) {
        return define(symbol, block.getItemHolder());
    }

    public ExtendedShapedRecipeBuilder define(Character symbol, Holder<Item> item) {
        return define(symbol, ingredient(item));
    }

    public ExtendedShapedRecipeBuilder define(Character symbol, Ingredient ingredient) {
        if (key.containsKey(symbol)) {
            throw new IllegalArgumentException("Symbol '" + symbol + "' is already defined!");
        } else if (symbol == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        }
        key.put(symbol, ingredient);
        return this;
    }

    @Override
    protected Recipe<?> asRecipe() {
        return wrapRecipe(
              commonInfo(),
              bookInfo(),
              ShapedRecipePattern.of(this.key, this.rows),
              this.result
        );
    }

    protected Recipe<?> wrapRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, ItemStackTemplate result) {
        return new ShapedRecipe(commonInfo, bookInfo, pattern, result);
    }
}