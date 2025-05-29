package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.Many;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTChemicalStackIngredient;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Helper class to keep track of multiple tiny expansions to various CraftTweaker classes to allow for implicit casting to our wrapper ingredients.
 */
public class CrTIngredientExpansion {

    private CrTIngredientExpansion() {
    }

    @ZenRegister
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_CHEMICAL_AMOUNT_TAG)
    public static class ChemicalTagWithAmountExpansion {

        private ChemicalTagWithAmountExpansion() {
        }

        /**
         * Allows for casting {@link Many}&lt;{@link KnownTag}&lt;{@link Chemical}&gt;&gt;s to {@link ChemicalStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static ChemicalStackIngredient asChemicalStackIngredient(Many<KnownTag<Chemical>> _this) {
            return CrTChemicalStackIngredient.from(_this);
        }
    }

}