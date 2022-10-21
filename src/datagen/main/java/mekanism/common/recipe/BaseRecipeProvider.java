package mekanism.common.recipe;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.DifferenceIngredient;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public abstract class BaseRecipeProvider extends RecipeProvider {

    private final ExistingFileHelper existingFileHelper;
    private final String modid;

    protected BaseRecipeProvider(DataGenerator gen, ExistingFileHelper existingFileHelper, String modid) {
        super(gen);
        this.existingFileHelper = existingFileHelper;
        this.modid = modid;
    }

    @NotNull
    @Override
    public String getName() {
        return super.getName() + ": " + modid;
    }

    @Override
    protected final void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        Consumer<FinishedRecipe> trackingConsumer = consumer.andThen(recipe ->
              existingFileHelper.trackGenerated(recipe.getId(), PackType.SERVER_DATA, ".json", "recipes"));
        addRecipes(trackingConsumer);
        getSubRecipeProviders().forEach(subRecipeProvider -> subRecipeProvider.addRecipes(trackingConsumer));
    }

    protected abstract void addRecipes(Consumer<FinishedRecipe> consumer);

    /**
     * Gets all the sub/offloaded recipe providers that this recipe provider has.
     *
     * @implNote This is only called once per provider so there is no need to bother caching the list that this returns
     */
    protected List<ISubRecipeProvider> getSubRecipeProviders() {
        return Collections.emptyList();
    }

    public static Ingredient createIngredient(TagKey<Item> itemTag, ItemLike... items) {
        return createIngredient(Collections.singleton(itemTag), items);
    }

    public static Ingredient createIngredient(Collection<TagKey<Item>> itemTags, ItemLike... items) {
        return Ingredient.fromValues(Stream.concat(
              itemTags.stream().map(Ingredient.TagValue::new),
              Arrays.stream(items).map(item -> new Ingredient.ItemValue(new ItemStack(item)))
        ));
    }

    @SafeVarargs
    public static Ingredient createIngredient(TagKey<Item>... tags) {
        return Ingredient.fromValues(Arrays.stream(tags).map(Ingredient.TagValue::new));
    }

    public static Ingredient difference(TagKey<Item> base, ItemLike subtracted) {
        return DifferenceIngredient.of(Ingredient.of(base), Ingredient.of(subtracted));
    }
}