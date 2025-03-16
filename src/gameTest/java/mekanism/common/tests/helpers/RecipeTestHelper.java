package mekanism.common.tests.helpers;

import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

public class RecipeTestHelper extends MekGameTestHelper {

    public RecipeTestHelper(GameTestInfo info) {
        super(info);
    }

    public <RECIPE extends MekanismRecipe<?>> List<RecipeHolder<RECIPE>> getRecipes(IMekanismRecipeTypeProvider<?, RECIPE, ?> recipeType) {
        return recipeType.getRecipes(getLevel());
    }

    public <TYPE> Set<ResourceKey<TYPE>> collectKnownMissing(Registry<TYPE> registry, TagKey<TYPE> knownMissingTag) {
        Set<ResourceKey<TYPE>> inputs = new ReferenceOpenHashSet<>();
        for (Holder<TYPE> knownMissing : registry.getTagOrEmpty(knownMissingTag)) {
            ResourceKey<TYPE> key = knownMissing.getKey();
            if (key != null) {//Pretend all the known missing items are already there
                inputs.add(key);
            }
        }
        return inputs;
    }

    public void collectInputs(Set<ResourceKey<Item>> inputs, ItemStackIngredient input, TagKey<Item> knownMissingTag, String type) {
        for (ItemStack representation : input.getRepresentations()) {
            if (representation.is(knownMissingTag)) {
                fail("Item " + representation.getItem() + " is marked as being known to be missing, but has a " + type + " recipe.");
            }
            ResourceKey<Item> key = representation.getItemHolder().getKey();
            if (key != null) {
                inputs.add(key);
            }
        }
    }

    public <TYPE> Set<ResourceKey<TYPE>> collectMissingRecipes(Registry<TYPE> registry, DataMapType<TYPE, ?> dataMapType, Set<ResourceKey<Item>> recipeInputs) {
        Set<ResourceKey<TYPE>> missingRecipes = new ReferenceLinkedOpenHashSet<>();
        for (ResourceKey<TYPE> compostable : registry.getDataMap(dataMapType).keySet()) {
            if (!recipeInputs.contains(compostable)) {
                missingRecipes.add(compostable);
            }
        }
        return missingRecipes;
    }

    public void checkForMissing(Collection<? extends ResourceKey<?>> missingRecipes) {
        if (!missingRecipes.isEmpty()) {
            fail("Missing recipe for " + missingRecipes.stream()
                  .map(key -> key.location().toString())
                  .collect(Collectors.joining(", "))
            );
        }
    }
}