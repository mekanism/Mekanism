package mekanism.common.integration.crafttweaker.jeitweaker;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.TypedExpansion;
import com.blamejared.jeitweaker.zen.component.JeiDrawable;
import com.blamejared.jeitweaker.zen.component.JeiIngredient;
import com.blamejared.jeitweaker.zen.component.JeiIngredientExpansions;
import com.blamejared.jeitweaker.zen.component.RawJeiIngredient;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import org.openzen.zencode.java.ZenCodeType;

public class JEITweakerExpansions {

    private JEITweakerExpansions() {
    }

    @ZenRegister(modDeps = MekanismHooks.JEITWEAKER_MOD_ID)
    @TypedExpansion(ICrTGasStack.class)
    public static class ICrTGasStackExpansion {

        private ICrTGasStackExpansion() {
        }

        /**
         * Converts an {@link ICrTGasStack} into its {@link RawJeiIngredient} equivalent.
         */
        @ZenCodeType.Caster(implicit = true)
        public static RawJeiIngredient asJeiIngredient(final ICrTGasStack _this) {
            return JeiIngredient.of(MekanismJeiTweakerPluginProvider.GAS.get(), _this);
        }

        /**
         * Converts an {@link ICrTGasStack} to a {@link JeiDrawable} that draws it as a JEI ingredient.
         */
        @ZenCodeType.Caster(implicit = true)
        public static JeiDrawable asJeiDrawable(final ICrTGasStack _this) {
            return JeiIngredientExpansions.asJeiDrawable(asJeiIngredient(_this));
        }
    }

    @ZenRegister(modDeps = MekanismHooks.JEITWEAKER_MOD_ID)
    @TypedExpansion(ICrTInfusionStack.class)
    public static class ICrTInfusionStackExpansion {

        private ICrTInfusionStackExpansion() {
        }

        /**
         * Converts an {@link ICrTInfusionStack} into its {@link RawJeiIngredient} equivalent.
         */
        @ZenCodeType.Caster(implicit = true)
        public static RawJeiIngredient asJeiIngredient(final ICrTInfusionStack _this) {
            return JeiIngredient.of(MekanismJeiTweakerPluginProvider.INFUSION.get(), _this);
        }

        /**
         * Converts an {@link ICrTInfusionStack} to a {@link JeiDrawable} that draws it as a JEI ingredient.
         */
        @ZenCodeType.Caster(implicit = true)
        public static JeiDrawable asJeiDrawable(final ICrTInfusionStack _this) {
            return JeiIngredientExpansions.asJeiDrawable(asJeiIngredient(_this));
        }
    }

    @ZenRegister(modDeps = MekanismHooks.JEITWEAKER_MOD_ID)
    @TypedExpansion(ICrTPigmentStack.class)
    public static class ICrTPigmentStackExpansion {

        private ICrTPigmentStackExpansion() {
        }

        /**
         * Converts an {@link ICrTPigmentStack} into its {@link RawJeiIngredient} equivalent.
         */
        @ZenCodeType.Caster(implicit = true)
        public static RawJeiIngredient asJeiIngredient(final ICrTPigmentStack _this) {
            return JeiIngredient.of(MekanismJeiTweakerPluginProvider.PIGMENT.get(), _this);
        }

        /**
         * Converts an {@link ICrTPigmentStack} to a {@link JeiDrawable} that draws it as a JEI ingredient.
         */
        @ZenCodeType.Caster(implicit = true)
        public static JeiDrawable asJeiDrawable(final ICrTPigmentStack _this) {
            return JeiIngredientExpansions.asJeiDrawable(asJeiIngredient(_this));
        }
    }

    @ZenRegister(modDeps = MekanismHooks.JEITWEAKER_MOD_ID)
    @TypedExpansion(ICrTSlurryStack.class)
    public static class ICrTSlurryStackExpansion {

        private ICrTSlurryStackExpansion() {
        }

        /**
         * Converts an {@link ICrTSlurryStack} into its {@link RawJeiIngredient} equivalent.
         */
        @ZenCodeType.Caster(implicit = true)
        public static RawJeiIngredient asJeiIngredient(final ICrTSlurryStack _this) {
            return JeiIngredient.of(MekanismJeiTweakerPluginProvider.SLURRY.get(), _this);
        }

        /**
         * Converts an {@link ICrTSlurryStack} to a {@link JeiDrawable} that draws it as a JEI ingredient.
         */
        @ZenCodeType.Caster(implicit = true)
        public static JeiDrawable asJeiDrawable(final ICrTSlurryStack _this) {
            return JeiIngredientExpansions.asJeiDrawable(asJeiIngredient(_this));
        }
    }
}