package mekanism.common.integration.crafttweaker.chemical.attribute;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.datamaps.chemical.attribute.IChemicalAttribute;
import mekanism.common.integration.crafttweaker.CrTConstants;
import org.openzen.zencode.java.ZenCodeType;

/**
 * All chemical attributes should inherit from this class. No specific implementation is required.
 */
@ZenRegister
@NativeTypeRegistration(value = IChemicalAttribute.class, zenCodeName = CrTConstants.CLASS_ATTRIBUTE_CHEMICAL)
public class CrTChemicalAttribute {

    private CrTChemicalAttribute() {
    }

    /**
     * If this returns true, chemicals possessing this attribute will not be accepted by any prefab handlers by default unless validated.
     *
     * @return if chemicals with this attribute require validation before being accepted
     */
    @ZenCodeType.Method
    public static boolean needsValidation(IChemicalAttribute _this) {
        return _this.needsValidation();
    }
}