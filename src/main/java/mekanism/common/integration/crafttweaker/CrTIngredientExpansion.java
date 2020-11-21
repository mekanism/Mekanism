package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCIngredientList;
import com.blamejared.crafttweaker.impl.item.MCItemDefinition;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.blamejared.crafttweaker.impl.tag.expansions.ExpandItemTag;
import mekanism.common.integration.crafttweaker.ingredient.CrTFluidStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTItemStackIngredient;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Helper class to keep track of multiple tiny expansions to various CraftTweaker classes to allow for implicit casting to our wrapper ingredients.
 */
public class CrTIngredientExpansion {

    private CrTIngredientExpansion() {
    }

    @ZenRegister
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_INGREDIENT)
    public static class IIngredientExpansion {

        /**
         * Allows for casting {@link IIngredient}s to {@link CrTItemStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static CrTItemStackIngredient asItemStackIngredient(IIngredient _this) {
            return CrTItemStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_IITEM_STACK)
    public static class IItemStackExpansion {

        /**
         * {@inheritDoc}
         *
         * @implNote Override our expansion of {@link IIngredient} to use the {@link IItemStack} param based {@link CrTItemStackIngredient}.
         */
        @ZenCodeType.Caster(implicit = true)
        public static CrTItemStackIngredient asItemStackIngredient(IItemStack _this) {
            return CrTItemStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_INGREDIENT_LIST)
    public static class IngredientListExpansion {

        /**
         * {@inheritDoc}
         *
         * @implNote Override our expansion of {@link IIngredient} to use the {@link MCIngredientList} param based {@link CrTItemStackIngredient}.
         */
        @ZenCodeType.Caster(implicit = true)
        public static CrTItemStackIngredient asItemStackIngredient(MCIngredientList _this) {
            return CrTItemStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_ITEM_TAG)
    public static class ItemTagExpansion {

        /**
         * Allows for casting {@link MCTag<MCItemDefinition>}s to {@link CrTItemStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static CrTItemStackIngredient asItemStackIngredient(MCTag<MCItemDefinition> _this) {
            return CrTItemStackIngredient.from(ExpandItemTag.asIIngredient(_this));
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_MC_ITEM_DEFINITION)
    public static class MCItemDefinitionExpansion {

        /**
         * Allows for casting {@link MCItemDefinition}s to {@link CrTItemStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static CrTItemStackIngredient asItemStackIngredient(MCItemDefinition _this) {
            return CrTItemStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_IFLUID_STACK)
    public static class IFluidStackExpansion {

        /**
         * Allows for casting {@link IFluidStack}s to {@link CrTFluidStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static CrTFluidStackIngredient asFluidStackIngredient(IFluidStack _this) {
            return CrTFluidStackIngredient.from(_this);
        }
    }
}