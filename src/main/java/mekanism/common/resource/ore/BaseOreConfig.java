package mekanism.common.resource.ore;

import mekanism.common.world.height.HeightShape;

public record BaseOreConfig(String name, int perChunk, float discardChanceOnAirExposure, int maxVeinSize, HeightShape shape, OreAnchor min, OreAnchor max,
                            int plateau) {

    public BaseOreConfig {
        if (plateau > 0 && shape != HeightShape.TRAPEZOID) {
            throw new IllegalArgumentException("Plateau are only supported by trapezoid shape");
        }
    }

    public BaseOreConfig(String name, int perChunk, float discardChanceOnAirExposure, int maxVeinSize, HeightShape shape, OreAnchor min, OreAnchor max) {
        this(name, perChunk, discardChanceOnAirExposure, maxVeinSize, shape, min, max, 0);
    }
}