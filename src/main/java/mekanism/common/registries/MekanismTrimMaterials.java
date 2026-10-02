package mekanism.common.registries;

import mekanism.common.Mekanism;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

public class MekanismTrimMaterials {

    private MekanismTrimMaterials() {
    }

    public static final ResourceKey<TrimMaterial> ANTIMATTER = registryKey("antimatter");
    public static final ResourceKey<TrimMaterial> BRONZE = registryKey("bronze");
    public static final ResourceKey<TrimMaterial> FLUORITE = registryKey("fluorite");
    public static final ResourceKey<TrimMaterial> LEAD = registryKey("lead");
    public static final ResourceKey<TrimMaterial> OSMIUM = registryKey("osmium");
    public static final ResourceKey<TrimMaterial> PLUTONIUM = registryKey("plutonium");
    public static final ResourceKey<TrimMaterial> POLONIUM = registryKey("polonium");
    public static final ResourceKey<TrimMaterial> REFINED_GLOWSTONE = registryKey("refined_glowstone");
    public static final ResourceKey<TrimMaterial> REFINED_OBSIDIAN = registryKey("refined_obsidian");
    public static final ResourceKey<TrimMaterial> STEEL = registryKey("steel");
    public static final ResourceKey<TrimMaterial> TIN = registryKey("tin");
    public static final ResourceKey<TrimMaterial> URANIUM = registryKey("uranium");

    private static ResourceKey<TrimMaterial> registryKey(String id) {
        return ResourceKey.create(Registries.TRIM_MATERIAL, Mekanism.rl(id));
    }

    public static String getTranslationKey(ResourceKey<TrimMaterial> key) {
        return Util.makeDescriptionId("trim_material", key.identifier());
    }
}