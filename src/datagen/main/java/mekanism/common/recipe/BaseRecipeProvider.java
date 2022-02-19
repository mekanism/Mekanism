package mekanism.common.recipe;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

@ParametersAreNonnullByDefault
public abstract class BaseRecipeProvider extends RecipeProvider {

    private final ExistingFileHelper existingFileHelper;
    private final String modid;

    protected BaseRecipeProvider(DataGenerator gen, ExistingFileHelper existingFileHelper, String modid) {
        super(gen);
        this.existingFileHelper = existingFileHelper;
        this.modid = modid;
    }

    @Nonnull
    @Override
    public String getName() {
        return super.getName() + ": " + modid;
    }

    @Override
    protected final void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {
        Consumer<IFinishedRecipe> trackingConsumer = consumer.andThen(recipe ->
              existingFileHelper.trackGenerated(recipe.getId(), ResourcePackType.SERVER_DATA, ".json", "recipes"));
        addRecipes(trackingConsumer);
        getSubRecipeProviders().forEach(subRecipeProvider -> subRecipeProvider.addRecipes(trackingConsumer));
    }

    protected abstract void addRecipes(Consumer<IFinishedRecipe> consumer);

    /**
     * Gets all the sub/offloaded recipe providers that this recipe provider has.
     *
     * @implNote This is only called once per provider so there is no need to bother caching the list that this returns
     */
    protected List<ISubRecipeProvider> getSubRecipeProviders() {
        return Collections.emptyList();
    }

    public static Ingredient createIngredient(ITag<Item> itemTag, IItemProvider... items) {
        return createIngredient(Collections.singleton(itemTag), items);
    }

    public static Ingredient createIngredient(Collection<ITag<Item>> itemTags, IItemProvider... items) {
        return Ingredient.fromValues(Stream.concat(
              itemTags.stream().map(Ingredient.TagList::new),
              Arrays.stream(items).map(item -> new Ingredient.SingleItemList(new ItemStack(item)))
        ));
    }
}