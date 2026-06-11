package mekanism.common.lib.radiation;

import com.mojang.serialization.Codec;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.text.EnumColor;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.StringRepresentable;

/**
 * Created by Thiakil on 4/05/2025.
 */
public enum RadiationScale implements StringRepresentable {
    NONE,
    LOW,
    MEDIUM,
    ELEVATED,
    HIGH,
    EXTREME;

    /**
     * Get the corresponding RadiationScale from an equivalent dose rate (Sv/h)
     */
    public static RadiationScale get(double magnitude) {
        if (magnitude < 0.00001) { // 10 uSv/h
            return NONE;
        } else if (magnitude < 0.001) { // 1 mSv/h
            return LOW;
        } else if (magnitude < 0.1) { // 100 mSv/h
            return MEDIUM;
        } else if (magnitude < 10) { // 100 Sv/h
            return ELEVATED;
        } else if (magnitude < 100) {
            return HIGH;
        }
        return EXTREME;
    }

    /**
     * For both Sv and Sv/h.
     */
    public static EnumColor getSeverityColor(double magnitude) {
        if (magnitude <= IRadiationManager.INSTANCE.baselineRadiation()) {
            return EnumColor.BRIGHT_GREEN;
        } else if (magnitude < 0.00001) { // 10 uSv/h
            return EnumColor.GRAY;
        } else if (magnitude < 0.001) { // 1 mSv/h
            return EnumColor.YELLOW;
        } else if (magnitude < 0.1) { // 100 mSv/h
            return EnumColor.ORANGE;
        } else if (magnitude < 10) { // 100 Sv/h
            return EnumColor.RED;
        }
        return EnumColor.DARK_RED;
    }

    private static final double LOG_BASELINE = Math.log10(RadiationManager.get().minRadiationMagnitude());
    private static final double LOG_MAX = Math.log10(100); // 100 Sv
    private static final double SCALE = LOG_MAX - LOG_BASELINE;
    public static final Codec<RadiationScale> CODEC = StringRepresentable.fromEnum(RadiationScale::values);

    /**
     * Gets the severity of a dose (between 0 and 1) from a provided dosage in Sv.
     */
    public static double getScaledDoseSeverity(double magnitude) {
        if (magnitude < IRadiationManager.INSTANCE.minRadiationMagnitude()) {
            return 0;
        }
        return Math.clamp((-LOG_BASELINE + Math.log10(magnitude)) / SCALE, 0, 1);
    }

    public SoundEvent getSoundEvent() {
        return switch (this) {
            case LOW -> MekanismSounds.GEIGER_SLOW.get();
            case MEDIUM -> MekanismSounds.GEIGER_MEDIUM.get();
            case ELEVATED, HIGH -> MekanismSounds.GEIGER_ELEVATED.get();
            case EXTREME -> MekanismSounds.GEIGER_FAST.get();
            case NONE -> throw new IllegalStateException("Method not expected to be called for no radiation");
        };
    }

    @Override
    public String getSerializedName() {
        return name();
    }
}
