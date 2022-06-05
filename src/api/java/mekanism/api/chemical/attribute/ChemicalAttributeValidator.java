package mekanism.api.chemical.attribute;

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
     * Determines if a Chemical is considered valid for this validator.
     *
     * @param chemical chemical to test
     *
     * @return if the chemical is valid
     *
     * @since 10.2.3
     */
    default boolean process(Chemical<?> chemical) {
        return chemical.getAttributes().stream().allMatch(this::validate);
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
    default boolean process(ChemicalStack<?> stack) {
        return process(stack.getType());
    }

    /**
     * Determines if a Chemical is considered valid from a provided attribute validator.
     *
     * @param chemical  chemical to test
     * @param validator validator to use
     *
     * @return if the chemical is valid
     */
    @Deprecated(forRemoval = true, since = "10.2.3")
    static boolean process(Chemical<?> chemical, ChemicalAttributeValidator validator) {
        return validator.process(chemical);
    }

    /**
     * Determines if a ChemicalStack is considered valid from a provided attribute validator.
     *
     * @param stack     stack to test
     * @param validator validator to use
     *
     * @return if the stack is valid
     */
    @Deprecated(forRemoval = true, since = "10.2.3")
    static boolean process(ChemicalStack<?> stack, ChemicalAttributeValidator validator) {
        return validator.process(stack);
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
            this.validTypes = Set.of(attributeTypes);
            this.allowNoValidation = allowNoValidation;
        }

        @Override
        public boolean validate(ChemicalAttribute attribute) {
            return validTypes.contains(attribute.getClass()) || (allowNoValidation && !attribute.needsValidation());
        }
    }
}
