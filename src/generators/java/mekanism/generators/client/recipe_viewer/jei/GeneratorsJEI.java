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
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class GeneratorsJEI implements IModPlugin {

    @NotNull
    @Override
    public ResourceLocation getPluginUid() {
        //Note: Can't use MekanismGenerators.rl, as JEI needs this in the constructor and the class may not be loaded yet.
        // we can still reference the modid though because of constant inlining
        return ResourceLocation.fromNamespaceAndPath(MekanismGenerators.MODID, "jei_plugin");
    }

    @Override
    public void registerItemSubtypes(@NotNull ISubtypeRegistration registry) {
        if (MekanismJEI.shouldLoad()) {
            MekanismJEI.registerItemSubtypes(registry, GeneratorsItems.ITEMS.getEntries());
            MekanismJEI.registerItemSubtypes(registry, GeneratorsBlocks.BLOCKS.getSecondaryEntries());
        }
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registry) {
        if (!MekanismJEI.shouldLoad()) {
            return;
        }
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new FissionReactorRecipeCategory(guiHelper, GeneratorsRVRecipeType.FISSION));
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registry) {
        if (!MekanismJEI.shouldLoad()) {
            return;
        }
        CatalystRegistryHelper.register(registry, GeneratorsRVRecipeType.FISSION);
    }

    @Override
    public void registerIngredientAliases(@NotNull IIngredientAliasRegistration registration) {
        new GeneratorsAliasMapping().addAliases(new JEIAliasHelper(registration));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registry) {
        if (!MekanismJEI.shouldLoad()) {
            return;
        }
        RecipeRegistryHelper.register(registry, GeneratorsRVRecipeType.FISSION, FissionRecipeViewerRecipe.getFissionRecipes());
    }
}