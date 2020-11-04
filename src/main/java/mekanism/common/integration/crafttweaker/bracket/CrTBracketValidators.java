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

    @ZenCodeType.Method
    @BracketValidator(CrTConstants.BRACKET_GAS)
    public static boolean validateGasStack(String tokens) {
        return validateChemicalStack(CrTConstants.BRACKET_GAS, tokens, MekanismAPI.gasRegistry());
    }

    @ZenCodeType.Method
    @BracketValidator(CrTConstants.BRACKET_INFUSE_TYPE)
    public static boolean validateInfusionStack(String tokens) {
        return validateChemicalStack(CrTConstants.BRACKET_INFUSE_TYPE, tokens, MekanismAPI.infuseTypeRegistry());
    }

    @ZenCodeType.Method
    @BracketValidator(CrTConstants.BRACKET_PIGMENT)
    public static boolean validatePigmentStack(String tokens) {
        return validateChemicalStack(CrTConstants.BRACKET_PIGMENT, tokens, MekanismAPI.pigmentRegistry());
    }

    @ZenCodeType.Method
    @BracketValidator(CrTConstants.BRACKET_SLURRY)
    public static boolean validateSlurryStack(String tokens) {
        return validateChemicalStack(CrTConstants.BRACKET_SLURRY, tokens, MekanismAPI.slurryRegistry());
    }

    private static boolean validateChemicalStack(String bracket, String tokens, IForgeRegistry<?> registry) {
        ResourceLocation registryName = ResourceLocation.tryCreate(tokens);
        if (registryName == null) {
            //TODO: test to make sure this works
            CraftTweakerAPI.logError("Could not get BEP <%s:%s>. Syntax is <%1$s:modid:%1$s_name>", bracket, tokens);
            return false;
        }
        if (isRegistryUnlocked(registry) || registry.containsKey(registryName)) {
            return true;
        }
        //TODO: Test this
        String typeName = bracket.replace("_", " ");
        CraftTweakerAPI.logError("Could not get %s for <%s:%s" + tokens + ">, %1$s does not appear to exist!", typeName, bracket, tokens);
        return false;
    }

    private static boolean isRegistryUnlocked(IForgeRegistry<?> registry) {
        return registry instanceof ForgeRegistry && !((ForgeRegistry<?>) registry).isLocked();
    }
}