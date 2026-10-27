package mekanism.client.recipe_viewer.jei;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.SingleInputRecipe.ItemInputRecipe;
import mekanism.client.recipe_viewer.jei.transfer.JeiMachineTransferHandler;
import mekanism.client.recipe_viewer.jei.transfer.SimpleMachineTransferHandler;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.base.TileEntityMekanism;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeHolder;

public class RecipeRegistryHelper {

    private RecipeRegistryHelper() {
    }

    public static void registerCondensentrator(IRecipeRegistration registry) {
        List<RecipeHolder<RotaryRecipe>> condensentratorRecipes = new ArrayList<>();
        List<RecipeHolder<RotaryRecipe>> decondensentratorRecipes = new ArrayList<>();
        for (RecipeHolder<RotaryRecipe> recipeHolder : MekanismRecipeType.ROTARY.getRecipes()) {
            RotaryRecipe recipe = recipeHolder.value();
            if (recipe.hasChemicalToFluid()) {
                condensentratorRecipes.add(recipeHolder);
            }
            if (recipe.hasFluidToChemical()) {
                decondensentratorRecipes.add(recipeHolder);
            }
        }
        registry.addRecipes(MekanismJEI.holderRecipeType(RecipeViewerRecipeType.CONDENSENTRATING), condensentratorRecipes);
        registry.addRecipes(MekanismJEI.holderRecipeType(RecipeViewerRecipeType.DECONDENSENTRATING), decondensentratorRecipes);
    }

    public static <RECIPE extends MekanismRecipe<?, ?, ?>> void register(IRecipeRegistration registry, IRecipeViewerRecipeType<RECIPE> recipeType,
          IMekanismRecipeTypeProvider<?, ?, ?, RECIPE, ?> type) {
        registry.addRecipes(MekanismJEI.holderRecipeType(recipeType), type.getRecipes());
    }

    public static <RECIPE extends MekanismRecipe<?, ?, ?>> void registerAsHolder(IRecipeRegistration registry, IRecipeViewerRecipeType<RECIPE> recipeType, List<RecipeHolder<RECIPE>> recipes) {
        registry.addRecipes(MekanismJEI.holderRecipeType(recipeType), recipes);
    }

    public static <RECIPE> void register(IRecipeRegistration registry, IRecipeViewerRecipeType<RECIPE> recipeType, List<RECIPE> recipes) {
        registry.addRecipes(MekanismJEI.recipeType(recipeType), recipes);
    }

    public static <TILE extends TileEntityMekanism, RECIPE extends ItemInputRecipe<?, ?, ?>> void addSimpleTransferHandler(IRecipeTransferRegistration registry,
          MenuType<MekanismTileContainer<TILE>> menuType, IRecipeViewerRecipeType<RECIPE> recipeTypeProvider) {
        addSimpleTransferHandler(registry, tileContainerClass(), menuType, recipeTypeProvider);
    }

    public static <CONTAINER extends MekanismContainer, RECIPE extends ItemInputRecipe<?, ?, ?>> void addSimpleTransferHandler(IRecipeTransferRegistration registry,
          Class<CONTAINER> containerClass, MenuType<CONTAINER> menuType, IRecipeViewerRecipeType<RECIPE> recipeTypeProvider) {
        JeiMachineTransferHandler<CONTAINER, RECIPE> handler = new SimpleMachineTransferHandler<>(registry.getTransferHelper(), containerClass, menuType, recipeTypeProvider);
        addTransferHandler(registry, handler);
    }

    public static <CONTAINER extends MekanismContainer, RECIPE extends MekanismRecipe<?, ?, ?>> void addTransferHandler(IRecipeTransferRegistration registry,
          JeiMachineTransferHandler<CONTAINER, RECIPE> handler) {
        registry.addRecipeTransferHandler(handler, handler.getRecipeType());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <TILE extends TileEntityMekanism> Class<MekanismTileContainer<TILE>> tileContainerClass() {
        return (Class<MekanismTileContainer<TILE>>) (Class) MekanismTileContainer.class;
    }
}