package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.robit.RobitSkin;
import mekanism.common.registration.WrappedForgeDeferredRegister;
import net.minecraft.resources.ResourceLocation;

public class RobitSkinDeferredRegister extends WrappedForgeDeferredRegister<RobitSkin> {

    private final String modid;

    public RobitSkinDeferredRegister(String modid) {
        super(modid, MekanismAPI.robitSkinRegistryName());
        this.modid = modid;
    }

    public RobitSkinRegistryObject<RobitSkin> register(String name) {
        return register(name, 2);
    }

    public RobitSkinRegistryObject<RobitSkin> register(String name, int variants) {
        ResourceLocation[] textures = new ResourceLocation[variants];
        for (int variant = 0; variant < variants; variant++) {
            if (variant == 0) {
                textures[variant] = new ResourceLocation(modid, name);
            } else {
                textures[variant] = new ResourceLocation(modid, name + (variant + 1));
            }
        }
        return register(name, textures);
    }

    public RobitSkinRegistryObject<RobitSkin> register(String name, ResourceLocation... textures) {
        return register(name, () -> new RobitSkin(textures));
    }

    public <ROBIT_SKIN extends RobitSkin> RobitSkinRegistryObject<ROBIT_SKIN> register(String name, Supplier<? extends ROBIT_SKIN> sup) {
        return register(name, sup, RobitSkinRegistryObject::new);
    }
}