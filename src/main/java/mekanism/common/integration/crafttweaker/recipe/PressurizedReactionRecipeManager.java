package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.item.MCItemStackMutable;
import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.recipe.MekanismRecipeType;
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