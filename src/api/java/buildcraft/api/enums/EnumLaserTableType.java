package buildcraft.api.enums;

import net.minecraft.util.IStringSerializable;

public enum EnumLaserTableType implements IStringSerializable {
    ASSEMBLY_TABLE,
    ADVANCED_CRAFTING_TABLE,
    INTEGRATION_TABLE,
    CHARGING_TABLE,
    PROGRAMMING_TABLE;

    @Override
    public String getName() {
        return name();
    }
}
