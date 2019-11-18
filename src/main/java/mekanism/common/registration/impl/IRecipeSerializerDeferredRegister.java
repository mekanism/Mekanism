package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;

public class IRecipeSerializerDeferredRegister extends WrappedDeferredRegister<IRecipeSerializer<?>> {

    public IRecipeSerializerDeferredRegister(String modid) {
        super(modid, ForgeRegistries.RECIPE_SERIALIZERS);
    }

    public <RECIPE extends IRecipe<?>> IRecipeSerializerRegistryObject<RECIPE> register(String name, Supplier<IRecipeSerializer<RECIPE>> sup) {
        return register(name, sup, IRecipeSerializerRegistryObject::new);
    }
}