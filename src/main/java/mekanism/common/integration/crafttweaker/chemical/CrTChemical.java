package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.common.integration.crafttweaker.CrTConstants;

@ZenRegister
@NativeMethod(name = "isEmptyType", parameters = {}, getterName = "empty")
@NativeMethod(name = "isHidden", parameters = {}, getterName = "hidden")
@NativeMethod(name = "getTint", parameters = {}, getterName = "tint")
@NativeMethod(name = "getColorRepresentation", parameters = {}, getterName = "colorRepresentation")
@NativeMethod(name = "getAttributes", parameters = {}, getterName = "attributes")
@NativeMethod(name = "addAttribute", parameters = ChemicalAttribute.class)
@NativeTypeRegistration(value = Chemical.class, zenCodeName = CrTConstants.CLASS_CHEMICAL)
public class CrTChemical {

    private CrTChemical() {
    }
}