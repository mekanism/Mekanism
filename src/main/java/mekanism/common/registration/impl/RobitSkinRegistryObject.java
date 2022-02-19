package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import mekanism.api.providers.IRobitSkinProvider;
import mekanism.api.robit.RobitSkin;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraftforge.fml.RegistryObject;

public class RobitSkinRegistryObject<ROBIT_SKIN extends RobitSkin> extends WrappedRegistryObject<ROBIT_SKIN> implements IRobitSkinProvider {

    public RobitSkinRegistryObject(RegistryObject<ROBIT_SKIN> registryObject) {
        super(registryObject);
    }

    @Nonnull
    @Override
    public RobitSkin getSkin() {
        return get();
    }
}