package mekanism.common.inventory.container.slot;

import mekanism.common.Mekanism;
import net.minecraft.resources.Identifier;

public enum SlotOverlay {
    MINUS("minus"),
    PLUS("plus"),
    POWER("power"),
    INPUT("input"),
    OUTPUT("output"),
    CHECK("check"),
    X("x"),
    FORMULA("formula"),
    UPGRADE("upgrade"),
    SELECT("select"),
    MODULE("module");

    private final Identifier texture;

    SlotOverlay(String texture) {
        this.texture = Mekanism.rl("slot/overlay/" + texture);
    }

    public Identifier getTexture() {
        return texture;
    }
}