package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCItemStackMutable;
import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTFloatingLong;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.ingredient.CrTFluidStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTGasStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.PressurizedReactionIRecipe;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_REACTION)
public class PressurizedReactionRecipeManager extends MekanismRecipeManager<PressurizedReactionRecipe> {

    public static final PressurizedReactionRecipeManager INSTANCE = new PressurizedReactionRecipeManager();

    private PressurizedReactionRecipeManager() {
        super(MekanismRecipeType.REACTION);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CrTItemStackIngredient inputSolid, CrTFluidStackIngredient inputFluid, CrTGasStackIngredient inputGas, int duration,
          IItemStack outputItem, @ZenCodeType.Optional("0 as " + CrTConstants.CLASS_FLOATING_LONG) CrTFloatingLong energyRequired) {
        addRecipe(name, inputSolid, inputFluid, inputGas, duration, getAndValidateNotEmpty(outputItem), GasStack.EMPTY, energyRequired);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CrTItemStackIngredient inputSolid, CrTFluidStackIngredient inputFluid, CrTGasStackIngredient inputGas, int duration,
          ICrTGasStack outputGas, @ZenCodeType.Optional("0 as " + CrTConstants.CLASS_FLOATING_LONG) CrTFloatingLong energyRequired) {
        addRecipe(name, inputSolid, inputFluid, inputGas, duration, ItemStack.EMPTY, getAndValidateNotEmpty(outputGas), energyRequired);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CrTItemStackIngredient inputSolid, CrTFluidStackIngredient inputFluid, CrTGasStackIngredient inputGas, int duration,
          IItemStack outputItem, ICrTGasStack outputGas, @ZenCodeType.Optional("0 as " + CrTConstants.CLASS_FLOATING_LONG) CrTFloatingLong energyRequired) {
        addRecipe(name, inputSolid, inputFluid, inputGas, duration, getAndValidateNotEmpty(outputItem), getAndValidateNotEmpty(outputGas), energyRequired);
    }

    private void addRecipe(String name, CrTItemStackIngredient inputSolid, CrTFluidStackIngredient inputFluid, CrTGasStackIngredient inputGas,
          int duration, ItemStack outputItem, GasStack outputGas, CrTFloatingLong energyRequired) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive! Duration: " + duration);
        }
        addRecipe(new PressurizedReactionIRecipe(getAndValidateName(name), inputSolid.getInternal(), inputFluid.getInternal(), inputGas.getInternal(),
              energyRequired.getInternalAsConst(), duration, outputItem, outputGas));
    }

    @Override
    protected ActionAddMekanismRecipe getAction(PressurizedReactionRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                Pair<List<@NonNull ItemStack>, @NonNull GasStack> output = getRecipe().getOutputDefinition();
                StringBuilder builder = new StringBuilder();
                List<ItemStack> itemOutputs = output.getLeft();
                if (!itemOutputs.isEmpty()) {
                    builder.append("item: ").append(CrTUtils.describeOutputs(itemOutputs, MCItemStackMutable::new));
                }
                GasStack gasOutput = output.getRight();
                if (!gasOutput.isEmpty()) {
                    if (!itemOutputs.isEmpty()) {
                        builder.append("; ");
                    }
                    builder.append("gas: ").append(new CrTGasStack(gasOutput));
                }
                return builder.toString();
            }
        };
    }
}