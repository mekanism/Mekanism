package mekanism.api.chemical.attribute;

/**
 * All chemical attributes should inherit from from this class. No specific implementation is
 * required.
 *
 * @author aidancbrady
 *
 */
public abstract class ChemicalAttribute {

    /**
     * If this returns true, chemicals possessing this attribute will not be accepted by any prefab
     * handlers by default unless validated.
     *
     * @return if chemicals with this attribute require validation before being accepted
     */
    public boolean needsValidation() {
        return false;
    }
}
