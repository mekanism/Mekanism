package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentBuilder;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.util.ResourceLocation;

public class PigmentDeferredRegister extends WrappedDeferredRegister<Pigment> {

    public PigmentDeferredRegister(String modid) {
        super(modid, Pigment.class);
    }

    public PigmentRegistryObject<Pigment> register(String name, int tint) {
        return register(name, () -> new Pigment(PigmentBuilder.builder().color(tint)));
    }

    public PigmentRegistryObject<Pigment> register(String name, ResourceLocation texture) {
        return register(name, () -> new Pigment(PigmentBuilder.builder(texture)));
    }

    public <PIGMENT extends Pigment> PigmentRegistryObject<PIGMENT> register(String name, Supplier<? extends PIGMENT> sup) {
        return register(name, sup, PigmentRegistryObject::new);
    }
}