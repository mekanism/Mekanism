package mekanism.common.integration.crafttweaker.projecte;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTGas;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTInfuseType;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTPigment;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTSlurry;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import mekanism.common.integration.crafttweaker.tag.CrTGasTagManager;
import mekanism.common.integration.crafttweaker.tag.CrTInfuseTypeTagManager;
import mekanism.common.integration.crafttweaker.tag.CrTPigmentTagManager;
import mekanism.common.integration.crafttweaker.tag.CrTSlurryTagManager;
import mekanism.common.integration.projecte.NSSGas;
import mekanism.common.integration.projecte.NSSInfuseType;
import mekanism.common.integration.projecte.NSSPigment;
import mekanism.common.integration.projecte.NSSSlurry;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Expand ProjectE's NSSResolver CraftTweaker class to add helpers for creating {@link NormalizedSimpleStack} for our various chemicals.
 */
@ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
@ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_NSS_RESOLVER)
public class CrTNSSResolverExpansion {

    /**
     * Create a {@link NormalizedSimpleStack} representing a given {@link ICrTGas}.
     *
     * @param gas Gas to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link ICrTGas}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromGas(ICrTGas gas) {
        return NSSGas.createGas(validateNotEmptyAndGet(gas, "gas"));
    }

    /**
     * Create a {@link NormalizedSimpleStack} representing a given {@link ICrTGasStack}.
     *
     * @param stack Gas Stack to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link ICrTGasStack}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromGas(ICrTGasStack stack) {
        return NSSGas.createGas(validateNotEmptyAndGet(stack, "gas"));
    }

    /**
     * Create a {@link NormalizedSimpleStack} representing a given {@link MCTag<ICrTGas>}.
     *
     * @param tag Gas Tag to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link MCTag<ICrTGas>}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromGasTag(MCTag<ICrTGas> tag) {
        return NSSGas.createTag(CrTUtils.validateTagAndGet(tag, CrTGasTagManager.INSTANCE::getInternal));
    }

    /**
     * Create a {@link NormalizedSimpleStack} representing a given {@link ICrTInfuseType}.
     *
     * @param infuseType Infuse Type to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link ICrTInfuseType}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromInfuseType(ICrTInfuseType infuseType) {
        return NSSInfuseType.createInfuseType(validateNotEmptyAndGet(infuseType, "infuse type"));
    }

    /**
     * Create a {@link NormalizedSimpleStack} representing a given {@link ICrTInfusionStack}.
     *
     * @param stack Infusion Stack to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link ICrTInfusionStack}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromInfuseType(ICrTInfusionStack stack) {
        return NSSInfuseType.createInfuseType(validateNotEmptyAndGet(stack, "infusion"));
    }

    /**
     * Create a {@link NormalizedSimpleStack} representing a given {@link MCTag<ICrTInfuseType>}.
     *
     * @param tag Infuse Type Tag to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link MCTag<ICrTInfuseType>}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromInfuseTypeTag(MCTag<ICrTInfuseType> tag) {
        return NSSInfuseType.createTag(CrTUtils.validateTagAndGet(tag, CrTInfuseTypeTagManager.INSTANCE::getInternal));
    }

    /**
     * Create a {@link NormalizedSimpleStack} representing a given {@link ICrTPigment}.
     *
     * @param pigment Pigment to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link ICrTPigment}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromPigment(ICrTPigment pigment) {
        return NSSPigment.createPigment(validateNotEmptyAndGet(pigment, "pigment"));
    }

    /**
     * Create a {@link NormalizedSimpleStack} representing a given {@link ICrTPigmentStack}.
     *
     * @param stack Pigment Stack to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link ICrTPigmentStack}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromPigment(ICrTPigmentStack stack) {
        return NSSPigment.createPigment(validateNotEmptyAndGet(stack, "pigment"));
    }

    /**
     * Create a {@link NormalizedSimpleStack} representing a given {@link MCTag<ICrTPigment>}.
     *
     * @param tag Pigment Tag to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link MCTag<ICrTPigment>}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromPigmentTag(MCTag<ICrTPigment> tag) {
        return NSSPigment.createTag(CrTUtils.validateTagAndGet(tag, CrTPigmentTagManager.INSTANCE::getInternal));
    }

    /**
     * Create a {@link NormalizedSimpleStack} representing a given {@link ICrTSlurry}.
     *
     * @param slurry Slurry to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link ICrTSlurry}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromSlurry(ICrTSlurry slurry) {
        return NSSSlurry.createSlurry(validateNotEmptyAndGet(slurry, "slurry"));
    }

    /**
     * Create a {@link NormalizedSimpleStack} representing a given {@link ICrTSlurryStack}.
     *
     * @param stack Slurry Stack to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link ICrTSlurryStack}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromSlurry(ICrTSlurryStack stack) {
        return NSSSlurry.createSlurry(validateNotEmptyAndGet(stack, "slurry"));
    }

    /**
     * Create a {@link NormalizedSimpleStack} representing a given {@link MCTag<ICrTSlurry>}.
     *
     * @param tag Slurry Tag to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link MCTag<ICrTSlurry>}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromSlurryTag(MCTag<ICrTSlurry> tag) {
        return NSSSlurry.createTag(CrTUtils.validateTagAndGet(tag, CrTSlurryTagManager.INSTANCE::getInternal));
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, CRT_CHEMICAL extends ICrTChemical<CHEMICAL, ?, ?, ?>> CHEMICAL validateNotEmptyAndGet(CRT_CHEMICAL chemical,
          String type) {
        if (chemical.isEmptyType()) {
            //Note: We check this here to provide a better error than we would get in the NSS create method
            throw new IllegalArgumentException("Cannot make an NSS Representation using an empty " + type + ".");
        }
        return chemical.getChemical();
    }

    private static <STACK extends ChemicalStack<?>, CRT_STACK extends ICrTChemicalStack<?, STACK, ?, ?>> STACK validateNotEmptyAndGet(CRT_STACK stack,
          String type) {
        if (stack.isEmpty()) {
            //Note: We check this here to provide a better error than we would get in the NSS create method
            throw new IllegalArgumentException("Cannot make an NSS Representation using an empty " + type + " stack.");
        }
        return stack.getInternal();
    }
}