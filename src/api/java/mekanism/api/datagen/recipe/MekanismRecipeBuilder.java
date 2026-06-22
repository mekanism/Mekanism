package mekanism.api.datagen.recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalInstance;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.RecipeUnlockedTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.fluids.FluidInstance;
import org.jspecify.annotations.Nullable;

/// Base recipe builder that declares various common methods between our different builders.
@SuppressWarnings("UnusedReturnValue")
public abstract class MekanismRecipeBuilder<BUILDER extends MekanismRecipeBuilder<BUILDER>> implements RecipeBuilder {

    protected static final ResourceKey<Recipe<?>> NO_DEFAULT_ID = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("if_you_see_this", "you_forgot_an_id"));
    protected final List<ICondition> conditions = new ArrayList<>();
    protected final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    protected String group;

    protected MekanismRecipeBuilder() {
        //TODO: We may also want to validate inputs, currently we are not validating our input ingredients as being valid, and are just validating the other parameters
    }

    @SuppressWarnings("unchecked")
    private BUILDER self() {
        return (BUILDER) this;
    }

    /// Adds a criterion to this recipe.
    ///
    /// @param criterion Criterion to add.
    public BUILDER unlockedBy(RecipeCriterion criterion) {
        return unlockedBy(criterion.name(), criterion.criterion());
    }

    /// Adds a criterion to this recipe.
    ///
    /// @param name      Name of the criterion.
    /// @param criterion Criterion to add.
    @Override
    public BUILDER unlockedBy(String name, Criterion<?> criterion) {
        criteria.put(name, criterion);
        return self();
    }

    @Override
    public BUILDER group(@Nullable String group) {
        this.group = group;
        return self();
    }

    /// Adds a condition to this recipe.
    ///
    /// @param condition Condition to add.
    public BUILDER addCondition(ICondition condition) {
        conditions.add(condition);
        return self();
    }

    /// Gets a recipe result object.
    protected abstract Recipe<?> asRecipe();

    /// Performs any extra validation.
    ///
    /// @param id ID of the recipe validation is being performed on.
    ///
    /// @since 10.8.0
    protected void ensureValid(ResourceKey<Recipe<?>> id) {
        //TODO - 26.2: Re-evaluate implementations, as it seems that vanilla changed what they are validating
    }

    /// Builds this recipe.
    ///
    /// @param recipeOutput Finished Recipe Consumer.
    /// @param id           Name of the recipe being built.
    public void save(RecipeOutput recipeOutput, Identifier id) {
        Identifier defaultId = defaultId().identifier();
        if (id.equals(defaultId)) {
            throw new IllegalStateException("Recipe " + id + " should remove its 'save' argument as it is equal to default one");
        } else {
            save(recipeOutput, ResourceKey.create(Registries.RECIPE, id));
        }
    }

    /// Builds this recipe.
    ///
    /// @param recipeOutput Finished Recipe Consumer.
    /// @param id           Name of the recipe being built.
    @Override
    public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> id) {
        ensureValid(id);
        AdvancementHolder advancementHolder = null;
        if (!this.criteria.isEmpty()) {
            Advancement.Builder advancementBuilder = recipeOutput.advancement()
                  .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                  .rewards(AdvancementRewards.Builder.recipe(id))
                  .requirements(AdvancementRequirements.Strategy.OR);
            //If there is a way to "unlock" this recipe then add an advancement with the criteria
            this.criteria.forEach(advancementBuilder::addCriterion);
            advancementHolder = advancementBuilder.build(id.identifier().withPrefix("recipes/"));
        }
        recipeOutput.accept(id, asRecipe(), advancementHolder, conditions.toArray(new ICondition[0]));
    }

    /// Builds this recipe basing the name on the output item.
    ///
    /// @param recipeOutput Finished Recipe Consumer.
    /// @param output       Output to base the recipe name off of.
    /// @since 10.7.11
    protected void save(RecipeOutput recipeOutput, Holder<Item> output) {
        ResourceKey<Item> key = output.getKey();
        if (key == null) {
            throw new IllegalStateException("Could not retrieve registry name for output.");
        }
        save(recipeOutput, key.identifier());
    }

    public static ResourceKey<Recipe<?>> getDefaultRecipeId(FluidInstance fluid) {
        return ResourceKey.create(Registries.RECIPE, fluid.typeHolder().unwrapKey().orElseThrow().identifier());
    }

    public static ResourceKey<Recipe<?>> getDefaultRecipeId(ChemicalInstance chemical) {
        return ResourceKey.create(Registries.RECIPE, chemicalId(chemical));
    }

    public static Identifier chemicalId(TypedInstance<Chemical> chemical) {
        return chemical.typeHolder().unwrapKey().orElseThrow().identifier();
    }
}