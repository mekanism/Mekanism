package mekanism.common.integration.crafttweaker.helpers;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.IngredientAny;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.gas.CraftTweakerGasStack;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import mekanism.common.recipe.ingredients.IMekanismIngredient;
import mekanism.common.recipe.ingredients.IngredientMekIngredientWrapper;
import mekanism.common.recipe.ingredients.ItemStackMekIngredient;
import mekanism.common.recipe.ingredients.OredictMekIngredient;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
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
            return CraftTweakerMC.getIItemStack((ItemStack) ingredient);
        } else if (ingredient instanceof GasStack) {
            return new CraftTweakerGasStack((GasStack) ingredient);
        } else if (ingredient instanceof Gas) {
            return new CraftTweakerGasStack(new GasStack((Gas) ingredient, 1));
        } else if (ingredient instanceof FluidStack) {
            return CraftTweakerMC.getILiquidStack((FluidStack) ingredient);
        } else if (ingredient instanceof Fluid) {
            return CraftTweakerMC.getILiquidStack(new FluidStack((Fluid) ingredient, 1));
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
            return toMatch != null && toMatch.matches((ILiquidStack) input);
        }
        //TODO: Support other types of things like ore dict
        return false;
    }

    public static boolean matches(Object input, IIngredient toMatch) {
        return matches(getIngredient(input), toMatch);
    }

    public static IMekanismIngredient<ItemStack> getMekanismIngredient(IIngredient ingredient) {
        if (ingredient instanceof IOreDictEntry) {
            return new OredictMekIngredient(((IOreDictEntry) ingredient).getName());
        } else if (ingredient instanceof IItemStack) {
            return new ItemStackMekIngredient(CraftTweakerMC.getItemStack(ingredient));
        }
        return new IngredientMekIngredientWrapper(CraftTweakerMC.getIngredient(ingredient));
    }

    public static FluidStack toFluid(ILiquidStack fluid) {
        return fluid == null ? null : FluidRegistry.getFluidStack(fluid.getName(), fluid.getAmount());
    }

    public static FluidStackIngredient toIngredient(ILiquidStack fluid) {
        return fluid == null ? null : FluidStackIngredient.from(FluidRegistry.getFluidStack(fluid.getName(), fluid.getAmount()));
    }

    public static ItemStackIngredient toIngredient(@Nonnull IIngredient ingredient) {
        return ItemStackIngredient.from(CraftTweakerMC.getIngredient(ingredient), ingredient.getAmount());
    }
}