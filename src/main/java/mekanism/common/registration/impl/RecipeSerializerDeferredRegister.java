package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;

public class RecipeSerializerDeferredRegister extends WrappedDeferredRegister<RecipeSerializer<?>> {

    public RecipeSerializerDeferredRegister(String modid) {
        super(modid, ForgeRegistries.RECIPE_SERIALIZERS);
    }

    public <RECIPE extends Recipe<?>> RecipeSerializerRegistryObject<RECIPE> register(String name, Supplier<RecipeSerializer<RECIPE>> sup) {
        return register(name, sup, RecipeSerializerRegistryObject::new);
    }
}