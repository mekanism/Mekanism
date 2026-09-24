package mekanism.common.tests.helpers;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.RecipeHolder;

public class MissingElementTestHelper extends MekGameTestHelper {

    public MissingElementTestHelper(GameTestInfo info) {
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

    public void checkForMissing(String type, Collection<? extends ResourceKey<?>> missingElements) {
        if (!missingElements.isEmpty()) {
            fail("Missing " + type + " for " + missingElements.stream()
                  .map(key -> key.identifier().toString())
                  .collect(Collectors.joining(", "))
            );
        }
    }
}