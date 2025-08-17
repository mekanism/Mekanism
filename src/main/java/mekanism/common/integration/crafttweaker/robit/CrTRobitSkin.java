package mekanism.common.integration.crafttweaker.robit;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.MekanismAPI;
import mekanism.api.robit.RobitSkin;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = RobitSkin.class, zenCodeName = CrTConstants.CLASS_ROBIT_SKIN)
public class CrTRobitSkin {

    @ZenCodeType.Method
    public static ResourceLocation getRegistryName(RobitSkin _this) {
        ResourceLocation skinName = CraftTweakerAPI.getAccessibleElementsProvider()
              .registryAccess()
              .registryOrThrow(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME)
              .getKeyOrNull(_this);
        if (skinName == null) {
            throw new IllegalArgumentException("Unregistered robit skin");
        }
        return skinName;
    }
}