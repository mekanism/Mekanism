package mekanism.common.integration.crafttweaker.bracket;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.BracketValidator;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_BRACKET_VALIDATORS)
public class CrTBracketValidators {

    /**
     * Validates if there is a {@link Chemical} with the given registry name.
     *
     * @param tokens The resource location to validate.
     *
     * @return {@code true} if valid, {@code false} otherwise.
     */
    @ZenCodeType.Method
    @BracketValidator(CrTConstants.BRACKET_CHEMICAL)
    public static boolean validateChemicalStack(String tokens) {
        return validate(CrTConstants.BRACKET_CHEMICAL, tokens, MekanismAPI.CHEMICAL_REGISTRY_NAME);
    }

    /**
     * Validates if there is a {@link mekanism.api.robit.RobitSkin} with the given registry name.
     *
     * @param tokens The resource location to validate.
     *
     * @return {@code true} if valid, {@code false} otherwise.
     */
    @ZenCodeType.Method
    @BracketValidator(CrTConstants.BRACKET_ROBIT_SKIN)
    public static boolean validateRobitSkin(String tokens) {
        return validate(CrTConstants.BRACKET_ROBIT_SKIN, tokens, MekanismAPI.ROBIT_SKIN_REGISTRY_NAME);
    }

    /**
     * Validates if there is a {@link mekanism.api.gear.ModuleData} with the given registry name.
     *
     * @param tokens The resource location to validate.
     *
     * @return {@code true} if valid, {@code false} otherwise.
     */
    @ZenCodeType.Method
    @BracketValidator(CrTConstants.BRACKET_MODULE_DATA)
    public static boolean validateModuleData(String tokens) {
        return validate(CrTConstants.BRACKET_MODULE_DATA, tokens, MekanismAPI.MODULE_REGISTRY_NAME);
    }

    private static boolean validate(String bracket, String tokens, ResourceKey<? extends Registry<?>> registryKey) {
        ResourceLocation registryName = ResourceLocation.tryParse(tokens);
        if (registryName == null) {
            CrTConstants.CRT_LOGGER.error("Could not get BEP <{}:{}>. Syntax is <{}:modid:{}_name>", bracket, tokens, bracket, bracket);
            return false;
        }
        Optional<Registry<Object>> registry = CraftTweakerAPI.getAccessibleElementsProvider()
              .registryAccess()
              .registry(registryKey);
        if (registry.isEmpty() || registry.get().containsKey(registryName)) {
            return true;
        }
        String typeName = bracket.replace("_", " ");
        CrTConstants.CRT_LOGGER.error("Could not get {} for <{}:{}>, {} does not appear to exist!", typeName, bracket, tokens, typeName);
        return false;
    }
}