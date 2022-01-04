package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.RegistryObject;

public class IRecipeSerializerRegistryObject<RECIPE extends Recipe<?>> extends WrappedRegistryObject<RecipeSerializer<RECIPE>> {

    public IRecipeSerializerRegistryObject(RegistryObject<RecipeSerializer<RECIPE>> registryObject) {
        super(registryObject);
    }
}