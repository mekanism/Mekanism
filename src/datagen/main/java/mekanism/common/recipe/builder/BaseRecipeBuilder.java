package mekanism.common.recipe.builder;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.common.DataGenJsonConstants;
import mekanism.common.util.RegistryUtils;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
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
    private RecipeCategory category = RecipeCategory.MISC;
    @Nullable
    private String group;

    protected BaseRecipeBuilder(RecipeSerializer<?> serializer, ItemLike result, int count) {
        super(RegistryUtils.getName(serializer));
        this.result = result.asItem();
        this.count = count;
    }

    @SuppressWarnings("unchecked")
    private BUILDER getThis() {
        return (BUILDER) this;
    }

    public BUILDER group(String group) {
        this.group = group;
        return getThis();
    }

    public BUILDER category(RecipeCategory category) {
        this.category = category;
        return getThis();
    }

    public void build(Consumer<FinishedRecipe> consumer) {
        build(consumer, result);
    }

    //Copied from CraftingRecipeBuilder#determineBookCategory
    protected StringRepresentable determineBookCategory() {
        return switch (category) {
            case BUILDING_BLOCKS -> CraftingBookCategory.BUILDING;
            case TOOLS, COMBAT -> CraftingBookCategory.EQUIPMENT;
            case REDSTONE -> CraftingBookCategory.REDSTONE;
            default -> CraftingBookCategory.MISC;
        };
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