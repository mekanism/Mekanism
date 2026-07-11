package mekanism.generators.client.recipe_viewer.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import mekanism.client.recipe_viewer.emi.MekanismEmi;
import mekanism.generators.client.recipe_viewer.GeneratorsRVRecipeType;
import mekanism.generators.client.recipe_viewer.emi.recipe.FissionReactorEmiRecipe;
import mekanism.generators.client.recipe_viewer.recipe.FissionRecipeViewerRecipe;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsItems;

@EmiEntrypoint
public class GeneratorsEmi implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        addCategories(registry);

        MekanismEmi.registerItemSubtypes(registry, GeneratorsItems.ITEMS.getEntries());
        MekanismEmi.registerItemSubtypes(registry, GeneratorsBlocks.BLOCKS.getSecondaryEntries());
    }

    private void addCategories(EmiRegistry registry) {
        MekanismEmi.addCategoryAndRecipes(registry, GeneratorsRVRecipeType.FISSION, FissionReactorEmiRecipe::new, FissionRecipeViewerRecipe.getFissionRecipes());
    }
}