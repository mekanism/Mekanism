package mekanism.api.datagen.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.JsonConstants;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base recipe builder that declares various common methods between our different builders.
 */
@NothingNullByDefault
public abstract class MekanismRecipeBuilder<BUILDER extends MekanismRecipeBuilder<BUILDER>> {

    protected static ResourceLocation mekSerializer(String name) {
        return new ResourceLocation(MekanismAPI.MEKANISM_MODID, name);
    }

    protected final List<ICondition> conditions = new ArrayList<>();
    protected final Advancement.Builder advancementBuilder = Advancement.Builder.advancement();
    protected final ResourceLocation serializerName;

    protected MekanismRecipeBuilder(ResourceLocation serializerName) {
        this.serializerName = serializerName;
        //TODO: We may also want to validate inputs, currently we are not validating our input ingredients as being valid, and are just validating the other parameters
    }

    /**
     * Adds a criterion to this recipe.
     *
     * @param criterion Criterion to add.
     */
    public BUILDER addCriterion(RecipeCriterion criterion) {
        return addCriterion(criterion.name(), criterion.criterion());
    }

    /**
     * Adds a criterion to this recipe.
     *
     * @param name      Name of the criterion.
     * @param criterion Criterion to add.
     */
    public BUILDER addCriterion(String name, CriterionTriggerInstance criterion) {
        advancementBuilder.addCriterion(name, criterion);
        return (BUILDER) this;
    }

    /**
     * Adds a condition to this recipe.
     *
     * @param condition Condition to add.
     */
    public BUILDER addCondition(ICondition condition) {
        conditions.add(condition);
        return (BUILDER) this;
    }

    /**
     * Checks if this recipe has any criteria.
     *
     * @return {@code true} if this recipe has any criteria.
     */
    protected boolean hasCriteria() {
        return !advancementBuilder.getCriteria().isEmpty();
    }

    /**
     * Gets a recipe result object.
     *
     * @param id ID of the recipe being built.
     */
    protected abstract RecipeResult getResult(ResourceLocation id);

    /**
     * Performs any extra validation.
     *
     * @param id ID of the recipe validation is being performed on.
     */
    protected void validate(ResourceLocation id) {
    }

    /**
     * Builds this recipe.
     *
     * @param consumer Finished Recipe Consumer.
     * @param id       Name of the recipe being built.
     */
    public void build(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        validate(id);
        if (hasCriteria()) {
            //If there is a way to "unlock" this recipe then add an advancement with the criteria
            advancementBuilder.parent(new ResourceLocation("recipes/root")).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                  .rewards(AdvancementRewards.Builder.recipe(id)).requirements(RequirementsStrategy.OR);
        }
        consumer.accept(getResult(id));
    }

    /**
     * Builds this recipe basing the name on the output item.
     *
     * @param consumer Finished Recipe Consumer.
     * @param output       Output to base the recipe name off of.
     */
    protected void build(Consumer<FinishedRecipe> consumer, ItemLike output) {
        ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(output.asItem());
        if (registryName == null) {
            throw new IllegalStateException("Could not retrieve registry name for output.");
        }
        build(consumer, registryName);
    }

    /**
     * Base recipe result.
     */
    protected abstract class RecipeResult implements FinishedRecipe {

        private final ResourceLocation id;

        public RecipeResult(ResourceLocation id) {
            this.id = id;
        }

        @Override
        public JsonObject serializeRecipe() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(JsonConstants.TYPE, serializerName.toString());
            if (!conditions.isEmpty()) {
                JsonArray conditionsArray = new JsonArray();
                for (ICondition condition : conditions) {
                    conditionsArray.add(CraftingHelper.serialize(condition));
                }
                jsonObject.add(JsonConstants.CONDITIONS, conditionsArray);
            }
            this.serializeRecipeData(jsonObject);
            return jsonObject;
        }

        @NotNull
        @Override
        public RecipeSerializer<?> getType() {
            //Note: This may be null if something is screwed up but this method isn't actually used, so it shouldn't matter
            // and in fact it will probably be null if only the API is included. But again, as we manually just use
            // the serializer's name this should not affect us
            return ForgeRegistries.RECIPE_SERIALIZERS.getValue(serializerName);
        }

        @NotNull
        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return hasCriteria() ? advancementBuilder.serializeToJson() : null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath());
        }
    }
}