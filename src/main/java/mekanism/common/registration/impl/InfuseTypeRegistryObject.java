package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.providers.IInfuseTypeProvider;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraftforge.fml.RegistryObject;

public class InfuseTypeRegistryObject<INFUSE_TYPE extends InfuseType> extends WrappedRegistryObject<INFUSE_TYPE> implements IInfuseTypeProvider {

    public InfuseTypeRegistryObject(RegistryObject<INFUSE_TYPE> registryObject) {
        super(registryObject);
    }

    @Nonnull
    @Override
    public INFUSE_TYPE getChemical() {
        return get();
    }
}