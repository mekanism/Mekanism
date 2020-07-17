package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.common.DataGenJsonConstants;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ExtendedSmithingRecipeBuilder extends BaseRecipeBuilder<ExtendedSmithingRecipeBuilder> {

    private final Ingredient ingredient;
    private final Ingredient upgradeIngredient;

    public ExtendedSmithingRecipeBuilder(Ingredient ingredient, Ingredient upgradeIngredient, IItemProvider result) {
        super(IRecipeSerializer.SMITHING, result, 1);
        this.ingredient = ingredient;
        this.upgradeIngredient = upgradeIngredient;
    }

    public static ExtendedSmithingRecipeBuilder smithing(IItemProvider ingredient, IItemProvider upgradeIngredient, IItemProvider result) {
        return smithing(Ingredient.fromItems(ingredient), Ingredient.fromItems(upgradeIngredient), result);
    }

    public static ExtendedSmithingRecipeBuilder smithing(Ingredient ingredient, Ingredient upgradeIngredient, IItemProvider result) {
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
        public void serialize(JsonObject json) {
            super.serialize(json);
            json.add(DataGenJsonConstants.BASE, ingredient.serialize());
            json.add(DataGenJsonConstants.ADDITION, upgradeIngredient.serialize());
        }
    }
}