package mekanism.additions.common.entity.baby;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum BabyType implements StringRepresentable {
    CREEPER,
    ENDERMAN,
    SKELETON,
    STRAY,
    WITHER_SKELETON;

    public static final Codec<BabyType> CODEC = StringRepresentable.fromEnum(BabyType::values);

    private final String serializedName;

    BabyType() {
        this.serializedName = name().toLowerCase(Locale.ROOT);
    }

    @NotNull
    @Override
    public String getSerializedName() {
        return serializedName;
    }
}