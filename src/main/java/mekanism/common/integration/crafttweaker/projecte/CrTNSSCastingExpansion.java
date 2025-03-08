package mekanism.common.integration.crafttweaker.projecte;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker_annotations.annotations.TypedExpansion;
import mekanism.api.chemical.Chemical;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Helper class to allow implicit casting various chemical types into the custom ProjectE {@link NormalizedSimpleStack} types we create.
 */
public class CrTNSSCastingExpansion {

    private CrTNSSCastingExpansion() {
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @TypedExpansion(ICrTChemicalStack.class)
    public static class ICrTChemicalStackExpansion {

        private ICrTChemicalStackExpansion() {
        }

        /**
         * Allows for casting {@link ICrTChemicalStack}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(ICrTChemicalStack _this) {
            return CrTNSSResolverExpansion.fromChemical(_this);
        }
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_CHEMICAL_TAG)
    public static class ChemicalTagExpansion {

        private ChemicalTagExpansion() {
        }

        /**
         * Allows for casting {@link KnownTag<Chemical>}s to {@link NormalizedSimpleStack} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(KnownTag<Chemical> _this) {
            return CrTNSSResolverExpansion.fromChemicalTag(_this);
        }
    }
}