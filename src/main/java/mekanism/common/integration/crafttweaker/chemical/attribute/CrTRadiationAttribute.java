package mekanism.common.integration.crafttweaker.chemical.attribute;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.attribute.ChemicalAttributes;
import mekanism.common.integration.crafttweaker.CrTConstants;
import org.openzen.zencode.java.ZenCodeType;

/**
 * This defines the radioactivity of a certain chemical. This attribute <i>requires validation</i>, meaning chemical containers won't be able to accept chemicals with
 * this attribute by default. Radioactivity is measured in Sv/h.
 */
@ZenRegister
@NativeTypeRegistration(value = ChemicalAttributes.Radiation.class, zenCodeName = CrTConstants.CLASS_ATTRIBUTE_RADIATION)
public class CrTRadiationAttribute {

    private CrTRadiationAttribute() {
    }

    /**
     * Creates an attribute representing the radioactivity of a chemical. This attribute <i>requires validation</i>, meaning chemical containers won't be able to accept
     * chemicals with this attribute by default.
     *
     * @param radioactivity Radioactivity of the chemical measured in Sv/h, must be greater than zero.
     *
     * @return Attribute representing the radioactivity of a substance.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ChemicalAttributes.Radiation create(double radioactivity) {
        return new ChemicalAttributes.Radiation(radioactivity);
    }

    /**
     * Gets the radioactivity of this chemical in Sv/h. Each mB of this chemical released into the environment will cause a radiation source of the given radioactivity.
     *
     * @return the radioactivity of this chemical
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("radioactivity")
    public static double getRadioactivity(ChemicalAttributes.Radiation _this) {
        return _this.getRadioactivity();
    }
}