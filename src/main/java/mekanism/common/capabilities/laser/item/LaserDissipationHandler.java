package mekanism.common.capabilities.laser.item;

import mekanism.api.lasers.ILaserDissipation;

public class LaserDissipationHandler implements ILaserDissipation {

    public static LaserDissipationHandler create(double dissipationPercent, double refractionPercent) {
        if (dissipationPercent < 0 || dissipationPercent > 1) {
            throw new IllegalArgumentException("Dissipation percent must be between zero and one inclusive");
        }
        if (refractionPercent < 0 || refractionPercent > 1) {
            throw new IllegalArgumentException("Refraction percent must be between zero and one inclusive");
        }
        return new LaserDissipationHandler(dissipationPercent, refractionPercent);
    }

    private final double dissipationPercent;
    private final double refractionPercent;

    private LaserDissipationHandler(double dissipationPercent, double refractionPercent) {
        this.dissipationPercent = dissipationPercent;
        this.refractionPercent = refractionPercent;
    }

    @Override
    public double getDissipationPercent() {
        return dissipationPercent;
    }

    @Override
    public double getRefractionPercent() {
        return refractionPercent;
    }
}