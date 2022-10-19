package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.providers.IBaseProvider;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = IBaseProvider.class, zenCodeName = CrTConstants.CLASS_BASE_PROVIDER)
public class CrTBaseProvider {

    /**
     * Gets the registry name of the element represented by this provider.
     *
     * @return Registry name.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("registryName")
    public static ResourceLocation getRegistryName(IBaseProvider internal) {
        return internal.getRegistryName();
    }
}