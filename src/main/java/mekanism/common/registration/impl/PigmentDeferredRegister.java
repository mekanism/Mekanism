package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentBuilder;
import mekanism.common.registration.MekanismDeferredRegister;
import mekanism.common.registration.impl.DeferredChemical.DeferredPigment;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public class PigmentDeferredRegister extends MekanismDeferredRegister<Pigment> {

    public PigmentDeferredRegister(String modid) {
        super(MekanismAPI.PIGMENT_REGISTRY_NAME, modid, DeferredPigment::new);
    }

    public DeferredPigment<Pigment> register(String name, int tint) {
        return register(name, () -> new Pigment(PigmentBuilder.builder().tint(tint)));
    }

    public DeferredPigment<Pigment> register(String name, ResourceLocation texture) {
        return register(name, () -> new Pigment(PigmentBuilder.builder(texture)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <PIGMENT extends Pigment> DeferredPigment<PIGMENT> register(String name, Supplier<? extends PIGMENT> sup) {
        return (DeferredPigment<PIGMENT>) super.register(name, sup);
    }
}