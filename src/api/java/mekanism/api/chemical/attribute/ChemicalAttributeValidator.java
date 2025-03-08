package mekanism.api.chemical.attribute;

import java.util.Set;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.datamaps.chemical.attribute.IChemicalAttribute;
import net.minecraft.core.Holder;

public interface ChemicalAttributeValidator {//TODO - 1.22: Re-evaluate how this class functions

    ChemicalAttributeValidator DEFAULT = new ChemicalAttributeValidatorLegacyAdapter() {
        @Override
        public boolean validate(IChemicalAttribute attr) {
            return !attr.needsValidation();
        }

        @Override
        public boolean process(Chemical chemical) {
            return !chemical.hasAttributesWithValidation();
        }
    };
    ChemicalAttributeValidator ALWAYS_ALLOW = new ChemicalAttributeValidatorLegacyAdapter() {
        @Override
        public boolean validate(IChemicalAttribute attr) {
            return true;
        }

        @Override
        public boolean process(Chemical chemical) {
            return true;
        }
    };

    /**
     * Whether a certain attribute is considered valid by the caller.
     *
     * @param attribute attribute to check
     *
     * @return if the attribute is valid
     * @deprecated Use {@link #validate(IChemicalAttribute)} instead
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    boolean validate(ChemicalAttribute attribute);

    /**
     * Whether a certain attribute is considered valid by the caller.
     *
     * @param attribute attribute to check
     *
     * @return if the attribute is valid
     *
     * @since 10.7.11
     */
    default boolean validate(IChemicalAttribute attribute) {
        return validate(attribute.toLegacyAttribute());
    }

    /**
     * Determines if a Chemical is considered valid for this validator.
     *
     * @param chemical holder representing the chemical to test
     *
     * @return if the chemical is valid
     *
     * @since 10.7.11
     */
    default boolean process(Holder<Chemical> chemical) {
        return process(chemical.value());
    }

    /**
     * Determines if a Chemical is considered valid for this validator.
     *
     * @param chemical chemical to test
     *
     * @return if the chemical is valid
     *
     * @since 10.2.3
     */
    default boolean process(Chemical chemical) {
        for (ChemicalAttribute chemicalAttribute : chemical.getAttributes()) {
            if (!validate(chemicalAttribute)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if a ChemicalStack is considered valid for this validator.
     *
     * @param stack stack to test
     *
     * @return if the stack is valid
     *
     * @since 10.2.3
     */
    default boolean process(ChemicalStack stack) {
        return process(stack.getChemicalHolder());
    }

    /**
     * Creates a simple attribute validator which accepts any attributes that don't require validation, and any attributes provided in the parameters.
     *
     * @param validAttributes attributes which can be accepted
     *
     * @return simple attribute validator
     */
    @SafeVarargs
    @SuppressWarnings("removal")
    static ChemicalAttributeValidator create(Class<? extends ChemicalAttribute>... validAttributes) {
        return new SimpleAttributeValidator(validAttributes, true);
    }

    /**
     * Creates a simple attribute validator which accepts only attributes provided in the parameters.
     *
     * @param validAttributes attributes which can be accepted
     *
     * @return simple attribute validator
     */
    @SafeVarargs
    @SuppressWarnings("removal")
    static ChemicalAttributeValidator createStrict(Class<? extends ChemicalAttribute>... validAttributes) {
        return new SimpleAttributeValidator(validAttributes, false);
    }

    /**
     * Helper interface for prioritizing checking against modern attributes before checking against only the in code legacy attributes.
     * @since 10.7.11
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    interface ChemicalAttributeValidatorLegacyAdapter extends ChemicalAttributeValidator {

        @Override
        @Deprecated(forRemoval = true, since = "10.7.11")
        default boolean validate(ChemicalAttribute attribute) {
            return validate((IChemicalAttribute) attribute);
        }

        @Override
        boolean validate(IChemicalAttribute attribute);

        @Override
        default boolean process(Chemical chemical) {
            for (IChemicalAttribute attribute : chemical.getModernAttributes()) {
                if (!validate(attribute)) {
                    return false;
                }
            }
            for (ChemicalAttribute chemicalAttribute : chemical.getLegacyAttributes()) {
                IChemicalAttribute modernVersion = chemicalAttribute.asModern();
                if (modernVersion != null) {
                    //Try to get the modern version for validation
                    if (!validate(modernVersion)) {
                        return false;
                    }
                } else if (!validate(chemicalAttribute)) {
                    //If that fails just validate against the old version
                    return false;
                }
            }
            return true;
        }
    }

    @SuppressWarnings("removal")//Note: As some mods do make use of this validator, we just let it keep acting on legacy attributes
    class SimpleAttributeValidator implements ChemicalAttributeValidator {

        private final Set<Class<? extends ChemicalAttribute>> validTypes;
        private final boolean allowNoValidation;

        SimpleAttributeValidator(Class<? extends ChemicalAttribute>[] attributeTypes, boolean allowNoValidation) {
            this.validTypes = Set.of(attributeTypes);
            this.allowNoValidation = allowNoValidation;
        }

        @Override
        public boolean validate(ChemicalAttribute attribute) {
            return (allowNoValidation && !attribute.needsValidation()) || validTypes.contains(attribute.getClass());
        }
    }
}
