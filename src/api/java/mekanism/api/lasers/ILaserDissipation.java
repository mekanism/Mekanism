package mekanism.api.lasers;

/**
 * Expose as a capability on armor items to allow lasers to be dissipated and/or refracted through entities wearing your armor.
 *
 * @implNote The order in which things are checked for reducing damage is as follows:
 * <ol>
 *     <li>Shield an entity is blocking with</li>
 *     <li>Total dissipation percent of worn armor</li>
 *     <li>Total refraction percent of worn armor</li>
 * </ol>
 * Any damage that isn't blocked, dissipated, or refracted through the entity will be applied to the entity as normal.
 */
public interface ILaserDissipation {

    /**
     * Gets the percentage for how much of a laser's energy this piece of armor will dissipate across it.
     *
     * @return Laser dissipation percentage (0.0 to 1.0).
     *
     * @implNote This value should be between zero and one, but values greater than one will work as well as the total percentage across the worn armor gets capped at
     * one.
     */
    double getDissipationPercent();

    /**
     * Gets the percentage for how much of a laser's energy this piece of armor will be refracted through it.
     *
     * @return Laser refraction percentage (0.0 to 1.0).
     *
     * @implNote This value should be between zero and one, but values greater than one will work as well as the total percentage across the worn armor gets capped at
     * one.
     */
    double getRefractionPercent();
}