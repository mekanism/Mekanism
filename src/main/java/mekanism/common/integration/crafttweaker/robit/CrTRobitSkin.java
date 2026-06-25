package mekanism.common.integration.crafttweaker.robit;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.MekanismRegistries;
import mekanism.api.robit.RobitSkin;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.resources.Identifier;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = RobitSkin.class, zenCodeName = CrTConstants.CLASS_ROBIT_SKIN)
public class CrTRobitSkin {

    @ZenCodeType.Method
    public static Identifier getRegistryName(RobitSkin _this) {
        Identifier skinName = CraftTweakerAPI.getAccessibleElementsProvider()
              .registryAccess()
              .lookupOrThrow(MekanismRegistries.Keys.ROBIT_SKINS)
              .getKeyOrNull(_this);
        if (skinName == null) {
            throw new IllegalArgumentException("Unregistered robit skin");
        }
        return skinName;
    }
}