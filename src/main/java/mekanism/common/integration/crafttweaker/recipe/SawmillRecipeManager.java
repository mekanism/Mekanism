package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCItemStackMutable;
import com.blamejared.crafttweaker.impl.item.MCWeightedItemStack;
import java.util.List;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.ingredient.CrTItemStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.SawmillIRecipe;
import mekanism.common.util.text.TextUtils;
import net.minecraft.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_SAWING)
public class SawmillRecipeManager extends MekanismRecipeManager<SawmillRecipe> {

    public static final SawmillRecipeManager INSTANCE = new SawmillRecipeManager();

    private SawmillRecipeManager() {
        super(MekanismRecipeType.SAWING);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CrTItemStackIngredient input, IItemStack mainOutput) {
        addRecipe(name, input, getAndValidateNotEmpty(mainOutput), ItemStack.EMPTY, 0);
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CrTItemStackIngredient input, MCWeightedItemStack secondaryOutput) {
        addRecipe(name, input, secondaryOutput.getItemStack(), secondaryOutput.getWeight());
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CrTItemStackIngredient input, IItemStack secondaryOutput, double secondaryChance) {
        addRecipe(name, input, ItemStack.EMPTY, getAndValidateNotEmpty(secondaryOutput), getAndValidateSecondaryChance(secondaryChance));
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CrTItemStackIngredient input, IItemStack mainOutput, MCWeightedItemStack secondaryOutput) {
        addRecipe(name, input, mainOutput, secondaryOutput.getItemStack(), secondaryOutput.getWeight());
    }

    @ZenCodeType.Method
    public void addRecipe(String name, CrTItemStackIngredient input, IItemStack mainOutput, IItemStack secondaryOutput, double secondaryChance) {
        addRecipe(name, input, getAndValidateNotEmpty(mainOutput), getAndValidateNotEmpty(secondaryOutput), getAndValidateSecondaryChance(secondaryChance));
    }

    private void addRecipe(String name, CrTItemStackIngredient input, ItemStack mainOutput, ItemStack secondaryOutput, double secondaryChance) {
        addRecipe(new SawmillIRecipe(getAndValidateName(name), input.getInternal(), mainOutput, secondaryOutput, secondaryChance));
    }

    private double getAndValidateSecondaryChance(double secondaryChance) {
        if (secondaryChance <= 0 || secondaryChance > 1) {
            throw new IllegalArgumentException("This sawing recipe requires a secondary output chance greater than zero and at most one.");
        }
        return secondaryChance;
    }

    @Override
    protected ActionAddMekanismRecipe getAction(SawmillRecipe recipe) {
        return new ActionAddMekanismRecipe(recipe) {
            @Override
            protected String describeOutputs() {
                SawmillRecipe recipe = getRecipe();
                StringBuilder builder = new StringBuilder();
                List<ItemStack> mainOutputs = recipe.getMainOutputDefinition();
                if (!mainOutputs.isEmpty()) {
                    builder.append("main: ").append(CrTUtils.describeOutputs(mainOutputs, MCItemStackMutable::new));
                }
                if (recipe.getSecondaryChance() > 0) {
                    if (!mainOutputs.isEmpty()) {
                        builder.append("; ");
                    }
                    if (recipe.getSecondaryChance() == 1) {
                        builder.append("secondary: ");
                    } else {
                        builder.append("secondary with chance ")
                              .append(TextUtils.getPercent(recipe.getSecondaryChance()))
                              .append(": ");
                    }
                    builder.append(CrTUtils.describeOutputs(recipe.getSecondaryOutputDefinition(), MCItemStackMutable::new));
                }
                return builder.toString();
            }
        };
    }
}