package mekanism.common.integration.crafttweaker.projecte;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
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

    private CrTNSSResolverExpansion() {
    }

    /**
     * Create a {@link NormalizedSimpleStack} representing a given {@link Gas}.
     *
     * @param gas Gas to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link Gas}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromGas(Gas gas) {
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
     * Create a {@link NormalizedSimpleStack} representing a given {@link KnownTag<Gas>}.
     *
     * @param tag Gas Tag to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link KnownTag<Gas>}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromGasTag(KnownTag<Gas> tag) {
        return NSSGas.createTag(CrTUtils.validateTagAndGet(tag));
    }

    /**
     * Create a {@link NormalizedSimpleStack} representing a given {@link InfuseType}.
     *
     * @param infuseType Infuse Type to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link InfuseType}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromInfuseType(InfuseType infuseType) {
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
     * Create a {@link NormalizedSimpleStack} representing a given {@link KnownTag<InfuseType>}.
     *
     * @param tag Infuse Type Tag to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link KnownTag<InfuseType>}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromInfuseTypeTag(KnownTag<InfuseType> tag) {
        return NSSInfuseType.createTag(CrTUtils.validateTagAndGet(tag));
    }

    /**
     * Create a {@link NormalizedSimpleStack} representing a given {@link Pigment}.
     *
     * @param pigment Pigment to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link Pigment}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromPigment(Pigment pigment) {
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
     * Create a {@link NormalizedSimpleStack} representing a given {@link KnownTag<Pigment>}.
     *
     * @param tag Pigment Tag to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link KnownTag<Pigment>}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromPigmentTag(KnownTag<Pigment> tag) {
        return NSSPigment.createTag(CrTUtils.validateTagAndGet(tag));
    }

    /**
     * Create a {@link NormalizedSimpleStack} representing a given {@link Slurry}.
     *
     * @param slurry Slurry to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link Slurry}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromSlurry(Slurry slurry) {
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
     * Create a {@link NormalizedSimpleStack} representing a given {@link KnownTag<Slurry>}.
     *
     * @param tag Slurry Tag to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link KnownTag<Slurry>}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromSlurryTag(KnownTag<Slurry> tag) {
        return NSSSlurry.createTag(CrTUtils.validateTagAndGet(tag));
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>> CHEMICAL validateNotEmptyAndGet(CHEMICAL chemical, String type) {
        if (chemical.isEmptyType()) {
            //Note: We check this here to provide a better error than we would get in the NSS create method
            throw new IllegalArgumentException("Cannot make an NSS Representation using an empty " + type + ".");
        }
        return chemical.getChemical();
    }

    private static <STACK extends ChemicalStack<?>, CRT_STACK extends ICrTChemicalStack<?, STACK, ?>> STACK validateNotEmptyAndGet(CRT_STACK stack, String type) {
        if (stack.isEmpty()) {
            //Note: We check this here to provide a better error than we would get in the NSS create method
            throw new IllegalArgumentException("Cannot make an NSS Representation using an empty " + type + " stack.");
        }
        return stack.getInternal();
    }
}