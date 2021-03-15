package mekanism.common.integration.crafttweaker.projecte;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTGas;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTInfuseType;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTPigment;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTSlurry;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Helper class to allow implicit casting various chemical types into the custom ProjectE {@link NormalizedSimpleStack} types we create.
 */
public class CrTNSSCastingExpansion {

    private CrTNSSCastingExpansion() {
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @ZenCodeType.Expansion(CrTConstants.CLASS_GAS)
    public static class ICrTGasExpansion {

        /**
         * Allows for casting {@link ICrTGas}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(ICrTGas _this) {
            return CrTNSSResolverExpansion.fromGas(_this);
        }
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @ZenCodeType.Expansion(CrTConstants.CLASS_GAS_STACK)
    public static class ICrTGasStackExpansion {

        /**
         * Allows for casting {@link ICrTGasStack}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(ICrTGasStack _this) {
            return CrTNSSResolverExpansion.fromGas(_this);
        }
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_GAS_TAG)
    public static class GasTagExpansion {

        /**
         * Allows for casting {@link MCTag<ICrTGas>}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(MCTag<ICrTGas> _this) {
            return CrTNSSResolverExpansion.fromGasTag(_this);
        }
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @ZenCodeType.Expansion(CrTConstants.CLASS_INFUSE_TYPE)
    public static class ICrTInfuseTypeExpansion {

        /**
         * Allows for casting {@link ICrTInfuseType}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(ICrTInfuseType _this) {
            return CrTNSSResolverExpansion.fromInfuseType(_this);
        }
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @ZenCodeType.Expansion(CrTConstants.CLASS_INFUSION_STACK)
    public static class ICrTInfusionStackExpansion {

        /**
         * Allows for casting {@link ICrTInfusionStack}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(ICrTInfusionStack _this) {
            return CrTNSSResolverExpansion.fromInfuseType(_this);
        }
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_INFUSE_TYPE_TAG)
    public static class InfuseTypeTagExpansion {

        /**
         * Allows for casting {@link MCTag<ICrTInfuseType>}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(MCTag<ICrTInfuseType> _this) {
            return CrTNSSResolverExpansion.fromInfuseTypeTag(_this);
        }
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @ZenCodeType.Expansion(CrTConstants.CLASS_PIGMENT)
    public static class ICrTPigmentExpansion {

        /**
         * Allows for casting {@link ICrTPigment}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(ICrTPigment _this) {
            return CrTNSSResolverExpansion.fromPigment(_this);
        }
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @ZenCodeType.Expansion(CrTConstants.CLASS_PIGMENT_STACK)
    public static class ICrTPigmentStackExpansion {

        /**
         * Allows for casting {@link ICrTPigmentStack}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(ICrTPigmentStack _this) {
            return CrTNSSResolverExpansion.fromPigment(_this);
        }
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_PIGMENT_TAG)
    public static class PigmentTagExpansion {

        /**
         * Allows for casting {@link MCTag<ICrTPigment>}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(MCTag<ICrTPigment> _this) {
            return CrTNSSResolverExpansion.fromPigmentTag(_this);
        }
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @ZenCodeType.Expansion(CrTConstants.CLASS_SLURRY)
    public static class ICrTSlurryExpansion {

        /**
         * Allows for casting {@link ICrTSlurry}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(ICrTSlurry _this) {
            return CrTNSSResolverExpansion.fromSlurry(_this);
        }
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @ZenCodeType.Expansion(CrTConstants.CLASS_SLURRY_STACK)
    public static class ICrTSlurryStackExpansion {

        /**
         * Allows for casting {@link ICrTSlurryStack}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(ICrTSlurryStack _this) {
            return CrTNSSResolverExpansion.fromSlurry(_this);
        }
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_SLURRY_TAG)
    public static class SlurryTagExpansion {

        /**
         * Allows for casting {@link MCTag<ICrTSlurry>}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(MCTag<ICrTSlurry> _this) {
            return CrTNSSResolverExpansion.fromSlurryTag(_this);
        }
    }
}