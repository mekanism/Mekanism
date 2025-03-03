
package mekanism.common.tests.recipe;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.basic.BasicItemStackToItemStackRecipe;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tests.helpers.MekGameTestHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider.TagLookup;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;
import org.jetbrains.annotations.NotNull;

@ForEachTest(groups = "recipe.bio_fuel")
public class MissingBioFuelRecipeTest {

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests to make sure that there are no unknown composter recipes missing corresponding bio fuel recipes.")
    public static void testMissingComposterRecipes(final DynamicTest test, final RegistrationHelper reg) {
        final TagKey<Item> KNOWN_MISSING = ItemTags.create(ResourceLocation.fromNamespaceAndPath(reg.modId(), "known_missing"));

        //TODO: Decide if we want to have things like the biome mods load during game tests as well for purposes of checking if we want to add
        // compat with any of their organic items for making bio-fuel
        reg.addProvider(event -> new ItemTagsProvider(event.getGenerator().getPackOutput(), event.getLookupProvider(),
              CompletableFuture.completedFuture(TagLookup.empty()), reg.modId(), event.getExistingFileHelper()) {
            @Override
            protected void addTags(@NotNull HolderLookup.Provider provider) {
                //TODO: Figure out if we want to add bio fuel recipes for any of these
                tag(KNOWN_MISSING);
            }
        });
        test.onGameTest(MekGameTestHelper.class, helper -> helper.startSequence()
              .thenMap(() -> MekanismRecipeType.CRUSHING.getRecipes(helper.getLevel()))
              .thenMap(crushingRecipes -> {
                  Set<ResourceKey<Item>> bioFuelRecipeInputs = new ReferenceOpenHashSet<>();
                  for (Holder<Item> knownMissing : BuiltInRegistries.ITEM.getTagOrEmpty(KNOWN_MISSING)) {
                      ResourceKey<Item> key = knownMissing.getKey();
                      if (key != null) {//Pretend all the known missing items are already there
                          bioFuelRecipeInputs.add(key);
                      }
                  }
                  for (RecipeHolder<ItemStackToItemStackRecipe> crushingRecipe : crushingRecipes) {
                      if (crushingRecipe.value() instanceof BasicItemStackToItemStackRecipe basicRecipe && basicRecipe.getOutputRaw().is(MekanismItems.BIO_FUEL)) {
                          for (ItemStack representation : basicRecipe.getInput().getRepresentations()) {
                              if (representation.is(KNOWN_MISSING)) {
                                  helper.fail("Item " + representation.getItem() + " is marked as being known to be missing, but has a bio fuel recipe.");
                              }
                              ResourceKey<Item> key = representation.getItemHolder().getKey();
                              if (key != null) {
                                  bioFuelRecipeInputs.add(key);
                              }
                          }
                      }
                  }
                  return bioFuelRecipeInputs;
              }).thenExecute(bioFuelRecipeInputs -> {
                  List<ResourceKey<Item>> missingRecipes = new ArrayList<>();
                  for (Entry<ResourceKey<Item>, Compostable> entry : BuiltInRegistries.ITEM.getDataMap(NeoForgeDataMaps.COMPOSTABLES).entrySet()) {
                      if (!bioFuelRecipeInputs.contains(entry.getKey())) {
                          missingRecipes.add(entry.getKey());
                      }
                  }
                  if (!missingRecipes.isEmpty()) {
                      helper.fail("Missing recipe for " + missingRecipes.stream()
                            .map(key -> key.location().toString())
                            .collect(Collectors.joining(", "))
                      );
                  }
              }).thenSucceed()
        );
    }
}