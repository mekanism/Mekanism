package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.base.IData;
import com.blamejared.crafttweaker.api.data.base.converter.JSONConverter;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.fluid.MCFluidStack;
import com.blamejared.crafttweaker.api.tag.MCTag;
import com.blamejared.crafttweaker.api.tag.manager.TagManagerFluid;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = FluidStackIngredient.class, zenCodeName = CrTConstants.CLASS_FLUID_STACK_INGREDIENT)
public class CrTFluidStackIngredient {

    private CrTFluidStackIngredient() {
    }

    /**
     * Creates a {@link FluidStackIngredient} that matches a given fluid and amount.
     *
     * @param fluid  Fluid to match
     * @param amount Amount needed
     *
     * @return A {@link FluidStackIngredient} that matches a given fluid and amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static FluidStackIngredient from(Fluid fluid, int amount) {
        CrTIngredientHelper.assertValidAmount("FluidStackIngredients", amount);
        if (fluid == Fluids.EMPTY) {
            throw new IllegalArgumentException("FluidStackIngredients cannot be created from an empty fluid.");
        }
        return FluidStackIngredient.from(fluid, amount);
    }

    /**
     * Creates a {@link FluidStackIngredient} that matches a given fluid stack.
     *
     * @param instance Fluid stack to match
     *
     * @return A {@link FluidStackIngredient} that matches a given fluid stack.
     */
    @ZenCodeType.StaticExpansionMethod
    public static FluidStackIngredient from(IFluidStack instance) {
        if (instance.isEmpty()) {
            throw new IllegalArgumentException("FluidStackIngredients cannot be created from an empty stack.");
        }
        return FluidStackIngredient.from(instance.getImmutableInternal());
    }

    /**
     * Creates a {@link FluidStackIngredient} that matches a given fluid tag with a given amount.
     *
     * @param fluidTag Tag to match
     * @param amount   Amount needed
     *
     * @return A {@link FluidStackIngredient} that matches a given fluid tag with a given amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static FluidStackIngredient from(MCTag<Fluid> fluidTag, int amount) {
        Tag<Fluid> tag = CrTIngredientHelper.assertValidAndGet(fluidTag, amount, TagManagerFluid.INSTANCE::getInternal, "FluidStackIngredients");
        return FluidStackIngredient.from(tag, amount);
    }

    /**
     * Creates a {@link FluidStackIngredient} that matches a given fluid tag with amount.
     *
     * @param fluidTag Tag and amount to match
     *
     * @return A {@link FluidStackIngredient} that matches a given fluid tag with amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static FluidStackIngredient from(Many<MCTag<Fluid>> fluidTag) {
        return from(fluidTag.getData(), fluidTag.getAmount());
    }

    /**
     * Combines multiple {@link FluidStackIngredient}s into a single {@link FluidStackIngredient}.
     *
     * @param ingredients Ingredients to combine
     *
     * @return A single {@link FluidStackIngredient} representing all the passed in ingredients.
     */
    @ZenCodeType.StaticExpansionMethod
    public static FluidStackIngredient createMulti(FluidStackIngredient... ingredients) {
        return CrTIngredientHelper.createMulti("FluidStackIngredients", FluidStackIngredient::createMulti, ingredients);
    }

    /**
     * Converts this {@link FluidStackIngredient} into JSON ({@link IData}).
     *
     * @return {@link FluidStackIngredient} as JSON.
     */
    @ZenCodeType.Method
    @ZenCodeType.Caster(implicit = true)
    public static IData asIData(FluidStackIngredient _this) {
        return JSONConverter.convert(_this.serialize());
    }

    /**
     * Checks if a given {@link IFluidStack} has a type match for this {@link FluidStackIngredient}. Type matches ignore stack size.
     *
     * @param type Type to check for a match
     *
     * @return {@code true} if the type is supported by this {@link FluidStackIngredient}.
     */
    @ZenCodeType.Method
    public static boolean testType(FluidStackIngredient _this, IFluidStack type) {
        return _this.testType(type.getInternal());
    }

    /**
     * Checks if a given {@link IFluidStack} matches this {@link FluidStackIngredient}. (Checks size for >=)
     *
     * @param stack Stack to check for a match
     *
     * @return {@code true} if the stack fulfills the requirements for this {@link FluidStackIngredient}.
     */
    @ZenCodeType.Method
    public static boolean test(FluidStackIngredient _this, IFluidStack stack) {
        return _this.test(stack.getInternal());
    }

    /**
     * Gets a list of valid instances for this {@link FluidStackIngredient}, may not include all or may be empty depending on how complex the ingredient is as the
     * internal version is mostly used for JEI display purposes.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("representations")
    public static List<IFluidStack> getRepresentations(FluidStackIngredient _this) {
        return CrTUtils.convert(_this.getRepresentations(), MCFluidStack::new);
    }

    /**
     * OR's this {@link FluidStackIngredient} with another {@link FluidStackIngredient} to create a multi {@link FluidStackIngredient}
     *
     * @param other {@link FluidStackIngredient} to combine with.
     *
     * @return Multi {@link FluidStackIngredient} that matches both the source {@link FluidStackIngredient} and the OR'd {@link FluidStackIngredient}.
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.OR)
    public static FluidStackIngredient or(FluidStackIngredient _this, FluidStackIngredient other) {
        return FluidStackIngredient.createMulti(_this, other);
    }
}