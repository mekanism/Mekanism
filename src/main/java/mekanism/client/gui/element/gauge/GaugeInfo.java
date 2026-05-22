package mekanism.client.gui.element.gauge;

import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public enum GaugeInfo {
    STANDARD("normal", 2, 2, null),
    BLUE("blue", 2, 2, EnumColor.DARK_BLUE),
    RED("red", 2, 2, EnumColor.DARK_RED),
    YELLOW("yellow", 2, 2, EnumColor.YELLOW),
    ORANGE("orange", 2, 2, EnumColor.ORANGE),
    AQUA("aqua", 2, 2, EnumColor.AQUA);

    @Nullable
    private final EnumColor color;
    private final int sideWidth;
    private final int sideHeight;
    private final Identifier resourceLocation;

    GaugeInfo(String texture, int sideWidth, int sideHeight, @Nullable EnumColor color) {
        this.resourceLocation = Mekanism.rl(texture).withPrefix("gauge/");
        this.sideWidth = sideWidth;
        this.sideHeight = sideHeight;
        this.color = color;
    }

    @Nullable
    public EnumColor getColor() {
        return color;
    }

    public int getSideWidth() {
        return sideWidth;
    }

    public int getSideHeight() {
        return sideHeight;
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