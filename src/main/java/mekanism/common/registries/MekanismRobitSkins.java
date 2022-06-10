package mekanism.common.registries;

import com.google.common.collect.ImmutableMap;
import mekanism.api.robit.RobitSkin;
import mekanism.common.Mekanism;
import mekanism.common.entity.RobitPrideSkinData;
import mekanism.common.registration.impl.RobitSkinDeferredRegister;
import mekanism.common.registration.impl.RobitSkinRegistryObject;

import java.util.HashMap;
import java.util.Map;

public class MekanismRobitSkins {

    private MekanismRobitSkins() {
    }

    public static final RobitSkinDeferredRegister ROBIT_SKINS = new RobitSkinDeferredRegister(Mekanism.MODID);

    public static final RobitSkinRegistryObject<RobitSkin> BASE = ROBIT_SKINS.register("robit");

    public static final Map<RobitPrideSkinData, RobitSkinRegistryObject<RobitSkin>> PRIDE_SKINS;

    static {
        Map<RobitPrideSkinData, RobitSkinRegistryObject<RobitSkin>> tempMap = new HashMap<>();
        for (RobitPrideSkinData data: RobitPrideSkinData.values()) {
            tempMap.put(data, ROBIT_SKINS.register(data.lowerCaseName(), data.getColor().length));
        }
        PRIDE_SKINS = ImmutableMap.copyOf(tempMap);
    }
}