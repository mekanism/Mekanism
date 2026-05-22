package mekanism.client.gui.element.gauge;

import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public enum GaugeInfo {
    STANDARD("normal", null),
    BLUE("blue", EnumColor.DARK_BLUE),
    RED("red", EnumColor.DARK_RED),
    YELLOW("yellow", EnumColor.YELLOW),
    ORANGE("orange", EnumColor.ORANGE),
    AQUA("aqua", EnumColor.AQUA);

    @Nullable
    private final EnumColor color;
    private final Identifier resourceLocation;

    GaugeInfo(String texture, @Nullable EnumColor color) {
        this.resourceLocation = Mekanism.rl(texture).withPrefix("gauge/");
        this.color = color;
    }

    @Nullable
    public EnumColor getColor() {
        return color;
    }

    public Identifier getResourceLocation() {
        return resourceLocation;
    }

    public static GaugeInfo get(DataType type) {
        return switch (type) {
            case OUTPUT, OUTPUT_1 -> BLUE;
            case INPUT, INPUT_1 -> RED;
            case OUTPUT_2 -> AQUA;
            case INPUT_2 -> ORANGE;
            default -> STANDARD;
        };
    }
}