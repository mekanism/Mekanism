package mekanism.additions.common.entity.baby;

import com.mojang.serialization.Codec;
import java.util.Locale;
import javax.annotation.Nonnull;
import net.minecraft.util.StringRepresentable;

public enum BabyType implements StringRepresentable {
    CREEPER,
    ENDERMAN,
    SKELETON,
    STRAY,
    WITHER_SKELETON;

    public static Codec<BabyType> CODEC = StringRepresentable.fromEnum(BabyType::values);

    @Nonnull
    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}