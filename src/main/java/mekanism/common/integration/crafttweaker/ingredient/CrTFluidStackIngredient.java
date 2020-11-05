package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.impl.fluid.MCFluid;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_FLUID_STACK_INGREDIENT)
public class CrTFluidStackIngredient extends CrTIngredientWrapper<FluidStack, FluidStackIngredient> {

    @ZenCodeType.Method
    public static CrTFluidStackIngredient from(MCFluid instance, int amount) {
        assertValidAmount("FluidStackIngredients", amount);
        Fluid fluid = instance.getInternal();
        if (fluid == Fluids.EMPTY) {
            throw new IllegalArgumentException("FluidStackIngredients cannot be created from an empty fluid.");
        }
        return new CrTFluidStackIngredient(FluidStackIngredient.from(fluid, amount));
    }

    @ZenCodeType.Method
    public static CrTFluidStackIngredient from(IFluidStack instance) {
        FluidStack fluidStack = instance.getInternal();
        if (fluidStack.isEmpty()) {
            throw new IllegalArgumentException("FluidStackIngredients cannot be created from an empty stack.");
        }
        return new CrTFluidStackIngredient(FluidStackIngredient.from(fluidStack));
    }

    @ZenCodeType.Method
    public static CrTFluidStackIngredient from(MCTag fluidTag, int amount) {
        assertValid(fluidTag, amount, MCTag::isFluidTag, "FluidStackIngredients", "FluidTag");
        return new CrTFluidStackIngredient(FluidStackIngredient.from(fluidTag.getFluidTag(), amount));
    }

    @ZenCodeType.Method
    public static CrTFluidStackIngredient createMulti(CrTFluidStackIngredient... crtIngredients) {
        return createMulti(FluidStackIngredient[]::new, ingredients -> new CrTFluidStackIngredient(FluidStackIngredient.createMulti(ingredients)), crtIngredients);
    }

    private CrTFluidStackIngredient(FluidStackIngredient ingredient) {
        super(ingredient);
    }
}