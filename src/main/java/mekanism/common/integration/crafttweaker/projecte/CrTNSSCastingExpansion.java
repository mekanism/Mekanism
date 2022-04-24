package mekanism.common.integration.crafttweaker.projecte;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker_annotations.annotations.TypedExpansion;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.crafttweaker.CrTConstants;
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
    @TypedExpansion(Gas.class)
    public static class ICrTGasExpansion {

        private ICrTGasExpansion() {
        }

        /**
         * Allows for casting {@link Gas}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(Gas _this) {
            return CrTNSSResolverExpansion.fromGas(_this);
        }
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @TypedExpansion(ICrTGasStack.class)
    public static class ICrTGasStackExpansion {

        private ICrTGasStackExpansion() {
        }

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

        private GasTagExpansion() {
        }

        /**
         * Allows for casting {@link KnownTag<Gas>}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(KnownTag<Gas> _this) {
            return CrTNSSResolverExpansion.fromGasTag(_this);
        }
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @TypedExpansion(InfuseType.class)
    public static class ICrTInfuseTypeExpansion {

        private ICrTInfuseTypeExpansion() {
        }

        /**
         * Allows for casting {@link InfuseType}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(InfuseType _this) {
            return CrTNSSResolverExpansion.fromInfuseType(_this);
        }
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @TypedExpansion(ICrTInfusionStack.class)
    public static class ICrTInfusionStackExpansion {

        private ICrTInfusionStackExpansion() {
        }

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

        private InfuseTypeTagExpansion() {
        }

        /**
         * Allows for casting {@link KnownTag<InfuseType>}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(KnownTag<InfuseType> _this) {
            return CrTNSSResolverExpansion.fromInfuseTypeTag(_this);
        }
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @TypedExpansion(Pigment.class)
    public static class ICrTPigmentExpansion {

        private ICrTPigmentExpansion() {
        }

        /**
         * Allows for casting {@link Pigment}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(Pigment _this) {
            return CrTNSSResolverExpansion.fromPigment(_this);
        }
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @TypedExpansion(ICrTPigmentStack.class)
    public static class ICrTPigmentStackExpansion {

        private ICrTPigmentStackExpansion() {
        }

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

        private PigmentTagExpansion() {
        }

        /**
         * Allows for casting {@link KnownTag<Pigment>}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(KnownTag<Pigment> _this) {
            return CrTNSSResolverExpansion.fromPigmentTag(_this);
        }
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @TypedExpansion(Slurry.class)
    public static class ICrTSlurryExpansion {

        private ICrTSlurryExpansion() {
        }

        /**
         * Allows for casting {@link Slurry}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(Slurry _this) {
            return CrTNSSResolverExpansion.fromSlurry(_this);
        }
    }

    @ZenRegister(modDeps = MekanismHooks.PROJECTE_MOD_ID)
    @TypedExpansion(ICrTSlurryStack.class)
    public static class ICrTSlurryStackExpansion {

        private ICrTSlurryStackExpansion() {
        }

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

        private SlurryTagExpansion() {
        }

        /**
         * Allows for casting {@link KnownTag<Slurry>}s to {@link NormalizedSimpleStack} without needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static NormalizedSimpleStack asNormalizedSimpleStack(KnownTag<Slurry> _this) {
            return CrTNSSResolverExpansion.fromSlurryTag(_this);
        }
    }
}