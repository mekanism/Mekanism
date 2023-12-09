package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.common.DataGenJsonConstants;
import mekanism.common.util.RegistryUtils;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class BaseRecipeBuilder<BUILDER extends BaseRecipeBuilder<BUILDER>> extends MekanismRecipeBuilder<BUILDER> {

    protected final Item result;
    protected final int count;
    protected RecipeCategory category = RecipeCategory.MISC;
    @Nullable
    protected String group;

    protected BaseRecipeBuilder(ItemLike result, int count) {
        this.result = result.asItem();
        this.count = count;
    }

    @SuppressWarnings("unchecked")
    private BUILDER self() {
        return (BUILDER) this;
    }

    public BUILDER group(String group) {
        this.group = group;
        return self();
    }

    public BUILDER category(RecipeCategory category) {
        this.category = category;
        return self();
    }

    public void build(RecipeOutput recipeOutput) {
        build(recipeOutput, result);
    }

    protected abstract class BaseRecipeResult extends RecipeResult {

        protected BaseRecipeResult(ResourceLocation id, @Nullable AdvancementHolder advancementHolder) {
            super(id, advancementHolder);
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            if (group != null && !group.isEmpty()) {
                json.addProperty(DataGenJsonConstants.GROUP, group);
            }
            serializeResult(json);
        }

        protected void serializeResult(JsonObject json) {
            StringRepresentable category = determineBookCategory();
            if (category != CraftingBookCategory.MISC && category != CookingBookCategory.MISC) {
                json.addProperty(DataGenJsonConstants.CATEGORY, category.getSerializedName());
            }
            JsonObject jsonResult = new JsonObject();
            jsonResult.addProperty(JsonConstants.ITEM, RegistryUtils.getName(result).toString());
            if (count > 1) {
                jsonResult.addProperty(JsonConstants.COUNT, count);
            }
            json.add(DataGenJsonConstants.RESULT, jsonResult);
        }
    }
}