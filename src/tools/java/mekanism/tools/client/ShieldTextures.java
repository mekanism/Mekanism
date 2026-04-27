package mekanism.tools.client;

import mekanism.tools.common.MekanismTools;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.sprite.SpriteId;

public enum ShieldTextures {
    BRONZE("bronze"),
    LAPIS_LAZULI("lapis_lazuli"),
    OSMIUM("osmium"),
    REFINED_GLOWSTONE("refined_glowstone"),
    REFINED_OBSIDIAN("refined_obsidian"),
    STEEL("steel");

    private final SpriteId base;

    ShieldTextures(String name) {
        base = new SpriteId(Sheets.SHIELD_SHEET, MekanismTools.rl("entity/shield/" + name));
    }

    public SpriteId getBase() {
        return base;
    }
}