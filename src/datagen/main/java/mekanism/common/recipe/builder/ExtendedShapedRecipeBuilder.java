package mekanism.common.recipe.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.common.DataGenJsonConstants;
import mekanism.common.recipe.pattern.RecipePattern;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExtendedShapedRecipeBuilder extends BaseRecipeBuilder<ExtendedShapedRecipeBuilder> {

    private final Char2ObjectMap<Ingredient> key = new Char2ObjectArrayMap<>(9);
    private final List<String> pattern = new ArrayList<>();

    protected ExtendedShapedRecipeBuilder(RecipeSerializer<?> serializer, ItemLike result, int count) {
        super(serializer, result, count);
    }

    private ExtendedShapedRecipeBuilder(ItemLike result, int count) {
        this(RecipeSerializer.SHAPED_RECIPE, result, count);
    }

    public static ExtendedShapedRecipeBuilder shapedRecipe(ItemLike result) {
        return shapedRecipe(result, 1);
    }

    public static ExtendedShapedRecipeBuilder shapedRecipe(ItemLike result, int count) {
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

    public ExtendedShapedRecipeBuilder key(char symbol, ItemLike item) {
        return key(symbol, Ingredient.of(item));
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
        } else if (pattern.size() == 1 && pattern.get(0).length() == 1) {
            throw new IllegalStateException("Shaped recipe " + id + " only takes in a single item, and should probably be a shapeless recipe instead");
        }
    }

    @Override
    protected RecipeResult getResult(ResourceLocation id) {
        return new Result(id);
    }

    public class Result extends BaseRecipeResult {

        protected Result(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            super.serializeRecipeData(json);
            JsonArray jsonPattern = new JsonArray();
            for (String s : pattern) {
                jsonPattern.add(s);
            }
            json.add(DataGenJsonConstants.PATTERN, jsonPattern);
            JsonObject jsonobject = new JsonObject();
            for (Char2ObjectMap.Entry<Ingredient> entry : key.char2ObjectEntrySet()) {
                jsonobject.add(String.valueOf(entry.getCharKey()), entry.getValue().toJson());
            }
            json.add(DataGenJsonConstants.KEY, jsonobject);
        }
    }
}