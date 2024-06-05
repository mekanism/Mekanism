package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.Many;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.recipes.ingredients.GasStackIngredient;
import mekanism.api.recipes.ingredients.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.SlurryStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTGasStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTInfusionStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTPigmentStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTSlurryStackIngredient;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Helper class to keep track of multiple tiny expansions to various CraftTweaker classes to allow for implicit casting to our wrapper ingredients.
 */
public class CrTIngredientExpansion {

    private CrTIngredientExpansion() {
    }

    @ZenRegister
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_GAS_AMOUNT_TAG)
    public static class GasTagWithAmountExpansion {

        private GasTagWithAmountExpansion() {
        }

        /**
         * Allows for casting {@link Many<KnownTag<Gas>>}s to {@link GasStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static GasStackIngredient asGasStackIngredient(Many<KnownTag<Gas>> _this) {
            return CrTGasStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_INFUSE_TYPE_AMOUNT_TAG)
    public static class InfuseTypeTagWithAmountExpansion {

        private InfuseTypeTagWithAmountExpansion() {
        }

        /**
         * Allows for casting {@link Many<KnownTag<InfuseType>>}s to {@link InfusionStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static InfusionStackIngredient asGasStackIngredient(Many<KnownTag<InfuseType>> _this) {
            return CrTInfusionStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_PIGMENT_AMOUNT_TAG)
    public static class PigmentTagWithAmountExpansion {

        private PigmentTagWithAmountExpansion() {
        }

        /**
         * Allows for casting {@link Many<KnownTag<Pigment>>}s to {@link PigmentStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static PigmentStackIngredient asGasStackIngredient(Many<KnownTag<Pigment>> _this) {
            return CrTPigmentStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_SLURRY_AMOUNT_TAG)
    public static class SlurryTagWithAmountExpansion {

        private SlurryTagWithAmountExpansion() {
        }

        /**
         * Allows for casting {@link Many<KnownTag<Slurry>>}s to {@link SlurryStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static SlurryStackIngredient asGasStackIngredient(Many<KnownTag<Slurry>> _this) {
            return CrTSlurryStackIngredient.from(_this);
        }
    }
}