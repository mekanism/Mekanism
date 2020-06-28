package mekanism.common.recipe.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.common.DataGenJsonConstants;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExtendedShapelessRecipeBuilder extends BaseRecipeBuilder<ExtendedShapelessRecipeBuilder> {

    private final List<Ingredient> ingredients = new ArrayList<>();

    private ExtendedShapelessRecipeBuilder(IItemProvider result, int count) {
        super(IRecipeSerializer.CRAFTING_SHAPELESS, result, count);
    }

    public static ExtendedShapelessRecipeBuilder shapelessRecipe(IItemProvider result) {
        return shapelessRecipe(result, 1);
    }

    public static ExtendedShapelessRecipeBuilder shapelessRecipe(IItemProvider result, int count) {
        return new ExtendedShapelessRecipeBuilder(result, count);
    }

    public ExtendedShapelessRecipeBuilder addIngredient(ITag<Item> tag) {
        return addIngredient(Ingredient.fromTag(tag));
    }

    public ExtendedShapelessRecipeBuilder addIngredient(IItemProvider item) {
        return addIngredient(item, 1);
    }

    public ExtendedShapelessRecipeBuilder addIngredient(IItemProvider item, int quantity) {
        for (int i = 0; i < quantity; ++i) {
            addIngredient(Ingredient.fromItems(item));
        }
        return this;
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
    protected RecipeResult getResult(ResourceLocation id) {
        return new Result(id);
    }

    public class Result extends BaseRecipeResult {

        public Result(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serialize(JsonObject json) {
            super.serialize(json);
            JsonArray jsonIngredients = new JsonArray();
            for (Ingredient ingredient : ingredients) {
                jsonIngredients.add(ingredient.serialize());
            }
            json.add(DataGenJsonConstants.INGREDIENTS, jsonIngredients);
        }
    }
}