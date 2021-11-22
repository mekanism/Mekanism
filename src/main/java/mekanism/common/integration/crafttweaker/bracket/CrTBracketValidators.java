package mekanism.common.integration.crafttweaker.bracket;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.BracketValidator;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.MekanismAPI;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_BRACKET_VALIDATORS)
public class CrTBracketValidators {

    /**
     * Validates if there is a {@link mekanism.api.chemical.gas.Gas} with the given registry name.
     *
     * @param tokens The resource location to validate.
     *
     * @return {@code true} if valid, {@code false} otherwise.
     */
    @ZenCodeType.Method
    @BracketValidator(CrTConstants.BRACKET_GAS)
    public static boolean validateGasStack(String tokens) {
        return validate(CrTConstants.BRACKET_GAS, tokens, MekanismAPI.gasRegistry());
    }

    /**
     * Validates if there is a {@link mekanism.api.chemical.infuse.InfuseType} with the given registry name.
     *
     * @param tokens The resource location to validate.
     *
     * @return {@code true} if valid, {@code false} otherwise.
     */
    @ZenCodeType.Method
    @BracketValidator(CrTConstants.BRACKET_INFUSE_TYPE)
    public static boolean validateInfusionStack(String tokens) {
        return validate(CrTConstants.BRACKET_INFUSE_TYPE, tokens, MekanismAPI.infuseTypeRegistry());
    }

    /**
     * Validates if there is a {@link mekanism.api.chemical.pigment.Pigment} with the given registry name.
     *
     * @param tokens The resource location to validate.
     *
     * @return {@code true} if valid, {@code false} otherwise.
     */
    @ZenCodeType.Method
    @BracketValidator(CrTConstants.BRACKET_PIGMENT)
    public static boolean validatePigmentStack(String tokens) {
        return validate(CrTConstants.BRACKET_PIGMENT, tokens, MekanismAPI.pigmentRegistry());
    }

    /**
     * Validates if there is a {@link mekanism.api.chemical.slurry.Slurry} with the given registry name.
     *
     * @param tokens The resource location to validate.
     *
     * @return {@code true} if valid, {@code false} otherwise.
     */
    @ZenCodeType.Method
    @BracketValidator(CrTConstants.BRACKET_SLURRY)
    public static boolean validateSlurryStack(String tokens) {
        return validate(CrTConstants.BRACKET_SLURRY, tokens, MekanismAPI.slurryRegistry());
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
        return validate(CrTConstants.BRACKET_ROBIT_SKIN, tokens, MekanismAPI.robitSkinRegistry());
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
        return validate(CrTConstants.BRACKET_MODULE_DATA, tokens, MekanismAPI.moduleRegistry());
    }

    private static boolean validate(String bracket, String tokens, IForgeRegistry<?> registry) {
        ResourceLocation registryName = ResourceLocation.tryParse(tokens);
        if (registryName == null) {
            CraftTweakerAPI.logError("Could not get BEP <%s:%s>. Syntax is <%1$s:modid:%1$s_name>", bracket, tokens);
            return false;
        }
        if (isRegistryUnlocked(registry) || registry.containsKey(registryName)) {
            return true;
        }
        String typeName = bracket.replace("_", " ");
        CraftTweakerAPI.logError("Could not get %s for <%s:%s" + tokens + ">, %1$s does not appear to exist!", typeName, bracket, tokens);
        return false;
    }

    private static boolean isRegistryUnlocked(IForgeRegistry<?> registry) {
        return registry instanceof ForgeRegistry && !((ForgeRegistry<?>) registry).isLocked();
    }
}