package mekanism.common.recipe;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Ingredient.ItemValue;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

@NothingNullByDefault
public abstract class BaseRecipeProvider extends RecipeProvider {

    protected final HolderGetter<Fluid> fluids;
    protected final HolderGetter<Chemical> chemicals;

    protected BaseRecipeProvider(RecipeOutput output, HolderLookup.Provider registries) {
        super(registries, output);
        this.fluids = this.registries.lookupOrThrow(Registries.FLUID);
        this.chemicals = this.registries.lookupOrThrow(MekanismAPI.CHEMICAL_REGISTRY_NAME);
    }

    @Override
    protected final void buildRecipes() {
        addRecipes(registries);
        for (ISubRecipeProvider subRecipeProvider : getSubRecipeProviders()) {
            subRecipeProvider.addRecipes(output, registries);
        }
    }

    protected abstract void addRecipes(HolderLookup.Provider registries);

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

    public static HolderSet<Item> osmiumIngot(HolderGetter<Item> items) {
        TagKey<Item> tag = Objects.requireNonNull(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM));
        return items.getOrThrow(tag);
    }

    public static HolderSet<Item> leadIngot(HolderGetter<Item> items) {
        TagKey<Item> tag = Objects.requireNonNull(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD));
        return items.getOrThrow(tag);
    }

    public static HolderSet<Item> tinIngot(HolderGetter<Item> items) {
        TagKey<Item> tag = Objects.requireNonNull(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN));
        return items.getOrThrow(tag);
    }
}