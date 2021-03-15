package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.blamejared.crafttweaker.impl.tag.manager.TagManagerFluid;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.tags.ITag;
import net.minecraftforge.fluids.FluidStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = FluidStackIngredient.class, zenCodeName = CrTConstants.CLASS_FLUID_STACK_INGREDIENT)
public class CrTFluidStackIngredient {

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
        FluidStack fluidStack = instance.getInternal();
        if (fluidStack.isEmpty()) {
            throw new IllegalArgumentException("FluidStackIngredients cannot be created from an empty stack.");
        }
        return FluidStackIngredient.from(fluidStack);
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
        ITag<Fluid> tag = CrTIngredientHelper.assertValidAndGet(fluidTag, amount, TagManagerFluid.INSTANCE::getInternal, "FluidStackIngredients");
        return FluidStackIngredient.from(tag, amount);
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
}