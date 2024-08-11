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
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.Mekanism;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import org.openzen.zencode.java.ZenCodeType;

public class JEITweakerExpansions {

    private JEITweakerExpansions() {
    }

    @ZenRegister(modDeps = MekanismHooks.JEITWEAKER_MOD_ID)
    @TypedExpansion(ICrTChemicalStack.class)
    public static class ICrTChemicalStackExpansion {

        private static final Supplier<JeiIngredientType<ChemicalStack, ICrTChemicalStack>> TYPE = Suppliers.memoize(() -> JeiIngredientTypes.findById(Mekanism.rl("chemical")));

        private ICrTChemicalStackExpansion() {
        }

        /**
         * Converts an {@link ICrTChemicalStack} into its {@link ZenJeiIngredient} equivalent.
         */
        @ZenCodeType.Caster(implicit = true)
        public static ZenJeiIngredient asJeiIngredient(final ICrTChemicalStack _this) {
            return JeiIngredients.toZenIngredient(JeiIngredient.ofZen(TYPE.get(), _this));
        }
    }
}