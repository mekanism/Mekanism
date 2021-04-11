package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCIngredientList;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.blamejared.crafttweaker.impl.tag.MCTagWithAmount;
import com.blamejared.crafttweaker_annotations.annotations.TypedExpansion;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.api.recipes.inputs.chemical.SlurryStackIngredient;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTGas;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTInfuseType;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTPigment;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTSlurry;
import mekanism.common.integration.crafttweaker.ingredient.CrTFluidStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTGasStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTInfusionStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTItemStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTPigmentStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTSlurryStackIngredient;
import net.minecraft.fluid.Fluid;
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
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_ITEM_AMOUNT_TAG)
    public static class ItemTagWithAmountExpansion {

        /**
         * Allows for casting {@link MCTagWithAmount<Item>}s to {@link ItemStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static ItemStackIngredient asItemStackIngredient(MCTagWithAmount<Item> _this) {
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

    @ZenRegister
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_FLUID_AMOUNT_TAG)
    public static class FluidTagWithAmountExpansion {

        /**
         * Allows for casting {@link MCTagWithAmount<Fluid>}s to {@link FluidStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static FluidStackIngredient asFluidStackIngredient(MCTagWithAmount<Fluid> _this) {
            return CrTFluidStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_GAS_AMOUNT_TAG)
    public static class GasTagWithAmountExpansion {

        /**
         * Allows for casting {@link MCTagWithAmount<ICrTGas>}s to {@link GasStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static GasStackIngredient asGasStackIngredient(MCTagWithAmount<ICrTGas> _this) {
            return CrTGasStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_INFUSE_TYPE_AMOUNT_TAG)
    public static class InfuseTypeTagWithAmountExpansion {

        /**
         * Allows for casting {@link MCTagWithAmount<ICrTInfuseType>}s to {@link InfusionStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static InfusionStackIngredient asGasStackIngredient(MCTagWithAmount<ICrTInfuseType> _this) {
            return CrTInfusionStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_PIGMENT_AMOUNT_TAG)
    public static class PigmentTagWithAmountExpansion {

        /**
         * Allows for casting {@link MCTagWithAmount<ICrTPigment>}s to {@link PigmentStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static PigmentStackIngredient asGasStackIngredient(MCTagWithAmount<ICrTPigment> _this) {
            return CrTPigmentStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_SLURRY_AMOUNT_TAG)
    public static class SlurryTagWithAmountExpansion {

        /**
         * Allows for casting {@link MCTagWithAmount<ICrTSlurry>}s to {@link SlurryStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static SlurryStackIngredient asGasStackIngredient(MCTagWithAmount<ICrTSlurry> _this) {
            return CrTSlurryStackIngredient.from(_this);
        }
    }
}