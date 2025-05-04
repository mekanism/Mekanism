package mekanism.common.lib.radiation;

public record LevelAndMaxMagnitude(double level, double maxMagnitude) {

    public static final LevelAndMaxMagnitude BASELINE = new LevelAndMaxMagnitude(RadiationManager.BASELINE, RadiationManager.BASELINE);
}
