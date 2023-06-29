package mekanism.tools.client;

import mekanism.tools.common.MekanismTools;
import net.minecraft.client.renderer.Sheets;
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
        base = new Material(Sheets.SHIELD_SHEET, MekanismTools.rl("entity/shield/" + name));
    }

    public Material getBase() {
        return base;
    }
}