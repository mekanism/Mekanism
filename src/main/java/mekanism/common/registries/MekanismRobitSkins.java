package mekanism.common.registries;

import mekanism.api.robit.RobitSkin;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.RobitSkinDeferredRegister;
import mekanism.common.registration.impl.RobitSkinRegistryObject;

public class MekanismRobitSkins {

    private MekanismRobitSkins() {
    }

    public static final RobitSkinDeferredRegister ROBIT_SKINS = new RobitSkinDeferredRegister(Mekanism.MODID);

    public static final RobitSkinRegistryObject<RobitSkin> BASE = ROBIT_SKINS.register("robit");
    public static final RobitSkinRegistryObject<RobitSkin> LESBIAN = ROBIT_SKINS.register("lesbian", 8);
    public static final RobitSkinRegistryObject<RobitSkin> PRIDE = ROBIT_SKINS.register("pride", 6);
    public static final RobitSkinRegistryObject<RobitSkin> TRANS = ROBIT_SKINS.register("trans", 4);
}