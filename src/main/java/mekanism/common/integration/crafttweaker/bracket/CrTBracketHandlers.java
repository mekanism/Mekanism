package mekanism.common.integration.crafttweaker.bracket;

import com.blamejared.crafttweaker.api.annotation.BracketResolver;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import java.util.function.Function;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.gear.ModuleData;
import mekanism.api.robit.RobitSkin;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_BRACKET_HANDLER)
public class CrTBracketHandlers {

    /**
     * Gets the {@link ICrTGasStack} based on registry name. Throws an error if it can't find the {@link mekanism.api.chemical.gas.Gas}.
     *
     * @param tokens The {@link mekanism.api.chemical.gas.Gas}'s resource location.
     *
     * @return A stack of the {@link mekanism.api.chemical.gas.Gas} with an amount of one mB.
     */
    @ZenCodeType.Method
    @BracketResolver(CrTConstants.BRACKET_GAS)
    public static ICrTGasStack getGasStack(String tokens) {
        return getChemicalStack(CrTConstants.BRACKET_GAS, tokens, MekanismAPI.gasRegistry(), CrTUtils::stackFromGas);
    }

    /**
     * Gets the {@link ICrTInfusionStack} based on registry name. Throws an error if it can't find the {@link mekanism.api.chemical.infuse.InfuseType}.
     *
     * @param tokens The {@link mekanism.api.chemical.infuse.InfuseType}'s resource location.
     *
     * @return A stack of the {@link mekanism.api.chemical.infuse.InfuseType} with an amount of one mB.
     */
    @ZenCodeType.Method
    @BracketResolver(CrTConstants.BRACKET_INFUSE_TYPE)
    public static ICrTInfusionStack getInfusionStack(String tokens) {
        return getChemicalStack(CrTConstants.BRACKET_INFUSE_TYPE, tokens, MekanismAPI.infuseTypeRegistry(), CrTUtils::stackFromInfuseType);
    }

    /**
     * Gets the {@link ICrTPigmentStack} based on registry name. Throws an error if it can't find the {@link mekanism.api.chemical.pigment.Pigment}.
     *
     * @param tokens The {@link mekanism.api.chemical.pigment.Pigment}'s resource location.
     *
     * @return A stack of the {@link mekanism.api.chemical.pigment.Pigment} with an amount of one mB.
     */
    @ZenCodeType.Method
    @BracketResolver(CrTConstants.BRACKET_PIGMENT)
    public static ICrTPigmentStack getPigmentStack(String tokens) {
        return getChemicalStack(CrTConstants.BRACKET_PIGMENT, tokens, MekanismAPI.pigmentRegistry(), CrTUtils::stackFromPigment);
    }

    /**
     * Gets the {@link ICrTSlurryStack} based on registry name. Throws an error if it can't find the {@link mekanism.api.chemical.slurry.Slurry}.
     *
     * @param tokens The {@link mekanism.api.chemical.slurry.Slurry}'s resource location.
     *
     * @return A stack of the {@link mekanism.api.chemical.slurry.Slurry} with an amount of one mB.
     */
    @ZenCodeType.Method
    @BracketResolver(CrTConstants.BRACKET_SLURRY)
    public static ICrTSlurryStack getSlurryStack(String tokens) {
        return getChemicalStack(CrTConstants.BRACKET_SLURRY, tokens, MekanismAPI.slurryRegistry(), CrTUtils::stackFromSlurry);
    }

    /**
     * Gets the {@link RobitSkin} based on registry name. Throws an error if it can't find the {@link RobitSkin}.
     *
     * @param tokens The {@link RobitSkin}'s resource location.
     *
     * @return A reference to the {@link RobitSkin}.
     */
    @ZenCodeType.Method
    @BracketResolver(CrTConstants.BRACKET_ROBIT_SKIN)
    public static RobitSkin getRobitSkin(String tokens) {
        return getValue(CrTConstants.BRACKET_ROBIT_SKIN, tokens, MekanismAPI.robitSkinRegistry());
    }

    /**
     * Gets the {@link ModuleData} based on registry name. Throws an error if it can't find the {@link ModuleData}.
     *
     * @param tokens The {@link ModuleData}'s resource location.
     *
     * @return A reference to the {@link ModuleData}.
     */
    @ZenCodeType.Method
    @BracketResolver(CrTConstants.BRACKET_MODULE_DATA)
    public static ModuleData<?> getModuleData(String tokens) {
        return getValue(CrTConstants.BRACKET_MODULE_DATA, tokens, MekanismAPI.moduleRegistry());
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, ?, CRT_STACK>> CRT_STACK getChemicalStack(String bracket,
          String tokens, IForgeRegistry<CHEMICAL> registry, Function<CHEMICAL, CRT_STACK> getter) {
        return getter.apply(getValue(bracket, tokens, registry));
    }

    private static <V> V getValue(String bracket, String tokens, IForgeRegistry<V> registry) {
        ResourceLocation registryName = ResourceLocation.tryParse(tokens);
        if (registryName == null) {
            String typeName = bracket.replace("_", " ");
            throw new IllegalArgumentException("Could not get " + typeName + " for <" + bracket + ":" + tokens + ">. Syntax is <" + bracket + ":modid:" + bracket + "_name>");
        } else if (!registry.containsKey(registryName)) {
            String typeName = bracket.replace("_", " ");
            throw new IllegalArgumentException("Could not get " + typeName + " for <" + bracket + ":" + tokens + ">, " + typeName + " does not appear to exist!");
        }
        return registry.getValue(registryName);
    }
}