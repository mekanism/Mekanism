package mekanism.api.radiation.capability;

/**
 * Simple capability that can be added to things like armor to provide shielding against radiation.
 */
public interface IRadiationShielding {

    /**
     * Gets a percentage representing how much radiation shielding this capability provides.
     *
     * @return Radiation shielding (0.0 to 1.0).
     */
    double getRadiationShielding();
}