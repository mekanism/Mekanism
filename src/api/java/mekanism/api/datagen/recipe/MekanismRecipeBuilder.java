package mekanism.api.datagen.recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Base recipe builder that declares various common methods between our different builders.
 */
@NothingNullByDefault
@SuppressWarnings("UnusedReturnValue")
public abstract class MekanismRecipeBuilder<BUILDER extends MekanismRecipeBuilder<BUILDER>> {

    protected final List<ICondition> conditions = new ArrayList<>();
    protected final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    protected MekanismRecipeBuilder() {
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
     */
    protected abstract Recipe<?> asRecipe();

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
     * @param id           Name of the recipe being built.
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
            advancementHolder = advancementBuilder.build(id.withPrefix("recipes/"));
        }
        recipeOutput.accept(id, asRecipe(), advancementHolder, conditions.toArray(new ICondition[0]));
    }

    /**
     * Builds this recipe basing the name on the output item.
     *
     * @param recipeOutput Finished Recipe Consumer.
     * @param output       Output to base the recipe name off of.
     * @deprecated Use {@link #build(RecipeOutput, Holder)} instead
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    protected void build(RecipeOutput recipeOutput, ItemLike output) {
        ResourceLocation registryName = BuiltInRegistries.ITEM.getKeyOrNull(output.asItem());
        if (registryName == null) {
            throw new IllegalStateException("Could not retrieve registry name for output.");
        }
        build(recipeOutput, registryName);
    }

    /**
     * Builds this recipe basing the name on the output item.
     *
     * @param recipeOutput Finished Recipe Consumer.
     * @param output       Output to base the recipe name off of.
     * @since 10.7.11
     */
    protected void build(RecipeOutput recipeOutput, Holder<Item> output) {
        ResourceKey<Item> key = output.getKey();
        if (key == null) {
            throw new IllegalStateException("Could not retrieve registry name for output.");
        }
        build(recipeOutput, key.location());
    }
}