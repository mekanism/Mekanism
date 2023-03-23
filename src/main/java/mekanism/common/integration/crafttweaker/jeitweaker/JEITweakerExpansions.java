package mekanism.common.integration.crafttweaker.jeitweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.TypedExpansion;
import com.blamejared.jeitweaker.common.api.ingredient.JeiIngredient;
import com.blamejared.jeitweaker.common.api.ingredient.JeiIngredientType;
import com.blamejared.jeitweaker.common.api.ingredient.JeiIngredientTypes;
import com.blamejared.jeitweaker.common.api.ingredient.JeiIngredients;
import com.blamejared.jeitweaker.common.api.zen.ingredient.ZenJeiIngredient;
import com.google.common.base.Suppliers;
import java.util.function.Supplier;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.Mekanism;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import org.openzen.zencode.java.ZenCodeType;

public class JEITweakerExpansions {

    private JEITweakerExpansions() {
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>>
    Supplier<JeiIngredientType<STACK, CRT_STACK>> find(String path) {
        return Suppliers.memoize(() -> JeiIngredientTypes.findById(Mekanism.rl(path)));
    }

    @ZenRegister(modDeps = MekanismHooks.JEITWEAKER_MOD_ID)
    @TypedExpansion(ICrTGasStack.class)
    public static class ICrTGasStackExpansion {

        private static final Supplier<JeiIngredientType<GasStack, ICrTGasStack>> TYPE = find("gas");

        private ICrTGasStackExpansion() {
        }

        /**
         * Converts an {@link ICrTGasStack} into its {@link ZenJeiIngredient} equivalent.
         */
        @ZenCodeType.Caster(implicit = true)
        public static ZenJeiIngredient asJeiIngredient(final ICrTGasStack _this) {
            return JeiIngredients.toZenIngredient(JeiIngredient.ofZen(TYPE.get(), _this));
        }
    }

    @ZenRegister(modDeps = MekanismHooks.JEITWEAKER_MOD_ID)
    @TypedExpansion(ICrTInfusionStack.class)
    public static class ICrTInfusionStackExpansion {

        private static final Supplier<JeiIngredientType<InfusionStack, ICrTInfusionStack>> TYPE = find("infusion");

        private ICrTInfusionStackExpansion() {
        }

        /**
         * Converts an {@link ICrTInfusionStack} into its {@link ZenJeiIngredient} equivalent.
         */
        @ZenCodeType.Caster(implicit = true)
        public static ZenJeiIngredient asJeiIngredient(final ICrTInfusionStack _this) {
            return JeiIngredients.toZenIngredient(JeiIngredient.ofZen(TYPE.get(), _this));
        }
    }

    @ZenRegister(modDeps = MekanismHooks.JEITWEAKER_MOD_ID)
    @TypedExpansion(ICrTPigmentStack.class)
    public static class ICrTPigmentStackExpansion {

        private static final Supplier<JeiIngredientType<PigmentStack, ICrTPigmentStack>> TYPE = find("pigment");

        private ICrTPigmentStackExpansion() {
        }

        /**
         * Converts an {@link ICrTPigmentStack} into its {@link ZenJeiIngredient} equivalent.
         */
        @ZenCodeType.Caster(implicit = true)
        public static ZenJeiIngredient asJeiIngredient(final ICrTPigmentStack _this) {
            return JeiIngredients.toZenIngredient(JeiIngredient.ofZen(TYPE.get(), _this));
        }
    }

    @ZenRegister(modDeps = MekanismHooks.JEITWEAKER_MOD_ID)
    @TypedExpansion(ICrTSlurryStack.class)
    public static class ICrTSlurryStackExpansion {

        private static final Supplier<JeiIngredientType<SlurryStack, ICrTSlurryStack>> TYPE = find("slurry");

        private ICrTSlurryStackExpansion() {
        }

        /**
         * Converts an {@link ICrTSlurryStack} into its {@link ZenJeiIngredient} equivalent.
         */
        @ZenCodeType.Caster(implicit = true)
        public static ZenJeiIngredient asJeiIngredient(final ICrTSlurryStack _this) {
            return JeiIngredients.toZenIngredient(JeiIngredient.ofZen(TYPE.get(), _this));
        }
    }
}