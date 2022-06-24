package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.ForgeRegistries;

public class BiomeModifierSerializerDeferredRegister extends WrappedDeferredRegister<Codec<? extends BiomeModifier>> {

    public BiomeModifierSerializerDeferredRegister(String modid) {
        super(modid, ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS);
    }

    public <T extends BiomeModifier> BiomeModifierSerializerRegistryObject<T> register(String name, Supplier<Codec<T>> sup) {
        return register(name, sup, BiomeModifierSerializerRegistryObject::new);
    }
}