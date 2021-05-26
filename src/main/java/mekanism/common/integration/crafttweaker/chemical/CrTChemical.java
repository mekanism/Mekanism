package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.Chemical;
import mekanism.common.integration.crafttweaker.CrTConstants;

//TODO - 10.1: Uncomment these native methods and bump min CrT version to one that contains the annotation
@ZenRegister
//@NativeMethod(name = "isEmptyType", parameters = {}, getterName = "empty")
//@NativeMethod(name = "isHidden", parameters = {}, getterName = "hidden")
//@NativeMethod(name = "getTags", parameters = {}, getterName = "tags")
//@NativeMethod(name = "getTranslationKey", parameters = {})
@NativeTypeRegistration(value = Chemical.class, zenCodeName = CrTConstants.CLASS_CHEMICAL)
public class CrTChemical {

    private CrTChemical() {
    }
}