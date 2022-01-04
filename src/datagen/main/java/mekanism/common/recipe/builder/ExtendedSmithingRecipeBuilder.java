package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.common.DataGenJsonConstants;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExtendedSmithingRecipeBuilder extends BaseRecipeBuilder<ExtendedSmithingRecipeBuilder> {

    private final Ingredient ingredient;
    private final Ingredient upgradeIngredient;

    public ExtendedSmithingRecipeBuilder(Ingredient ingredient, Ingredient upgradeIngredient, ItemLike result) {
        super(RecipeSerializer.SMITHING, result, 1);
        this.ingredient = ingredient;
        this.upgradeIngredient = upgradeIngredient;
    }

    public static ExtendedSmithingRecipeBuilder smithing(ItemLike ingredient, ItemLike upgradeIngredient, ItemLike result) {
        return smithing(Ingredient.of(ingredient), Ingredient.of(upgradeIngredient), result);
    }

    public static ExtendedSmithingRecipeBuilder smithing(Ingredient ingredient, Ingredient upgradeIngredient, ItemLike result) {
        return new ExtendedSmithingRecipeBuilder(ingredient, upgradeIngredient, result);
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
            json.add(DataGenJsonConstants.BASE, ingredient.toJson());
            json.add(DataGenJsonConstants.ADDITION, upgradeIngredient.toJson());
        }
    }
}