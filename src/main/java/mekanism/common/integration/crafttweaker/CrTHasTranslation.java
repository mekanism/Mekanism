package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.text.IHasTranslationKey;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = IHasTranslationKey.class, zenCodeName = CrTConstants.CLASS_HAS_TRANSLATION)
public class CrTHasTranslation {

    /**
     * Gets the translation key for this object.
     */
    @ZenCodeType.Method
    public static String getTranslationKey(IHasTranslationKey internal) {
        return internal.getTranslationKey();
    }
}