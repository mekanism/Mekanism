package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.blamejared.crafttweaker.impl.tag.manager.TagManagerFluid;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.tags.ITag;
import net.minecraftforge.fluids.FluidStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_FLUID_STACK_INGREDIENT)
public class CrTFluidStackIngredient extends CrTIngredientWrapper<FluidStack, FluidStackIngredient> {

    /**
     * Creates a {@link CrTFluidStackIngredient} that matches a given fluid and amount.
     *
     * @param fluid  Fluid to match
     * @param amount Amount needed
     *
     * @return A {@link CrTFluidStackIngredient} that matches a given fluid and amount.
     */
    @ZenCodeType.Method
    public static CrTFluidStackIngredient from(Fluid fluid, int amount) {
        assertValidAmount("FluidStackIngredients", amount);
        if (fluid == Fluids.EMPTY) {
            throw new IllegalArgumentException("FluidStackIngredients cannot be created from an empty fluid.");
        }
        return new CrTFluidStackIngredient(FluidStackIngredient.from(fluid, amount));
    }

    /**
     * Creates a {@link CrTFluidStackIngredient} that matches a given fluid stack.
     *
     * @param instance Fluid stack to match
     *
     * @return A {@link CrTFluidStackIngredient} that matches a given fluid stack.
     */
    @ZenCodeType.Method
    public static CrTFluidStackIngredient from(IFluidStack instance) {
        FluidStack fluidStack = instance.getInternal();
        if (fluidStack.isEmpty()) {
            throw new IllegalArgumentException("FluidStackIngredients cannot be created from an empty stack.");
        }
        return new CrTFluidStackIngredient(FluidStackIngredient.from(fluidStack));
    }

    /**
     * Creates a {@link CrTFluidStackIngredient} that matches a given fluid tag with a given amount.
     *
     * @param fluidTag Tag to match
     * @param amount   Amount needed
     *
     * @return A {@link CrTFluidStackIngredient} that matches a given fluid tag with a given amount.
     */
    @ZenCodeType.Method
    public static CrTFluidStackIngredient from(MCTag<Fluid> fluidTag, int amount) {
        ITag<Fluid> tag = assertValidAndGet(fluidTag, amount, TagManagerFluid.INSTANCE::getInternal, "FluidStackIngredients");
        return new CrTFluidStackIngredient(FluidStackIngredient.from(tag, amount));
    }

    /**
     * Combines multiple {@link CrTFluidStackIngredient}s into a single {@link CrTFluidStackIngredient}.
     *
     * @param crtIngredients Ingredients to combine
     *
     * @return A single {@link CrTFluidStackIngredient} representing all the passed in ingredients.
     */
    @ZenCodeType.Method
    public static CrTFluidStackIngredient createMulti(CrTFluidStackIngredient... crtIngredients) {
        return createMulti("FluidStackIngredients", FluidStackIngredient[]::new,
              ingredients -> new CrTFluidStackIngredient(FluidStackIngredient.createMulti(ingredients)), crtIngredients);
    }

    private CrTFluidStackIngredient(FluidStackIngredient ingredient) {
        super(ingredient);
    }
}