package mekanism.common.capabilities.laser.item;

import mekanism.api.lasers.ILaserDissipation;

public class LaserDissipationHandler implements ILaserDissipation {

    public static LaserDissipationHandler create(float dissipationPercent, float refractionPercent) {
        if (dissipationPercent < 0 || dissipationPercent > 1) {
            throw new IllegalArgumentException("Dissipation percent must be between zero and one inclusive");
        }
        if (refractionPercent < 0 || refractionPercent > 1) {
            throw new IllegalArgumentException("Refraction percent must be between zero and one inclusive");
        }
        return new LaserDissipationHandler(dissipationPercent, refractionPercent);
    }

    private final float dissipationPercent;
    private final float refractionPercent;

    private LaserDissipationHandler(float dissipationPercent, float refractionPercent) {
        this.dissipationPercent = dissipationPercent;
        this.refractionPercent = refractionPercent;
    }

    @Override
    public float getDissipationPercent() {
        return dissipationPercent;
    }

    @Override
    public float getRefractionPercent() {
        return refractionPercent;
    }
}