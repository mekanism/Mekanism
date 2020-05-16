package mekanism.client.gui.element.slot;

import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public enum SlotType {
    NORMAL("normal.png", 18, 18),
    DIGITAL("digital.png", 18, 18),
    POWER("power.png", 18, 18),
    EXTRA("extra.png", 18, 18),
    INPUT("input.png", 18, 18),
    INPUT_2("input_2.png", 18, 18),
    OUTPUT("output.png", 18, 18),
    OUTPUT_2("output_2.png", 18, 18),
    OUTPUT_WIDE("output_wide.png", 42, 26),
    OUTPUT_LARGE("output_large.png", 36, 54),
    ORE("ore.png", 18, 18),
    INNER_HOLDER_SLOT("inner_holder_slot.png", 18, 18);

    private final ResourceLocation texture;
    private final int width;
    private final int height;

    SlotType(String texture, int width, int height) {
        this.texture = MekanismUtils.getResource(ResourceType.GUI_SLOT, texture);
        this.width = width;
        this.height = height;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public static SlotType get(DataType type) {
        switch (type) {
            case INPUT:
            case INPUT_1:
                return INPUT;
            case INPUT_2:
                return INPUT_2;
            case OUTPUT:
            case OUTPUT_1:
                return OUTPUT;
            case OUTPUT_2:
                return OUTPUT_2;
            case ENERGY:
                return POWER;
            case EXTRA:
                return EXTRA;
            default:
                return NORMAL;
        }
    }
}