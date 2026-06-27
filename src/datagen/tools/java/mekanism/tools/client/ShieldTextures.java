package mekanism.tools.client;

import mekanism.tools.common.MekanismTools;
import net.minecraft.resources.Identifier;

public enum ShieldTextures {
    BRONZE("bronze"),
    LAPIS_LAZULI("lapis_lazuli"),
    OSMIUM("osmium"),
    REFINED_GLOWSTONE("refined_glowstone"),
    REFINED_OBSIDIAN("refined_obsidian"),
    STEEL("steel");

    private final Identifier texture;

    ShieldTextures(String texture) {
        this.texture = MekanismTools.rl(texture);
    }

    public Identifier getTexture() {
        return texture;
    }
}