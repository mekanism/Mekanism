package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.impl.fluid.MCFluid;
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
    public static CrTFluidStackIngredient from(MCTag<MCFluid> fluidTag, int amount) {
        ITag<Fluid> tag = assertValidAndGet(fluidTag, amount, TagManagerFluid.INSTANCE::getInternal, "FluidStackIngredients");
        return new CrTFluidStackIngredient(FluidStackIngredient.from(tag, amount));
    }

    @ZenCodeType.Method
    public static CrTFluidStackIngredient createMulti(CrTFluidStackIngredient... crtIngredients) {
        return createMulti("FluidStackIngredients",  FluidStackIngredient[]::new,
              ingredients -> new CrTFluidStackIngredient(FluidStackIngredient.createMulti(ingredients)), crtIngredients);
    }

    private CrTFluidStackIngredient(FluidStackIngredient ingredient) {
        super(ingredient);
    }
}