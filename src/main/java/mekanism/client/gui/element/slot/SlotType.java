package mekanism.client.gui.element.slot;

import mekanism.common.Mekanism;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.resources.Identifier;

public enum SlotType {
    NORMAL("normal"),
    DARK("dark"),
    POWER("power"),
    EXTRA("extra"),
    INPUT("input"),
    INPUT_2("input_2"),
    OUTPUT("output"),
    OUTPUT_2("output_2"),
    INNER_HOLDER_SLOT("inner_holder_slot");

    public static final int SLOT_SIZE = 18;

    private final Identifier texture;

    SlotType(String texture) {
        this.texture = Mekanism.rl("slot/" + texture);
    }

    public Identifier getTexture() {
        return texture;
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