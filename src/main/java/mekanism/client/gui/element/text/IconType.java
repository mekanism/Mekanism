package mekanism.client.gui.element.text;

import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.resources.Identifier;

public enum IconType {
    DIGITAL(MekanismUtils.getResource(ResourceType.GUI, "digital_text_input.png"), 4, 7);

    private final Identifier icon;
    private final int xSize, ySize;

    IconType(Identifier icon, int xSize, int ySize) {
        this.icon = icon;
        this.xSize = xSize;
        this.ySize = ySize;
    }

    public Identifier getIcon() {
        return icon;
    }

    public int getWidth() {
        return xSize;
    }

    public int getHeight() {
        return ySize;
    }

    public int getOffsetX() {
        return xSize + 4;
    }
}