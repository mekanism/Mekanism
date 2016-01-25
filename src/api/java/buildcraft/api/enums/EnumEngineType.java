package buildcraft.api.enums;

import java.util.Locale;

import net.minecraft.util.IStringSerializable;

public enum EnumEngineType implements IStringSerializable {
    WOOD("core", "wood"),
    STONE("energy", "stone"),
    IRON("energy", "iron"),
    CREATIVE("energy", "creative");

    public final String resourceLocation;

    EnumEngineType(String mod, String loc) {
        resourceLocation = "buildcraft" + mod + ":blocks/engine/inv/" + loc + "/";
    }

    @Override
    public String getName() {
        return name();
    }

    public String getModelName() {
        return getName().toLowerCase(Locale.ROOT);
    }
}
