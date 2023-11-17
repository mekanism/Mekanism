package mekanism.common.registration.impl;

import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.providers.IPigmentProvider;
import mekanism.common.registration.WrappedRegistryObject;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

public class PigmentRegistryObject<PIGMENT extends Pigment> extends WrappedRegistryObject<Pigment, PIGMENT> implements IPigmentProvider {

    public PigmentRegistryObject(DeferredHolder<Pigment, PIGMENT> registryObject) {
        super(registryObject);
    }

    @NotNull
    @Override
    public PIGMENT getChemical() {
        return get();
    }
}