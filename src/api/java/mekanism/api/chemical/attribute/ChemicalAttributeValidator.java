package mekanism.api.chemical.attribute;

import java.util.Set;
import mekanism.api.chemical.Chemical;
import mekanism.api.datamaps.chemical.attribute.IChemicalAttribute;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;

public interface ChemicalAttributeValidator {//TODO - 26.1: Re-evaluate how this class functions

    ChemicalAttributeValidator DEFAULT = new ChemicalAttributeValidator() {
        @Override
        public boolean validate(IChemicalAttribute attr) {
            return !attr.needsValidation();
        }

        @Override
        public boolean process(Chemical chemical) {
            return !chemical.hasAttributesWithValidation();
        }
    };
    ChemicalAttributeValidator ALWAYS_ALLOW = new ChemicalAttributeValidator() {
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
     *
     * @since 10.7.11
     */
    boolean validate(IChemicalAttribute attribute);

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
        for (IChemicalAttribute chemicalAttribute : chemical.getAttributes()) {
            if (!validate(chemicalAttribute)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if an instanced of a chemical is considered valid for this validator.
     *
     * @param instance instance to test
     *
     * @return if the instance is valid
     *
     * @since 10.2.3
     */
    default boolean process(TypedInstance<Chemical> instance) {
        return process(instance.typeHolder());
    }

    /**
     * Creates a simple attribute validator which accepts any attributes that don't require validation, and any attributes provided in the parameters.
     *
     * @param validAttributes attributes which can be accepted
     *
     * @return simple attribute validator
     */
    @SafeVarargs
    static ChemicalAttributeValidator create(Class<? extends IChemicalAttribute>... validAttributes) {
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
    static ChemicalAttributeValidator createStrict(Class<? extends IChemicalAttribute>... validAttributes) {
        return new SimpleAttributeValidator(validAttributes, false);
    }

    class SimpleAttributeValidator implements ChemicalAttributeValidator {

        private final Set<Class<? extends IChemicalAttribute>> validTypes;
        private final boolean allowNoValidation;

        SimpleAttributeValidator(Class<? extends IChemicalAttribute>[] attributeTypes, boolean allowNoValidation) {
            this.validTypes = Set.of(attributeTypes);
            this.allowNoValidation = allowNoValidation;
        }

        @Override
        public boolean validate(IChemicalAttribute attribute) {
            return (allowNoValidation && !attribute.needsValidation()) || validTypes.contains(attribute.getClass());
        }
    }
}
