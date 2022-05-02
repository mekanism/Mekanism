package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.JsonConstants;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.common.DataGenJsonConstants;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class BaseRecipeBuilder<BUILDER extends BaseRecipeBuilder<BUILDER>> extends MekanismRecipeBuilder<BUILDER> {

    protected final Item result;
    protected final int count;
    private String group;

    protected BaseRecipeBuilder(RecipeSerializer<?> serializer, ItemLike result, int count) {
        super(serializer.getRegistryName());
        this.result = result.asItem();
        this.count = count;
    }

    @SuppressWarnings("unchecked")
    public BUILDER setGroup(String group) {
        this.group = group;
        return (BUILDER) this;
    }

    public void build(Consumer<FinishedRecipe> consumer) {
        build(consumer, result.getRegistryName());
    }

    protected abstract class BaseRecipeResult extends RecipeResult {

        protected BaseRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            if (group != null && !group.isEmpty()) {
                json.addProperty(DataGenJsonConstants.GROUP, group);
            }
            serializeResult(json);
        }

        protected void serializeResult(JsonObject json) {
            JsonObject jsonResult = new JsonObject();
            jsonResult.addProperty(JsonConstants.ITEM, result.getRegistryName().toString());
            if (count > 1) {
                jsonResult.addProperty(JsonConstants.COUNT, count);
            }
            json.add(DataGenJsonConstants.RESULT, jsonResult);
        }
    }
}