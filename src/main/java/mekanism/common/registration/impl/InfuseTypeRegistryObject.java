package mekanism.common.registration.impl;

import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.providers.IInfuseTypeProvider;
import mekanism.common.registration.WrappedRegistryObject;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

public class InfuseTypeRegistryObject<INFUSE_TYPE extends InfuseType> extends WrappedRegistryObject<InfuseType, INFUSE_TYPE> implements IInfuseTypeProvider {

    public InfuseTypeRegistryObject(DeferredHolder<InfuseType, INFUSE_TYPE> registryObject) {
        super(registryObject);
    }

    @NotNull
    @Override
    public INFUSE_TYPE getChemical() {
        return get();
    }
}