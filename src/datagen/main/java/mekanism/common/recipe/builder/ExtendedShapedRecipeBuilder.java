package mekanism.common.recipe.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.chars.Char2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.common.DataGenJsonConstants;
import mekanism.common.recipe.pattern.RecipePattern;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExtendedShapedRecipeBuilder extends BaseRecipeBuilder<ExtendedShapedRecipeBuilder> {

    private final Char2ObjectMap<Ingredient> key = new Char2ObjectLinkedOpenHashMap<>();
    private final List<String> pattern = new ArrayList<>();

    protected ExtendedShapedRecipeBuilder(IRecipeSerializer<?> serializer, IItemProvider result, int count) {
        super(serializer, result, count);
    }

    private ExtendedShapedRecipeBuilder(IItemProvider result, int count) {
        this(IRecipeSerializer.CRAFTING_SHAPED, result, count);
    }

    public static ExtendedShapedRecipeBuilder shapedRecipe(IItemProvider result) {
        return shapedRecipe(result, 1);
    }

    public static ExtendedShapedRecipeBuilder shapedRecipe(IItemProvider result, int count) {
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

    public ExtendedShapedRecipeBuilder key(char symbol, ITag<Item> tag) {
        return key(symbol, Ingredient.fromTag(tag));
    }

    public ExtendedShapedRecipeBuilder key(char symbol, IItemProvider item) {
        return key(symbol, Ingredient.fromItems(item));
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
        public void serialize(JsonObject json) {
            super.serialize(json);
            JsonArray jsonPattern = new JsonArray();
            for (String s : pattern) {
                jsonPattern.add(s);
            }
            json.add(DataGenJsonConstants.PATTERN, jsonPattern);
            JsonObject jsonobject = new JsonObject();
            for (Char2ObjectMap.Entry<Ingredient> entry : key.char2ObjectEntrySet()) {
                jsonobject.add(String.valueOf(entry.getCharKey()), entry.getValue().serialize());
            }
            json.add(DataGenJsonConstants.KEY, jsonobject);
        }
    }
}