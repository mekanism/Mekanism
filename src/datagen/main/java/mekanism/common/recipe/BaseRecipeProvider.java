package mekanism.common.recipe;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Ingredient.ItemValue;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class BaseRecipeProvider extends RecipeProvider {

    private final CompletableFuture<HolderLookup.Provider> registriesFuture;
    private final ExistingFileHelper existingFileHelper;

    protected BaseRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture, ExistingFileHelper existingFileHelper) {
        super(output, registriesFuture);
        this.registriesFuture = registriesFuture;
        this.existingFileHelper = existingFileHelper;
    }

    private record WrapperRecipeOutput(RecipeOutput parent, ExistingFileHelper existingFileHelper) implements RecipeOutput {

        @Override
        public void accept(ResourceLocation recipeId, Recipe<?> recipe, @Nullable AdvancementHolder advancementHolder, ICondition... conditions) {
            parent.accept(recipeId, recipe, advancementHolder, conditions);
            existingFileHelper.trackGenerated(recipeId, PackType.SERVER_DATA, ".json", "recipes");
        }

        @Override
        public Builder advancement() {
            return parent.advancement();
        }
    }

    @Override
    protected final void buildRecipes(RecipeOutput output) {
        HolderLookup.Provider registries = registriesFuture.resultNow();
        WrapperRecipeOutput trackingConsumer = new WrapperRecipeOutput(output, existingFileHelper);
        addRecipes(trackingConsumer, registries);
        for (ISubRecipeProvider subRecipeProvider : getSubRecipeProviders()) {
            subRecipeProvider.addRecipes(trackingConsumer, registries);
        }
    }

    protected abstract void addRecipes(RecipeOutput output, HolderLookup.Provider registries);

    /**
     * Gets all the sub/offloaded recipe providers that this recipe provider has.
     *
     * @implNote This is only called once per provider so there is no need to bother caching the list that this returns
     */
    protected List<ISubRecipeProvider> getSubRecipeProviders() {
        return Collections.emptyList();
    }

    public static Ingredient createIngredient(TagKey<Item> itemTag, Item... items) {
        return createIngredient(Stream.of(itemTag), Arrays.stream(items).map(ItemStack::new));
    }

    private static Ingredient createIngredient(Stream<TagKey<Item>> itemTags, Stream<ItemStack> items) {
        return Ingredient.fromValues(Stream.concat(
              itemTags.map(Ingredient.TagValue::new),
              items.map(ItemValue::new)
        ));
    }

    @SafeVarargs
    public static Ingredient createIngredient(Holder<Item>... items) {
        return Ingredient.of(Arrays.stream(items).map(ItemStack::new));
    }

    public static Ingredient difference(TagKey<Item> base, Holder<Item> subtracted) {
        return DifferenceIngredient.of(Ingredient.of(base), Ingredient.of(subtracted.value()));
    }

    public static TagKey<Item> osmiumIngot() {
        return Objects.requireNonNull(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM));
    }

    public static TagKey<Item> leadIngot() {
        return Objects.requireNonNull(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD));
    }

    public static TagKey<Item> tinIngot() {
        return Objects.requireNonNull(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN));
    }
}