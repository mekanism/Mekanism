package mekanism.common.recipe.builder;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.core.Holder;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

@NothingNullByDefault
public class ExtendedShapedRecipeBuilder extends BaseRecipeBuilder<ExtendedShapedRecipeBuilder> {

    private final Char2ObjectMap<Ingredient> key = new Char2ObjectArrayMap<>(9);
    private final List<String> pattern = new ArrayList<>();
    private boolean showNotification = true;

    protected ExtendedShapedRecipeBuilder(Holder<Item> result, int count) {
        super(result, count);
    }

    public static ExtendedShapedRecipeBuilder shapedRecipe(BlockRegistryObject<?, ?> result) {
        return shapedRecipe(result, 1);
    }

    public static ExtendedShapedRecipeBuilder shapedRecipe(BlockRegistryObject<?, ?> result, int count) {
        return shapedRecipe(result.getItemHolder(), count);
    }

    public static ExtendedShapedRecipeBuilder shapedRecipe(Holder<Item> result) {
        return shapedRecipe(result, 1);
    }

    public static ExtendedShapedRecipeBuilder shapedRecipe(Holder<Item> result, int count) {
        return new ExtendedShapedRecipeBuilder(result, count);
    }

    public ExtendedShapedRecipeBuilder pattern(RecipePattern pattern) {
        if (!this.pattern.isEmpty()) {
            throw new IllegalArgumentException("Recipe pattern has already been set!");
        }
        this.pattern.add(pattern.row1);
        if (pattern.row2 != null) {
            this.pattern.add(pattern.row2);
            if (pattern.row3 != null) {
                this.pattern.add(pattern.row3);
            }
        }
        return this;
    }

    public ExtendedShapedRecipeBuilder key(char symbol, TagKey<Item> tag) {
        return key(symbol, Ingredient.of(tag));
    }

    public ExtendedShapedRecipeBuilder key(char symbol, Item item) {
        return key(symbol, Ingredient.of(item));
    }

    public ExtendedShapedRecipeBuilder key(char symbol, BlockRegistryObject<?, ?> block) {
        return key(symbol, block.getItemHolder());
    }

    public ExtendedShapedRecipeBuilder key(char symbol, Holder<Item> item) {
        return key(symbol, Ingredient.of(item.value()));
    }

    public ExtendedShapedRecipeBuilder key(char symbol, Ingredient ingredient) {
        if (key.containsKey(symbol)) {
            throw new IllegalArgumentException("Symbol '" + symbol + "' is already defined!");
        } else if (symbol == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        }
        key.put(symbol, ingredient);
        return this;
    }

    public ExtendedShapedRecipeBuilder showNotification(boolean show) {
        this.showNotification = show;
        return this;
    }

    @Override
    protected void validate(ResourceLocation id) {
        if (pattern.isEmpty()) {
            throw new IllegalStateException("No pattern is defined for shaped recipe " + id + "!");
        }
        CharSet set = new CharOpenHashSet(key.keySet());
        set.remove(' ');
        for (String s : pattern) {
            for (int i = 0; i < s.length(); ++i) {
                char c = s.charAt(i);
                if (!key.containsKey(c) && c != ' ') {
                    throw new IllegalStateException("Pattern in recipe " + id + " uses undefined symbol '" + c + "'");
                }
                set.remove(c);
            }
        }
        if (!set.isEmpty()) {
            throw new IllegalStateException("Ingredients are defined but not used in pattern for recipe " + id);
        } else if (pattern.size() == 1 && pattern.getFirst().length() == 1) {
            throw new IllegalStateException("Shaped recipe " + id + " only takes in a single item, and should probably be a shapeless recipe instead");
        }
    }

    @Override
    protected Recipe<?> asRecipe() {
        return wrapRecipe(new ShapedRecipe(
              Objects.requireNonNullElse(this.group, ""),
              RecipeBuilder.determineBookCategory(this.category),
              ShapedRecipePattern.of(this.key, this.pattern),
              resultStack(),
              this.showNotification
        ));
    }

    protected Recipe<?> wrapRecipe(ShapedRecipe recipe) {
        return recipe;
    }
}