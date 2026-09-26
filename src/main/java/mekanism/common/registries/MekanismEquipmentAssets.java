package mekanism.common.registries;

import mekanism.common.Mekanism;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public class MekanismEquipmentAssets {

    public static final ResourceKey<EquipmentAsset> HDPE_ELYTRA = createId("hdpe_elytra");
    public static final ResourceKey<EquipmentAsset> HAZMAT = createId("hazmat");
    //Exists as markers for gender integration
    public static final ResourceKey<EquipmentAsset> SCUBA_GEAR = createId("scuba_gear");
    public static final ResourceKey<EquipmentAsset> JETPACK = createId("jetpack");
    public static final ResourceKey<EquipmentAsset> ARMORED_JETPACK = createId("armored_jetpack");
    public static final ResourceKey<EquipmentAsset> MEKASUIT = createId("mekasuit");

    private static ResourceKey<EquipmentAsset> createId(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Mekanism.rl(name));
    }
}
