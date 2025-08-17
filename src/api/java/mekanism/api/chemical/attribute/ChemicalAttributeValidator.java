package mekanism.api.chemical.attribute;

import java.util.Set;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.datamaps.chemical.attribute.IChemicalAttribute;
import net.minecraft.core.Holder;

public interface ChemicalAttributeValidator {//TODO - 1.22: Re-evaluate how this class functions

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
