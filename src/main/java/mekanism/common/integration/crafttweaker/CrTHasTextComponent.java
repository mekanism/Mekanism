package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.network.chat.Component;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = IHasTextComponent.class, zenCodeName = CrTConstants.CLASS_HAS_TEXT_COMPONENT)
public class CrTHasTextComponent {

    /**
     * Gets the text component that represents this object.
     */
    @ZenCodeType.Method
    public static Component getTextComponent(IHasTextComponent _this) {
        return _this.getTextComponent();
    }
}