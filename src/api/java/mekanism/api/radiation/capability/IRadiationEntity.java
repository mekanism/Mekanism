package mekanism.api.radiation.capability;

/**
 * Base capability definition for handling radiation for entities.
 *
 * @implNote Builtin Mekanism implementations of this are backed by an {@link net.neoforged.neoforge.attachment.AttachmentType AttachmentType< Double >} with the registry
 * name {@code mekanism:radiation}, though it is recommended to interact with this via capabilities.
 */
public interface IRadiationEntity {

    /**
     * Gets the radiation dosage (Sv) of the entity.
     *
     * @return radiation dosage
     */
    double getRadiation();

    /**
     * Applies an additional magnitude of radiation to the entity (Sv).
     *
     * @param magnitude dosage of radiation to apply (Sv)
     */
    void radiate(double magnitude);

    /**
     * Decays the entity's radiation dosage.
     */
    void decay();

    /**
     * Applies radiation effects to the entity, and syncs the capability if needed.
     */
    void update();

    /**
     * Sets the radiation level of the entity to a new value.
     *
     * @param magnitude value to set radiation dosage to
     */
    void set(double magnitude);
}
