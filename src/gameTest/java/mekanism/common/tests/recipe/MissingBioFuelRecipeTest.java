
package mekanism.common.tests.recipe;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.basic.BasicItemStackToItemStackRecipe;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tests.helpers.RecipeTestHelper;
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
import net.minecraft.world.item.crafting.RecipeHolder;
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
        final TagKey<Item> VALID_OUTPUT = ItemTags.create(ResourceLocation.fromNamespaceAndPath(reg.modId(), "valid_output"));

        //TODO: Decide if we want to have things like the biome mods load during game tests as well for purposes of checking if we want to add
        // compat with any of their organic items for making bio-fuel
        reg.addProvider(event -> new ItemTagsProvider(event.getGenerator().getPackOutput(), event.getLookupProvider(),
              CompletableFuture.completedFuture(TagLookup.empty()), reg.modId(), event.getExistingFileHelper()) {
            @Override
            @SuppressWarnings("unchecked")
            protected void addTags(@NotNull HolderLookup.Provider provider) {
                tag(VALID_OUTPUT).addTags(
                      MekanismTags.Items.FUELS_BIO,
                      MekanismTags.Items.FUELS_BLOCK_BIO
                );
                tag(KNOWN_MISSING);
            }
        });

        test.onGameTest(RecipeTestHelper.class, helper -> helper.succeedIf(() -> {
            Set<ResourceKey<Item>> bioFuelRecipeInputs = helper.collectKnownMissing(BuiltInRegistries.ITEM, KNOWN_MISSING);
            for (RecipeHolder<ItemStackToItemStackRecipe> crushingRecipe : helper.getRecipes(MekanismRecipeType.CRUSHING)) {
                if (crushingRecipe.value() instanceof BasicItemStackToItemStackRecipe basicRecipe && basicRecipe.getOutputRaw().is(VALID_OUTPUT)) {
                    helper.collectInputs(bioFuelRecipeInputs, basicRecipe.getInput(), KNOWN_MISSING, "bio fuel");
                }
            }
            Set<ResourceKey<Item>> missingRecipes = helper.collectMissingRecipes(BuiltInRegistries.ITEM, NeoForgeDataMaps.COMPOSTABLES, bioFuelRecipeInputs);
            helper.checkForMissing(missingRecipes);
        }));
    }
}