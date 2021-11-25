package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeMethod;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.Chemical;
import mekanism.common.integration.crafttweaker.CrTConstants;

@ZenRegister
@NativeMethod(name = "isEmptyType", parameters = {}, getterName = "empty")
@NativeMethod(name = "isHidden", parameters = {}, getterName = "hidden")
@NativeMethod(name = "getTags", parameters = {}, getterName = "tags")
@NativeTypeRegistration(value = Chemical.class, zenCodeName = CrTConstants.CLASS_CHEMICAL)
public class CrTChemical {

    private CrTChemical() {
    }
}