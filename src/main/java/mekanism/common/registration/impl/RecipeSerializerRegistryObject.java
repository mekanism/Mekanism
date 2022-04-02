package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.RegistryObject;

public class RecipeSerializerRegistryObject<RECIPE extends Recipe<?>> extends WrappedRegistryObject<RecipeSerializer<RECIPE>> {

    public RecipeSerializerRegistryObject(RegistryObject<RecipeSerializer<RECIPE>> registryObject) {
        super(registryObject);
    }
}