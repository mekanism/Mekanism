package mekanism.tools.client;

import mekanism.tools.common.MekanismTools;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.texture.AtlasTexture;

public enum ShieldTextures {
    BRONZE("bronze"),
    LAPIS_LAZULI("lapis_lazuli"),
    OSMIUM("osmium"),
    REFINED_GLOWSTONE("refined_glowstone"),
    REFINED_OBSIDIAN("refined_obsidian"),
    STEEL("steel");

    private final Material base;
    private final Material noPattern;

    ShieldTextures(String name) {
        base = material("item/" + name + "/shield");
        noPattern = material("item/" + name + "/shield_no_pattern");
    }

    public Material getBase() {
        return base;
    }

    public Material getNoPattern() {
        return noPattern;
    }

    private static Material material(String path) {
        return new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE, MekanismTools.rl(path));
    }
}