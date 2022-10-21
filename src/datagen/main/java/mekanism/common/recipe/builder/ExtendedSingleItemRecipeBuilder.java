package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.DataGenJsonConstants;
import mekanism.common.util.RegistryUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

@NothingNullByDefault
public class ExtendedSingleItemRecipeBuilder extends BaseRecipeBuilder<ExtendedSingleItemRecipeBuilder> {

    private final Ingredient ingredient;

    public ExtendedSingleItemRecipeBuilder(RecipeSerializer<?> serializer, Ingredient ingredient, ItemLike result, int count) {
        super(serializer, result, count);
        this.ingredient = ingredient;
    }

    public static ExtendedSingleItemRecipeBuilder stonecutting(Ingredient ingredient, ItemLike result) {
        return stonecutting(ingredient, result, 1);
    }

    public static ExtendedSingleItemRecipeBuilder stonecutting(Ingredient ingredient, ItemLike result, int count) {
        return new ExtendedSingleItemRecipeBuilder(RecipeSerializer.STONECUTTER, ingredient, result, count);
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
        public void serializeRecipeData(JsonObject json) {
            super.serializeRecipeData(json);
            json.add(JsonConstants.INGREDIENT, ingredient.toJson());
        }

        @Override
        protected void serializeResult(JsonObject json) {
            json.addProperty(DataGenJsonConstants.RESULT, RegistryUtils.getName(result).toString());
            json.addProperty(JsonConstants.COUNT, count);
        }
    }
}