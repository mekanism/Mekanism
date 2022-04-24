package mekanism.tools.client;

import mekanism.tools.common.MekanismTools;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;

public enum ShieldTextures {
    BRONZE("bronze"),
    LAPIS_LAZULI("lapis_lazuli"),
    OSMIUM("osmium"),
    REFINED_GLOWSTONE("refined_glowstone"),
    REFINED_OBSIDIAN("refined_obsidian"),
    STEEL("steel");

    private final Material base;

    ShieldTextures(String name) {
        base = material("item/" + name + "/shield");
    }

    public Material getBase() {
        return base;
    }

    private static Material material(String path) {
        return new Material(TextureAtlas.LOCATION_BLOCKS, MekanismTools.rl(path));
    }
}