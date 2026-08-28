package mekanism.generators.client.recipe_viewer.jei;

import mekanism.client.recipe_viewer.jei.CatalystRegistryHelper;
import mekanism.client.recipe_viewer.jei.JEIAliasHelper;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.jei.RecipeRegistryHelper;
import mekanism.generators.client.recipe_viewer.GeneratorsRVRecipeType;
import mekanism.generators.client.recipe_viewer.alias.GeneratorsAliasMapping;
import mekanism.generators.client.recipe_viewer.recipe.FissionRecipeViewerRecipe;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

@JeiPlugin
public class GeneratorsJEI implements IModPlugin {

    @Override
    public Identifier getPluginUid() {
        //Note: Can't use MekanismGenerators.rl, as JEI needs this in the constructor and the class may not be loaded yet.
        // we can still reference the modid though because of constant inlining
        return Identifier.fromNamespaceAndPath(MekanismGenerators.MODID, "jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registry) {
        MekanismJEI.registerItemSubtypes(registry, GeneratorsItems.ITEMS.getEntries());
        MekanismJEI.registerItemSubtypes(registry, GeneratorsBlocks.BLOCKS.getSecondaryEntries());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new FissionReactorRecipeCategory(guiHelper, GeneratorsRVRecipeType.FISSION));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        CatalystRegistryHelper.register(registry, GeneratorsRVRecipeType.FISSION);
    }

    @Override
    public void registerIngredientAliases(IIngredientAliasRegistration registration) {
        new GeneratorsAliasMapping().addAliases(new JEIAliasHelper(registration));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        HolderLookup.Provider registries = registry.getContextMap().getOrThrow(SlotDisplayContext.REGISTRIES);
        RecipeRegistryHelper.register(registry, GeneratorsRVRecipeType.FISSION, FissionRecipeViewerRecipe.getFissionRecipes(registries));
    }
}