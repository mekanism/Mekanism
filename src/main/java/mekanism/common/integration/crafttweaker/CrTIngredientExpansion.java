package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCIngredientList;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.blamejared.crafttweaker_annotations.annotations.TypedExpansion;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTFluidStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTItemStackIngredient;
import net.minecraft.item.Item;
import org.openzen.zencode.java.ZenCodeType;

/**
 * Helper class to keep track of multiple tiny expansions to various CraftTweaker classes to allow for implicit casting to our wrapper ingredients.
 */
public class CrTIngredientExpansion {

    private CrTIngredientExpansion() {
    }

    @ZenRegister
    @TypedExpansion(IIngredient.class)
    public static class IIngredientExpansion {

        /**
         * Allows for casting {@link IIngredient}s to {@link ItemStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static ItemStackIngredient asItemStackIngredient(IIngredient _this) {
            return CrTItemStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @TypedExpansion(IItemStack.class)
    public static class IItemStackExpansion {

        /**
         * {@inheritDoc}
         *
         * @implNote Override our expansion of {@link IIngredient} to use the {@link IItemStack} param based {@link ItemStackIngredient}.
         */
        @ZenCodeType.Caster(implicit = true)
        public static ItemStackIngredient asItemStackIngredient(IItemStack _this) {
            return CrTItemStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_INGREDIENT_LIST)
    public static class IngredientListExpansion {

        /**
         * {@inheritDoc}
         *
         * @implNote Override our expansion of {@link IIngredient} to use the {@link MCIngredientList} param based {@link ItemStackIngredient}.
         */
        @ZenCodeType.Caster(implicit = true)
        public static ItemStackIngredient asItemStackIngredient(MCIngredientList _this) {
            return CrTItemStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_ITEM_TAG)
    public static class ItemTagExpansion {

        /**
         * Allows for casting {@link MCTag<Item>}s to {@link ItemStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static ItemStackIngredient asItemStackIngredient(MCTag<Item> _this) {
            return CrTItemStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @TypedExpansion(Item.class)
    public static class ItemExpansion {

        /**
         * Allows for casting {@link Item}s to {@link ItemStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static ItemStackIngredient asItemStackIngredient(Item _this) {
            return CrTItemStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @TypedExpansion(IFluidStack.class)
    public static class IFluidStackExpansion {

        /**
         * Allows for casting {@link IFluidStack}s to {@link FluidStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static FluidStackIngredient asFluidStackIngredient(IFluidStack _this) {
            return CrTFluidStackIngredient.from(_this);
        }
    }
}