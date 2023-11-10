package mekanism.api.datagen.recipe;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import mekanism.api.JsonConstants;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base recipe builder that declares various common methods between our different builders.
 */
@NothingNullByDefault
@SuppressWarnings("UnusedReturnValue")
public abstract class MekanismRecipeBuilder<BUILDER extends MekanismRecipeBuilder<BUILDER>> {

    protected static ResourceLocation mekSerializer(String name) {
        return new ResourceLocation(MekanismAPI.MEKANISM_MODID, name);
    }

    protected final List<ICondition> conditions = new ArrayList<>();
    protected final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
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
    public BUILDER unlockedBy(RecipeCriterion criterion) {
        return unlockedBy(criterion.name(), criterion.criterion());
    }

    /**
     * Adds a criterion to this recipe.
     *
     * @param name      Name of the criterion.
     * @param criterion Criterion to add.
     */
    @SuppressWarnings("unchecked")
    public BUILDER unlockedBy(String name, Criterion<?> criterion) {
        criteria.put(name, criterion);
        return (BUILDER) this;
    }

    /**
     * Adds a condition to this recipe.
     *
     * @param condition Condition to add.
     */
    @SuppressWarnings("unchecked")
    public BUILDER addCondition(ICondition condition) {
        conditions.add(condition);
        return (BUILDER) this;
    }

    /**
     * Gets a recipe result object.
     *
     * @param id ID of the recipe being built.
     */
    protected abstract RecipeResult getResult(ResourceLocation id, @Nullable AdvancementHolder advancementHolder);

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
     * @param recipeOutput Finished Recipe Consumer.
     * @param id       Name of the recipe being built.
     */
    public void build(RecipeOutput recipeOutput, ResourceLocation id) {
        validate(id);
        AdvancementHolder advancementHolder = null;
        if (!this.criteria.isEmpty()) {
            Advancement.Builder advancementBuilder = recipeOutput.advancement()
                  .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                  .rewards(AdvancementRewards.Builder.recipe(id))
                  .requirements(AdvancementRequirements.Strategy.OR);
            //If there is a way to "unlock" this recipe then add an advancement with the criteria
            this.criteria.forEach(advancementBuilder::addCriterion);
            advancementHolder = advancementBuilder.build(new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath()));
        }
        recipeOutput.accept(getResult(id, advancementHolder));
    }

    /**
     * Builds this recipe basing the name on the output item.
     *
     * @param recipeOutput Finished Recipe Consumer.
     * @param output   Output to base the recipe name off of.
     */
    protected void build(RecipeOutput recipeOutput, ItemLike output) {
        ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(output.asItem());
        if (registryName == null) {
            throw new IllegalStateException("Could not retrieve registry name for output.");
        }
        build(recipeOutput, registryName);
    }

    /**
     * Base recipe result.
     */
    protected abstract class RecipeResult implements FinishedRecipe {

        private final ResourceLocation id;
        @Nullable
        private final AdvancementHolder advancementHolder;

        public RecipeResult(ResourceLocation id, @Nullable AdvancementHolder advancementHolder) {
            this.id = id;
            this.advancementHolder = advancementHolder;
        }

        @Override
        public JsonObject serializeRecipe() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(JsonConstants.TYPE, serializerName.toString());
            if (!conditions.isEmpty()) {
                ICondition.LIST_CODEC.fieldOf(JsonConstants.CONDITIONS).codec().encode(conditions, JsonOps.INSTANCE, jsonObject);
            }
            this.serializeRecipeData(jsonObject);
            return jsonObject;
        }

        @NotNull
        @Override
        public RecipeSerializer<?> type() {
            //Note: This may be null if something is screwed up but this method isn't actually used, so it shouldn't matter
            // and in fact it will probably be null if only the API is included. But again, as we manually just use
            // the serializer's name this should not affect us
            return Objects.requireNonNull(ForgeRegistries.RECIPE_SERIALIZERS.getValue(serializerName));
        }

        @NotNull
        @Override
        public ResourceLocation id() {
            return this.id;
        }

        @Nullable
        @Override
        public AdvancementHolder advancement() {
            return advancementHolder;
        }
    }
}