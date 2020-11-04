package mekanism.common.integration.crafttweaker.bracket;

import com.blamejared.crafttweaker.api.annotations.BracketResolver;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import java.util.function.Function;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_BRACKET_HANDLER)
public class CrTBracketHandlers {

    @ZenCodeType.Method
    @BracketResolver(CrTConstants.BRACKET_GAS)
    public static ICrTGasStack getGasStack(String tokens) {
        return getChemicalStack(CrTConstants.BRACKET_GAS, tokens, MekanismAPI.gasRegistry(), CrTUtils::stackFromGas);
    }

    @ZenCodeType.Method
    @BracketResolver(CrTConstants.BRACKET_INFUSE_TYPE)
    public static ICrTInfusionStack getInfusionStack(String tokens) {
        return getChemicalStack(CrTConstants.BRACKET_INFUSE_TYPE, tokens, MekanismAPI.infuseTypeRegistry(), CrTUtils::stackFromInfuseType);
    }

    @ZenCodeType.Method
    @BracketResolver(CrTConstants.BRACKET_PIGMENT)
    public static ICrTPigmentStack getPigmentStack(String tokens) {
        return getChemicalStack(CrTConstants.BRACKET_PIGMENT, tokens, MekanismAPI.pigmentRegistry(), CrTUtils::stackFromPigment);
    }

    @ZenCodeType.Method
    @BracketResolver(CrTConstants.BRACKET_SLURRY)
    public static ICrTSlurryStack getSlurryStack(String tokens) {
        return getChemicalStack(CrTConstants.BRACKET_SLURRY, tokens, MekanismAPI.slurryRegistry(), CrTUtils::stackFromSlurry);
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, ?, ?, CRT_STACK>> CRT_STACK getChemicalStack(String bracket,
          String tokens, IForgeRegistry<CHEMICAL> registry, Function<CHEMICAL, CRT_STACK> getter) {
        ResourceLocation registryName = ResourceLocation.tryCreate(tokens);
        if (registryName == null) {
            throw new IllegalArgumentException("Could not get " + bracket + " for <" + bracket + ":" + tokens + ">. Syntax is <" + bracket + ":modid:" + bracket + "_name>");
        }
        if (!registry.containsKey(registryName)) {
            String typeName = bracket.replace("_", " ");
            throw new IllegalArgumentException("Could not get " + typeName + " for <" + bracket + ":" + tokens + ">, " + typeName + " does not appear to exist!");
        }
        return getter.apply(registry.getValue(registryName));
    }
}