package mekanism.common.integration.crafttweaker.helpers;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.IngredientAny;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.common.integration.crafttweaker.gas.CraftTweakerGasStack;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import mekanism.common.integration.crafttweaker.util.IngredientWrapper;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.inputs.ChemicalPairInput;
import mekanism.common.recipe.inputs.DoubleMachineInput;
import mekanism.common.recipe.inputs.FluidInput;
import mekanism.common.recipe.inputs.GasInput;
import mekanism.common.recipe.inputs.InfusionInput;
import mekanism.common.recipe.inputs.IntegerInput;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.inputs.PressurizedInput;
import mekanism.common.recipe.outputs.ChanceOutput;
import mekanism.common.recipe.outputs.ChemicalPairOutput;
import mekanism.common.recipe.outputs.FluidOutput;
import mekanism.common.recipe.outputs.GasOutput;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mekanism.common.recipe.outputs.MachineOutput;
import mekanism.common.recipe.outputs.PressurizedOutput;
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

    public static <INPUT extends MachineInput<INPUT>> boolean matches(INPUT in, IngredientWrapper toMatch) {
        if (in instanceof ItemStackInput) {
            ItemStackInput input = (ItemStackInput) in;
            return matches(input.ingredient, toMatch.getIngredient());
        } else if (in instanceof GasInput) {
            GasInput input = (GasInput) in;
            return matches(input.ingredient, toMatch.getIngredient());
        } else if (in instanceof FluidInput) {
            FluidInput input = (FluidInput) in;
            return matches(input.ingredient, toMatch.getIngredient());
        } else if (in instanceof AdvancedMachineInput) {
            AdvancedMachineInput input = (AdvancedMachineInput) in;
            return matches(input.itemStack, toMatch.getLeft()) && matches(input.gasType, toMatch.getRight());
        } else if (in instanceof ChemicalPairInput) {
            ChemicalPairInput input = (ChemicalPairInput) in;
            return matches(input.leftGas, toMatch.getLeft()) && matches(input.rightGas, toMatch.getRight());
        } else if (in instanceof DoubleMachineInput) {
            DoubleMachineInput input = (DoubleMachineInput) in;
            return matches(input.itemStack, toMatch.getLeft()) && matches(input.extraStack, toMatch.getRight());
        } else if (in instanceof PressurizedInput) {
            PressurizedInput input = (PressurizedInput) in;
            return matches(input.getSolid(), toMatch.getLeft()) && matches(input.getFluid(), toMatch.getMiddle())
                  && matches(input.getGas(), toMatch.getRight());
        } else if (in instanceof InfusionInput) {
            InfusionInput input = (InfusionInput) in;
            return matches(input.inputStack, toMatch.getIngredient()) && (toMatch.getInfuseType().isEmpty() || toMatch
                  .getInfuseType().equalsIgnoreCase(input.infuse.type.name));
        } else if (in instanceof IntegerInput) {
            IntegerInput input = (IntegerInput) in;
            return input.ingredient == toMatch.getAmount();
        }
        return false;
    }

    public static <OUTPUT extends MachineOutput<OUTPUT>> boolean matches(OUTPUT out, IngredientWrapper toMatch) {
        if (out instanceof ItemStackOutput) {
            ItemStackOutput output = (ItemStackOutput) out;
            return matches(output.output, toMatch.getIngredient());
        } else if (out instanceof GasOutput) {
            GasOutput output = (GasOutput) out;
            return matches(output.output, toMatch.getIngredient());
        } else if (out instanceof FluidOutput) {
            FluidOutput output = (FluidOutput) out;
            return matches(output.output, toMatch.getIngredient());
        } else if (out instanceof ChanceOutput) {
            ChanceOutput output = (ChanceOutput) out;
            return matches(output.primaryOutput, toMatch.getLeft()) && matches(output.secondaryOutput,
                  toMatch.getRight());
        } else if (out instanceof ChemicalPairOutput) {
            ChemicalPairOutput output = (ChemicalPairOutput) out;
            return matches(output.leftGas, toMatch.getLeft()) && matches(output.rightGas, toMatch.getRight());
        } else if (out instanceof PressurizedOutput) {
            PressurizedOutput output = (PressurizedOutput) out;
            return matches(output.getItemOutput(), toMatch.getLeft()) && matches(output.getGasOutput(),
                  toMatch.getRight());
        }
        return false;
    }

    public static FluidStack toFluid(ILiquidStack fluid) {
        return fluid == null ? null : FluidRegistry.getFluidStack(fluid.getName(), fluid.getAmount());
    }
}