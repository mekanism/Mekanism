package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.api.robit.RobitSkin;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.util.ResourceLocation;

public class RobitSkinDeferredRegister extends WrappedDeferredRegister<RobitSkin> {

    private final String modid;

    public RobitSkinDeferredRegister(String modid) {
        super(modid, RobitSkin.class);
        this.modid = modid;
    }

    public RobitSkinRegistryObject<RobitSkin> register(String name) {
        return register(name, new ResourceLocation(modid, name), new ResourceLocation(modid, name + "2"));
    }

    public RobitSkinRegistryObject<RobitSkin> register(String name, ResourceLocation... textures) {
        return register(name, () -> new RobitSkin(textures));
    }

    public <ROBIT_SKIN extends RobitSkin> RobitSkinRegistryObject<ROBIT_SKIN> register(String name, Supplier<? extends ROBIT_SKIN> sup) {
        return register(name, sup, RobitSkinRegistryObject::new);
    }
}