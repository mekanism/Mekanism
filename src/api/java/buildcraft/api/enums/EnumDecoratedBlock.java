package buildcraft.api.enums;

import java.util.Locale;

import net.minecraft.util.IStringSerializable;

public enum EnumDecoratedBlock implements IStringSerializable {
    DESTROY(0),
    BLUEPRINT(10),
    TEMPLATE(10),
    PAPER(10),
    LEATHER(10),
    LASER_BACK(0);

    public static final EnumDecoratedBlock[] VALUES = values();

    public final int lightValue;

    EnumDecoratedBlock(int lightValue) {
        this.lightValue = lightValue;
    }

    @Override
    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static EnumDecoratedBlock fromMeta(int meta) {
        if (meta < 0 || meta >= VALUES.length) {
            return EnumDecoratedBlock.DESTROY;
        }
        return VALUES[meta];
    }
}
