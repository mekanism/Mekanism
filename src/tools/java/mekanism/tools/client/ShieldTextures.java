package mekanism.tools.client;

import mekanism.tools.common.MekanismTools;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;

public enum ShieldTextures {
    BRONZE("bronze"),
    LAPIS_LAZULI("lapis_lazuli"),
    OSMIUM("osmium"),
    REFINED_GLOWSTONE("refined_glowstone"),
    REFINED_OBSIDIAN("refined_obsidian"),
    STEEL("steel");

    private final RenderMaterial base;

    ShieldTextures(String name) {
        base = material("item/" + name + "/shield");
    }

    public RenderMaterial getBase() {
        return base;
    }

    private static RenderMaterial material(String path) {
        return new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, MekanismTools.rl(path));
    }
}