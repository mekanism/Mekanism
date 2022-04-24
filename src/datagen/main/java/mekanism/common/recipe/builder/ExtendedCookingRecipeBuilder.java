package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.JsonConstants;
import mekanism.common.DataGenJsonConstants;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.level.ItemLike;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExtendedCookingRecipeBuilder extends BaseRecipeBuilder<ExtendedCookingRecipeBuilder> {

    private final Ingredient ingredient;
    private final int cookingTime;
    private float experience;

    private ExtendedCookingRecipeBuilder(SimpleCookingSerializer<?> serializer, ItemLike result, int count, Ingredient ingredient, int cookingTime) {
        super(serializer, result, count);
        this.ingredient = ingredient;
        this.cookingTime = cookingTime;
    }

    public static ExtendedCookingRecipeBuilder blasting(ItemLike result, Ingredient ingredient, int cookingTime) {
        return blasting(result, 1, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder blasting(ItemLike result, int count, Ingredient ingredient, int cookingTime) {
        return new ExtendedCookingRecipeBuilder(RecipeSerializer.BLASTING_RECIPE, result, count, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder campfire(ItemLike result, Ingredient ingredient, int cookingTime) {
        return campfire(result, 1, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder campfire(ItemLike result, int count, Ingredient ingredient, int cookingTime) {
        return new ExtendedCookingRecipeBuilder(RecipeSerializer.CAMPFIRE_COOKING_RECIPE, result, count, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder smelting(ItemLike result, Ingredient ingredient, int cookingTime) {
        return smelting(result, 1, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder smelting(ItemLike result, int count, Ingredient ingredient, int cookingTime) {
        return new ExtendedCookingRecipeBuilder(RecipeSerializer.SMELTING_RECIPE, result, count, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder smoking(ItemLike result, Ingredient ingredient, int cookingTime) {
        return smoking(result, 1, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder smoking(ItemLike result, int count, Ingredient ingredient, int cookingTime) {
        return new ExtendedCookingRecipeBuilder(RecipeSerializer.SMOKING_RECIPE, result, count, ingredient, cookingTime);
    }

    public ExtendedCookingRecipeBuilder experience(float experience) {
        if (experience < 0) {
            throw new IllegalArgumentException("Experience cannot be negative.");
        }
        this.experience = experience;
        return this;
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
            json.addProperty(DataGenJsonConstants.COOKING_TIME, cookingTime);
            if (experience > 0) {
                json.addProperty(DataGenJsonConstants.EXPERIENCE, experience);
            }
        }

        @Override
        protected void serializeResult(JsonObject json) {
            if (count == 1) {
                json.addProperty(DataGenJsonConstants.RESULT, result.getRegistryName().toString());
            } else {
                super.serializeResult(json);
            }
        }
    }
}