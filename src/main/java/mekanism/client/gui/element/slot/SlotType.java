package mekanism.client.gui.element.slot;

import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.resources.ResourceLocation;

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

    private static final ResourceLocation WARNING = MekanismUtils.getResource(ResourceType.GUI_SLOT, "output_warning.png");
    private static final ResourceLocation WIDE_WARNING = MekanismUtils.getResource(ResourceType.GUI_SLOT, "output_wide_warning.png");
    private static final ResourceLocation LARGE_WARNING = MekanismUtils.getResource(ResourceType.GUI_SLOT, "output_large_warning.png");

    private final ResourceLocation texture;
    private final int width;
    private final int height;

    SlotType(String texture, int width, int height) {
        this.texture = MekanismUtils.getResource(ResourceType.GUI_SLOT, texture);
        this.width = width;
        this.height = height;
    }

    public ResourceLocation getWarningTexture() {
        return switch (this) {
            case OUTPUT_WIDE -> WIDE_WARNING;
            case OUTPUT_LARGE -> LARGE_WARNING;
            default -> WARNING;
        };
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
        return switch (type) {
            case INPUT, INPUT_1 -> INPUT;
            case INPUT_2 -> INPUT_2;
            case OUTPUT, OUTPUT_1 -> OUTPUT;
            case OUTPUT_2 -> OUTPUT_2;
            case ENERGY -> POWER;
            case EXTRA -> EXTRA;
            default -> NORMAL;
        };
    }
}