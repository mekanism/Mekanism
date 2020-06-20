package mekanism.api.chemical.attribute;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;

public interface ChemicalAttributeValidator {

    ChemicalAttributeValidator DEFAULT = attr -> !attr.needsValidation();
    ChemicalAttributeValidator ALWAYS_ALLOW = attr -> true;

    /**
     * Whether a certain attribute is considered valid by the caller.
     *
     * @param attribute attribute to check
     *
     * @return if the attribute is valid
     */
    boolean validate(ChemicalAttribute attribute);

    /**
     * Determines if a Chemical is considered valid from a provided attribute validator.
     *
     * @param chemical  chemical to test
     * @param validator validator to use
     *
     * @return if the chemical is valid
     */
    static boolean process(Chemical<?> chemical, ChemicalAttributeValidator validator) {
        return chemical.getAttributes().stream().allMatch(validator::validate);
    }

    /**
     * Determines if a ChemicalStack is considered valid from a provided attribute validator.
     *
     * @param stack     stack to test
     * @param validator validator to use
     *
     * @return if the stack is valid
     */
    static boolean process(ChemicalStack<?> stack, ChemicalAttributeValidator validator) {
        return process(stack.getType(), validator);
    }

    /**
     * Creates a simple attribute validator which accepts any attributes that don't require validation, and any attributes provided in the parameters.
     *
     * @param validAttributes attributes which can be accepted
     *
     * @return simple attribute validator
     */
    @SafeVarargs
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
    static ChemicalAttributeValidator createStrict(Class<? extends ChemicalAttribute>... validAttributes) {
        return new SimpleAttributeValidator(validAttributes, false);
    }

    class SimpleAttributeValidator implements ChemicalAttributeValidator {

        private final Set<Class<? extends ChemicalAttribute>> validTypes;
        private final boolean allowNoValidation;

        SimpleAttributeValidator(Class<? extends ChemicalAttribute>[] attributeTypes, boolean allowNoValidation) {
            this.validTypes = new HashSet<>(Arrays.asList(attributeTypes));
            this.allowNoValidation = allowNoValidation;
        }

        @Override
        public boolean validate(ChemicalAttribute attribute) {
            return validTypes.contains(attribute.getClass()) || (allowNoValidation && !attribute.needsValidation());
        }
    }
}
