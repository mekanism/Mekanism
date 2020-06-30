package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.JsonConstants;
import mekanism.common.DataGenJsonConstants;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExtendedSingleItemRecipeBuilder extends BaseRecipeBuilder<ExtendedSingleItemRecipeBuilder> {

    private final Ingredient ingredient;

    public ExtendedSingleItemRecipeBuilder(IRecipeSerializer<?> serializer, Ingredient ingredient, IItemProvider result, int count) {
        super(serializer, result, count);
        this.ingredient = ingredient;
    }

    public static ExtendedSingleItemRecipeBuilder stonecutting(Ingredient ingredient, IItemProvider result) {
        return stonecutting(ingredient, result, 1);
    }

    public static ExtendedSingleItemRecipeBuilder stonecutting(Ingredient ingredient, IItemProvider result, int count) {
        return new ExtendedSingleItemRecipeBuilder(IRecipeSerializer.STONECUTTING, ingredient, result, count);
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
            json.add(JsonConstants.INGREDIENT, ingredient.serialize());
        }

        @Override
        protected void serializeResult(JsonObject json) {
            json.addProperty(DataGenJsonConstants.RESULT, result.getRegistryName().toString());
            json.addProperty(JsonConstants.COUNT, count);
        }
    }
}