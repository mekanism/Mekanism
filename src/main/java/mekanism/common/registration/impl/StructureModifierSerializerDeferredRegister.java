package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraftforge.common.world.StructureModifier;
import net.minecraftforge.registries.ForgeRegistries;

public class StructureModifierSerializerDeferredRegister extends WrappedDeferredRegister<Codec<? extends StructureModifier>> {

    public StructureModifierSerializerDeferredRegister(String modid) {
        super(modid, ForgeRegistries.Keys.STRUCTURE_MODIFIER_SERIALIZERS);
    }

    public <T extends StructureModifier> StructureModifierSerializerRegistryObject<T> register(String name, Supplier<Codec<T>> sup) {
        return register(name, sup, StructureModifierSerializerRegistryObject::new);
    }
}