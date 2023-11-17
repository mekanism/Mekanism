package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

public class RecipeSerializerRegistryObject<RECIPE extends Recipe<?>> extends WrappedRegistryObject<RecipeSerializer<?>, RecipeSerializer<RECIPE>> {

    public RecipeSerializerRegistryObject(DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RECIPE>> registryObject) {
        super(registryObject);
    }
}