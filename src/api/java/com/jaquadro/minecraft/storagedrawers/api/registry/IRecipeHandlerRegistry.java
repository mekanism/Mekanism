package com.jaquadro.minecraft.storagedrawers.api.registry;

public interface IRecipeHandlerRegistry
{
    void registerRecipeHandler (Class clazz, IRecipeHandler handler);

    void registerIngredientHandler (Class clazz, IIngredientHandler handler);

    IRecipeHandler getRecipeHandler (Class clazz);

    IIngredientHandler getIngredientHandler (Class clazz);
}
