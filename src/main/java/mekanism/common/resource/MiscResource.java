package mekanism.common.resource;

import mekanism.common.registries.MekanismTrimMaterials;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import org.jspecify.annotations.Nullable;

public enum MiscResource implements IResource {
    BRONZE("bronze", MekanismTrimMaterials.BRONZE),
    CARBON("carbon"),
    CHARCOAL("charcoal"),
    COAL("coal"),
    DIAMOND("diamond", TrimMaterials.DIAMOND),
    EMERALD("emerald", TrimMaterials.EMERALD),
    NETHERITE("netherite", TrimMaterials.NETHERITE),
    LAPIS_LAZULI("lapis_lazuli", TrimMaterials.LAPIS),
    LITHIUM("lithium"),
    OBSIDIAN("obsidian"),
    QUARTZ("quartz", TrimMaterials.QUARTZ),
    REDSTONE("redstone", TrimMaterials.REDSTONE),
    REFINED_GLOWSTONE("refined_glowstone", MekanismTrimMaterials.REFINED_GLOWSTONE),
    REFINED_OBSIDIAN("refined_obsidian", MekanismTrimMaterials.REFINED_OBSIDIAN),
    STEEL("steel", MekanismTrimMaterials.STEEL),
    SULFUR("sulfur"),
    FLUORITE("fluorite", MekanismTrimMaterials.FLUORITE),;

    private final String registrySuffix;
    @Nullable
    private final ResourceKey<TrimMaterial> trimMaterial;

    MiscResource(String registrySuffix) {
        this(registrySuffix, null);
    }

    MiscResource(String registrySuffix, @Nullable ResourceKey<TrimMaterial> trimMaterial) {
        this.registrySuffix = registrySuffix;
        this.trimMaterial = trimMaterial;
    }

    @Override
    public String getRegistrySuffix() {
        return registrySuffix;
    }

    @Override
    public Item.Properties modifyProperties(Item.Properties properties, @Nullable ResourceType resourceType) {
        if (trimMaterial != null && (resourceType == null || resourceType == ResourceType.INGOT)) {
            properties = properties.trimMaterial(trimMaterial);
        }
        if (this == REFINED_OBSIDIAN) {
            properties = properties.fireResistant();
        }
        return properties;
    }
}