package mekanism.common.integration.crafttweaker.projecte;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import mekanism.api.chemical.Chemical;
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
     * Create a {@link NormalizedSimpleStack} representing a given {@link ICrTChemicalStack}.
     *
     * @param stack Chemical Stack to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link ICrTChemicalStack}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromChemical(ICrTChemicalStack stack) {
        if (stack.isEmpty()) {
            //Note: We check this here to provide a better error than we would get in the NSS create method
            throw new IllegalArgumentException("Cannot make an NSS Representation using an empty chemical stack.");
        }
        return NSSChemical.createChemical(stack.getInternal());
    }

    /**
     * Create a {@link NormalizedSimpleStack} representing a given {@link KnownTag}&lt;{@link Chemical}&gt;.
     *
     * @param tag Chemical Tag to represent
     *
     * @return A {@link NormalizedSimpleStack} representing a given {@link KnownTag}&lt;{@link Chemical}&gt;.
     */
    @ZenCodeType.StaticExpansionMethod
    public static NormalizedSimpleStack fromChemicalTag(KnownTag<Chemical> tag) {
        return NSSChemical.createTag(CrTUtils.validateTagAndGet(tag));
    }
}