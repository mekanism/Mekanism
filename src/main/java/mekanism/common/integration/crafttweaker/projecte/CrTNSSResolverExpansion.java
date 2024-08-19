package mekanism.common.integration.crafttweaker.projecte;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.projecte.NSSChemical;
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
     * Create a {@link NormalizedSimpleStack} representing a given {@link Chemical}.
     *
     * @param chemical Chemical to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link Chemical}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromChemical(Chemical chemical) {
        return NSSChemical.createChemical(validateNotEmptyAndGet(chemical, "chemical"));
    }

    /**
     * Create a {@link NormalizedSimpleStack} representing a given {@link ICrTChemicalStack}.
     *
     * @param stack Chemical Stack to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link ICrTChemicalStack}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromChemical(ICrTChemicalStack stack) {
        return NSSChemical.createChemical(validateNotEmptyAndGet(stack, "chemical"));
    }

    /**
     * Create a {@link NormalizedSimpleStack} representing a given {@link KnownTag< Chemical >}.
     *
     * @param tag Chemical Tag to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link KnownTag< Chemical >}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromChemicalTag(KnownTag<Chemical> tag) {
        return NSSChemical.createTag(CrTUtils.validateTagAndGet(tag));
    }


    private static Chemical validateNotEmptyAndGet(Chemical chemical, String type) {
        if (chemical.isEmptyType()) {
            //Note: We check this here to provide a better error than we would get in the NSS create method
            throw new IllegalArgumentException("Cannot make an NSS Representation using an empty " + type + ".");
        }
        return chemical.getChemical();
    }

    private static ChemicalStack validateNotEmptyAndGet(ICrTChemicalStack stack, String type) {
        if (stack.isEmpty()) {
            //Note: We check this here to provide a better error than we would get in the NSS create method
            throw new IllegalArgumentException("Cannot make an NSS Representation using an empty " + type + " stack.");
        }
        return stack.getInternal();
    }
}