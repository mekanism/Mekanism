package mekanism.common.integration.storagedrawer;

import com.jaquadro.minecraft.storagedrawers.api.IStorageDrawersApi;
import com.jaquadro.minecraft.storagedrawers.api.StorageDrawersApi;
import com.jaquadro.minecraft.storagedrawers.api.registry.IRecipeHandler;
import com.jaquadro.minecraft.storagedrawers.api.registry.IRecipeHandlerRegistry;
import mekanism.common.recipe.ShapedMekanismRecipe;
import mekanism.common.recipe.ShapelessMekanismRecipe;

import net.minecraft.item.crafting.IRecipe;

import java.util.List;

public class StorageDrawerRecipeHandler
{
    private StorageDrawerRecipeHandler(){}

    public static void register()
    {
        IStorageDrawersApi api = StorageDrawersApi.instance();
        if(api == null)
        {
            return;
        }
        IRecipeHandlerRegistry recipeHandlerRegistry = api.recipeHandlerRegistry();
        recipeHandlerRegistry.registerRecipeHandler(ShapedMekanismRecipe.class, new ShapedMekanismRecipeHandler());
        recipeHandlerRegistry.registerRecipeHandler(ShapelessMekanismRecipe.class, new ShapelessMekanismRecipeHandler());
    }

    private static class ShapedMekanismRecipeHandler implements IRecipeHandler
    {
        @Override
        public Object[] getInputAsArray (IRecipe recipe) {
            return ((ShapedMekanismRecipe) recipe).getInput();
        }

        @Override
        public List getInputAsList (IRecipe recipe) {
            return null;
        }
    }

    private static class ShapelessMekanismRecipeHandler implements IRecipeHandler
    {
        @Override
        public Object[] getInputAsArray (IRecipe recipe) {
            return null;
        }

        @Override
        public List getInputAsList (IRecipe recipe) {
            return ((ShapelessMekanismRecipe) recipe).getInput();
        }
    }
}
