package mekanism.client.render.item.gear;

import net.minecraft.util.StringRepresentable;

enum GearArmorType implements StringRepresentable {
    UNARMORED,
    ARMORED;

    public static final EnumCodec<GearArmorType> CODEC = StringRepresentable.fromEnum(GearArmorType::values);

    @Override
    public String getSerializedName() {
        return name();
    }
}
