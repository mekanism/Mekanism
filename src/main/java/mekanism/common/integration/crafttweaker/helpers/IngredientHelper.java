/*package mekanism.common.integration.crafttweaker.helpers;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCItemStack;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.gas.CraftTweakerGasStack;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import mekanism.common.temporary.ILiquidStack;
import mekanism.common.temporary.IngredientAny;
import mekanism.common.temporary.MCLiquidStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class IngredientHelper {

    private IngredientHelper() {
    }

    public static IIngredient optionalIngredient(IIngredient ingredient) {
        return ingredient != null ? ingredient : IngredientAny.INSTANCE;
    }

    public static boolean checkNotNull(String name, IIngredient... ingredients) {
        for (IIngredient ingredient : ingredients) {
            if (ingredient == null) {
                CraftTweakerAPI.logError(String.format("Required parameters missing for %s Recipe.", name));
                return false;
            }
        }
        return true;
    }

    private static IIngredient getIngredient(Object ingredient) {
        if (ingredient instanceof ItemStack) {
            return new MCItemStack((ItemStack) ingredient);
        } else if (ingredient instanceof GasStack) {
            return new CraftTweakerGasStack((GasStack) ingredient);
        } else if (ingredient instanceof Gas) {
            return new CraftTweakerGasStack(new GasStack((Gas) ingredient, 1));
        } else if (ingredient instanceof FluidStack) {
            return getILiquidStack((FluidStack) ingredient);
        } else if (ingredient instanceof Fluid) {
            return getILiquidStack(new FluidStack((Fluid) ingredient, 1));
        }
        //TODO: Support other types of things like ore dict
        return IngredientAny.INSTANCE;
    }

    public static boolean matches(IIngredient input, IIngredient toMatch) {
        if (input instanceof IGasStack) {
            return GasHelper.matches(toMatch, (IGasStack) input);
        } else if (input instanceof IItemStack) {
            return toMatch != null && toMatch.matches((IItemStack) input);
        } else if (input instanceof ILiquidStack) {
            //TODO
            return toMatch != null;// && toMatch.matches((ILiquidStack) input);
        }
        //TODO: Support other types of things like ore dict
        return false;
    }

    public static boolean matches(Object input, IIngredient toMatch) {
        return matches(getIngredient(input), toMatch);
    }

    public static ItemStackIngredient getMekanismIngredient(IIngredient ingredient) {
        if (ingredient instanceof MCTag) {
            return ItemStackIngredient.from(((MCTag) ingredient).getItemTag());
        } else if (ingredient instanceof IItemStack) {
            return ItemStackIngredient.from(getItemStack((IItemStack) ingredient));
        }
        return ItemStackIngredient.from(ingredient.asVanillaIngredient());
    }

    public static FluidStack toFluid(ILiquidStack fluid) {
        //TODO: Fluids CrT
        return FluidStack.EMPTY;//fluid == null ? FluidStack.EMPTY : FluidRegistry.getFluidStack(fluid.getName(), fluid.getAmount());
    }

    public static ILiquidStack getILiquidStack(@Nonnull FluidStack fluidStack) {
        return new MCLiquidStack(fluidStack);
    }

    public static ItemStack getItemStack(IItemStack crtItemStack) {
        return crtItemStack.getInternal();
    }

    public static FluidStackIngredient toIngredient(ILiquidStack fluid) {
        //TODO: Implement
        return null;//fluid == null ? null : FluidStackIngredient.from(FluidRegistry.getFluidStack(fluid.getName(), fluid.getAmount()));
    }

    @Nonnull
    public static ItemStackIngredient toIngredient(@Nonnull IIngredient ingredient) {
        //TODO: Implement
        return null;//ItemStackIngredient.from(CraftTweakerMC.getIngredient(ingredient), ingredient.getAmount());
    }
}*/