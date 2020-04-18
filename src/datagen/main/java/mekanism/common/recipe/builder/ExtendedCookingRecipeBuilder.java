package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.JsonConstants;
import mekanism.common.DataGenJsonConstants;
import net.minecraft.item.crafting.CookingRecipeSerializer;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExtendedCookingRecipeBuilder extends BaseRecipeBuilder<ExtendedCookingRecipeBuilder> {

    private final Ingredient ingredient;
    private final int cookingTime;
    private float experience;

    private ExtendedCookingRecipeBuilder(CookingRecipeSerializer<?> serializer, IItemProvider result, int count, Ingredient ingredient, int cookingTime) {
        super(serializer, result, count);
        this.ingredient = ingredient;
        this.cookingTime = cookingTime;
    }

    public static ExtendedCookingRecipeBuilder blasting(IItemProvider result, Ingredient ingredient, int cookingTime) {
        return blasting(result, 1, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder blasting(IItemProvider result, int count, Ingredient ingredient, int cookingTime) {
        return new ExtendedCookingRecipeBuilder(IRecipeSerializer.BLASTING, result, count, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder campfire(IItemProvider result, Ingredient ingredient, int cookingTime) {
        return campfire(result, 1, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder campfire(IItemProvider result, int count, Ingredient ingredient, int cookingTime) {
        return new ExtendedCookingRecipeBuilder(IRecipeSerializer.CAMPFIRE_COOKING, result, count, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder smelting(IItemProvider result, Ingredient ingredient, int cookingTime) {
        return smelting(result, 1, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder smelting(IItemProvider result, int count, Ingredient ingredient, int cookingTime) {
        return new ExtendedCookingRecipeBuilder(IRecipeSerializer.SMELTING, result, count, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder smoking(IItemProvider result, Ingredient ingredient, int cookingTime) {
        return smoking(result, 1, ingredient, cookingTime);
    }

    public static ExtendedCookingRecipeBuilder smoking(IItemProvider result, int count, Ingredient ingredient, int cookingTime) {
        return new ExtendedCookingRecipeBuilder(IRecipeSerializer.SMOKING, result, count, ingredient, cookingTime);
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
        public void serialize(JsonObject json) {
            super.serialize(json);
            json.add(JsonConstants.INGREDIENT, ingredient.serialize());
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