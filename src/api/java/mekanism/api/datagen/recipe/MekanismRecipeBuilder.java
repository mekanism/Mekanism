package mekanism.api.datagen.recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import mekanism.api.chemical.ChemicalInstance;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.fluids.FluidInstance;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

/// Base recipe builder that declares various common methods between our different builders.
@SuppressWarnings("UnusedReturnValue")
public abstract class MekanismRecipeBuilder<BOOK_CATEGORY, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
      BUILDER extends MekanismRecipeBuilder<BOOK_CATEGORY, BOOK_INFO, BUILDER>> implements RecipeBuilder {

    protected static final ResourceKey<Recipe<?>> NO_DEFAULT_ID = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("if_you_see_this", "you_forgot_an_id"));
    protected final List<ICondition> conditions = new ArrayList<>();
    protected final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    private final BiFunction<BOOK_CATEGORY, String, BOOK_INFO> bookInfoCreator;
    private boolean showNotification = true;
    private BOOK_CATEGORY category;
    @Nullable
    private String advancementCategory;
    @Nullable
    private String group;

    protected MekanismRecipeBuilder(BiFunction<BOOK_CATEGORY, String, BOOK_INFO> bookInfoCreator, BOOK_CATEGORY defaultCategory) {
        //TODO: We may also want to validate inputs, currently we are not validating our input ingredients as being valid, and are just validating the other parameters
        this.bookInfoCreator = bookInfoCreator;
        this.category = defaultCategory;
    }

    @SuppressWarnings("unchecked")
    @Contract("-> this")
    private BUILDER self() {
        return (BUILDER) this;
    }

    /// Adds a criterion to this recipe.
    ///
    /// @param criterion Criterion to add.
    @Contract("_ -> this")
    public BUILDER unlockedBy(RecipeCriterion criterion) {
        return unlockedBy(criterion.name(), criterion.criterion());
    }

    /// Adds a criterion to this recipe.
    ///
    /// @param name      Name of the criterion.
    /// @param criterion Criterion to add.
    @Override
    @Contract("_, _ -> this")
    public BUILDER unlockedBy(String name, Criterion<?> criterion) {
        criteria.put(name, criterion);
        return self();
    }

    /// Sets the recipe book category
    ///
    /// @param category Recipe book category to set
    @Contract("_ -> this")
    public BUILDER category(BOOK_CATEGORY category) {
        this.category = category;
        return self();
    }

    /// Sets the advancement unlock folder category
    ///
    /// @param category Unlock folder category to set
    @Contract("_ -> this")
    public BUILDER advancementCategory(String category) {
        this.advancementCategory = category;
        return self();
    }

    @Override
    @Contract("_ -> this")
    public BUILDER group(@Nullable String group) {
        this.group = group;
        return self();
    }

    /// Adds a condition to this recipe.
    ///
    /// @param condition Condition to add.
    @Contract("_ -> this")
    public BUILDER addCondition(ICondition condition) {
        conditions.add(condition);
        return self();
    }

    /// Sets whether a notification is shown when unlocking the recipe. Defaults to `true`.
    ///
    /// @param showNotification `true` if a notification should be set.
    ///
    /// @since 10.8.0
    @Contract("_ -> this")
    public BUILDER showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return self();
    }

    /// {@return the common recipe info}
    ///
    /// @since 10.8.0
    protected Recipe.CommonInfo commonInfo() {
        return RecipeBuilder.createCraftingCommonInfo(showNotification);
    }

    protected BOOK_INFO bookInfo() {
        return bookInfoCreator.apply(category, Objects.requireNonNullElse(group, ""));
    }

    /// Gets a recipe result object.
    protected abstract Recipe<?> asRecipe();

    /// Builds this recipe.
    ///
    /// @param recipeOutput Finished Recipe Consumer.
    /// @param id           Name of the recipe being built.
    public void save(RecipeOutput recipeOutput, Identifier id) {
        save(recipeOutput, id.toString());
    }

    /// Builds this recipe.
    ///
    /// @param recipeOutput Finished Recipe Consumer.
    /// @param id           Name of the recipe being built.
    @Override
    public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> id) {
        AdvancementHolder advancementHolder = null;
        if (!this.criteria.isEmpty()) {
            //If there is a way to "unlock" this recipe then add an advancement with the criteria
            RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();
            this.criteria.forEach(advancementBuilder::unlockedBy);
            advancementHolder = advancementBuilder.build(recipeOutput, id, advancementCategory);
        }
        recipeOutput.accept(id, asRecipe(), advancementHolder, conditions.toArray(new ICondition[0]));
    }

    /// Builds this recipe basing the name on the output item.
    ///
    /// @param recipeOutput Finished Recipe Consumer.
    /// @param output       Output to base the recipe name off of.
    ///
    /// @since 10.7.11
    protected void save(RecipeOutput recipeOutput, Holder<Item> output) {
        save(recipeOutput, output.key().identifier());
    }

    public static ResourceKey<Recipe<?>> getDefaultRecipeId(FluidInstance fluid) {
        return ResourceKey.create(Registries.RECIPE, instanceId(fluid));
    }

    public static ResourceKey<Recipe<?>> getDefaultRecipeId(ChemicalInstance chemical) {
        return ResourceKey.create(Registries.RECIPE, instanceId(chemical));
    }

    public static Identifier instanceId(TypedInstance<?> instance) {
        return instance.typeHolder().unwrapKey().orElseThrow().identifier();
    }
}