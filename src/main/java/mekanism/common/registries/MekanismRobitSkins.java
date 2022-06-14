package mekanism.common.registries;

import mekanism.api.robit.RobitSkin;
import mekanism.common.Mekanism;
import mekanism.common.entity.skins.AllayRobitSkin;
import mekanism.common.entity.RobitPrideSkinData;
import mekanism.common.registration.impl.RobitSkinDeferredRegister;
import mekanism.common.registration.impl.RobitSkinRegistryObject;
import net.minecraft.Util;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class MekanismRobitSkins {

    private MekanismRobitSkins() {
    }

    public static final RobitSkinDeferredRegister ROBIT_SKINS = new RobitSkinDeferredRegister(Mekanism.MODID);

    public static final RobitSkinRegistryObject<RobitSkin> BASE = ROBIT_SKINS.register("robit");

    public static final RobitSkinRegistryObject<RobitSkin> ALLAY = ROBIT_SKINS.register("allay", AllayRobitSkin::new);

    public static final Map<RobitPrideSkinData, RobitSkinRegistryObject<RobitSkin>> PRIDE_SKINS = Util.make(() -> {
        Map<RobitPrideSkinData, RobitSkinRegistryObject<RobitSkin>> internal = new EnumMap<>(RobitPrideSkinData.class);
        for (RobitPrideSkinData data: RobitPrideSkinData.values()) {
            internal.put(data, ROBIT_SKINS.register(data.lowerCaseName(), data.getColor().length));
        }
        return Collections.unmodifiableMap(internal);
    });
}