package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.DataGenJsonConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

@NothingNullByDefault
public class ExtendedSmithingRecipeBuilder extends BaseRecipeBuilder<ExtendedSmithingRecipeBuilder> {

    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;

    private ExtendedSmithingRecipeBuilder(Ingredient template, Ingredient base, Ingredient addition, ItemLike result) {
        super(RecipeSerializer.SMITHING_TRANSFORM, result, 1);
        this.template = template;
        this.base = base;
        this.addition = addition;
    }

    public static ExtendedSmithingRecipeBuilder smithing(ItemLike template, ItemLike base, ItemLike addition, ItemLike result) {
        return smithing(Ingredient.of(template), Ingredient.of(base), Ingredient.of(addition), result);
    }

    public static ExtendedSmithingRecipeBuilder smithing(Ingredient template, Ingredient base, Ingredient addition, ItemLike result) {
        return new ExtendedSmithingRecipeBuilder(template, base, addition, result);
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
            json.add(DataGenJsonConstants.TEMPLATE, template.toJson());
            json.add(DataGenJsonConstants.BASE, base.toJson());
            json.add(DataGenJsonConstants.ADDITION, addition.toJson());
        }
    }
}