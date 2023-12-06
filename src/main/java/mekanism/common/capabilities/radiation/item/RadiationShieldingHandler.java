package mekanism.common.capabilities.radiation.item;

import mekanism.api.radiation.capability.IRadiationShielding;

public class RadiationShieldingHandler implements IRadiationShielding {

    public static RadiationShieldingHandler create(double radiationShielding) {
        if (radiationShielding < 0 || radiationShielding > 1) {
            throw new IllegalArgumentException("Radiation shielding must be between zero and one inclusive");
        }
        return new RadiationShieldingHandler(radiationShielding);
    }

    private final double radiationShielding;

    private RadiationShieldingHandler(double radiationShielding) {
        this.radiationShielding = radiationShielding;
    }

    @Override
    public double getRadiationShielding() {
        return radiationShielding;
    }
}