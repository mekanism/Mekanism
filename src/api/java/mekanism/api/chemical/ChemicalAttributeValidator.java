package mekanism.api.chemical;

import java.util.Set;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.chemical.attribute.IChemicalAttribute;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

@FunctionalInterface
public interface ChemicalAttributeValidator {//TODO - 26.2: Re-evaluate how this class functions

    ChemicalAttributeValidator DEFAULT = attr -> !attr.needsValidation();
    ChemicalAttributeValidator ALWAYS_ALLOW = new ChemicalAttributeValidator() {
        @Override
        public boolean validate(IChemicalAttribute attr) {
            return true;
        }

        @Override
        public boolean process(ChemicalResource chemical) {
            return true;
        }
    };

    /// Whether a certain attribute is considered valid by the caller.
    ///
    /// @param attribute attribute to check
    ///
    /// @return if the attribute is valid
    ///
    /// @since 10.7.11
    boolean validate(IChemicalAttribute attribute);

    /// Determines if an instanced of a chemical is considered valid for this validator.
    ///
    /// @param instance instance to test
    ///
    /// @return if the instance is valid
    ///
    /// @since 10.8.0
    default boolean process(ChemicalResource instance) {
        for (DataMapType<Chemical, ? extends IChemicalAttribute> attributeType : IMekanismDataMapTypes.INSTANCE.chemicalAttributeTypes()) {
            IChemicalAttribute attribute = instance.getData(attributeType);
            if (attribute != null && !validate(attribute)) {
                return false;
            }
        }
        return true;
    }

    /// Creates a simple attribute validator which accepts any attributes that don't require validation, and any attributes provided in the parameters.
    ///
    /// @param validAttributes attributes which can be accepted
    ///
    /// @return simple attribute validator
    @SafeVarargs
    static ChemicalAttributeValidator create(Class<? extends IChemicalAttribute>... validAttributes) {
        return new SimpleAttributeValidator(validAttributes, true);
    }

    /// Creates a simple attribute validator which accepts only attributes provided in the parameters.
    ///
    /// @param validAttributes attributes which can be accepted
    ///
    /// @return simple attribute validator
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
