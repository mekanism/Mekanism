package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.ingredient.type.IIngredientList;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker_annotations.annotations.TypedExpansion;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.SlurryStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTFluidStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTGasStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTInfusionStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTItemStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTPigmentStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTSlurryStackIngredient;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
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

        private IIngredientExpansion() {
        }

        /**
         * Allows for casting {@link IIngredient}s to {@link ItemStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static ItemStackIngredient asItemStackIngredient(IIngredient _this) {
            return CrTItemStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @TypedExpansion(IIngredientWithAmount.class)
    public static class IIngredientWithAmountExpansion {

        private IIngredientWithAmountExpansion() {
        }

        /**
         * Allows for casting {@link IIngredientWithAmount}s to {@link ItemStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static ItemStackIngredient asItemStackIngredient(IIngredientWithAmount _this) {
            return CrTItemStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @TypedExpansion(IItemStack.class)
    public static class IItemStackExpansion {

        private IItemStackExpansion() {
        }

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
    @TypedExpansion(IIngredientList.class)
    public static class IngredientListExpansion {

        private IngredientListExpansion() {
        }

        /**
         * {@inheritDoc}
         *
         * @implNote Override our expansion of {@link IIngredient} to use the {@link IIngredientList} param based {@link ItemStackIngredient}.
         */
        @ZenCodeType.Caster(implicit = true)
        public static ItemStackIngredient asItemStackIngredient(IIngredientList _this) {
            return CrTItemStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_ITEM_TAG)
    public static class ItemTagExpansion {

        private ItemTagExpansion() {
        }

        /**
         * Allows for casting {@link KnownTag<Item>}s to {@link ItemStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static ItemStackIngredient asItemStackIngredient(KnownTag<Item> _this) {
            return CrTItemStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @ZenCodeType.Expansion(CrTConstants.EXPANSION_TARGET_ITEM_AMOUNT_TAG)
    public static class ItemTagWithAmountExpansion {

        private ItemTagWithAmountExpansion() {
        }

        /**
         * Allows for casting {@link Many<KnownTag<Item>>}s to {@link ItemStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static ItemStackIngredient asItemStackIngredient(Many<KnownTag<Item>> _this) {
            return CrTItemStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @TypedExpansion(Item.class)
    public static class ItemExpansion {

        private ItemExpansion() {
        }

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

        private IFluidStackExpansion() {
        }

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

        private FluidTagWithAmountExpansion() {
        }

        /**
         * Allows for casting {@link Many<KnownTag<Fluid>>}s to {@link FluidStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static FluidStackIngredient asFluidStackIngredient(Many<KnownTag<Fluid>> _this) {
            return CrTFluidStackIngredient.from(_this);
        }
    }

    @ZenRegister
    @TypedExpansion(CTFluidIngredient.class)
    public static class CTFluidIngredientExpansion {

        private CTFluidIngredientExpansion() {
        }

        /**
         * Allows for casting {@link CTFluidIngredient}s to {@link FluidStackIngredient} without even needing to specify the cast.
         */
        @ZenCodeType.Caster(implicit = true)
        public static FluidStackIngredient asFluidStackIngredient(CTFluidIngredient _this) {
            return CrTFluidStackIngredient.from(_this);
        }
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