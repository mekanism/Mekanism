package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import mekanism.common.registration.WrappedDatapackDeferredRegister;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.ForgeRegistries;

public class BiomeModifierSerializerDeferredRegister extends WrappedDatapackDeferredRegister<BiomeModifier> {

    public BiomeModifierSerializerDeferredRegister(String modid) {
        super(modid, ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, ForgeRegistries.Keys.BIOME_MODIFIERS);
    }

    public <T extends BiomeModifier> BiomeModifierSerializerRegistryObject<T> register(String name, Supplier<Codec<T>> sup) {
        return register(name, sup, BiomeModifierSerializerRegistryObject::new);
    }
}