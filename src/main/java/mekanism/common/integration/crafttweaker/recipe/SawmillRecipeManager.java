package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.item.MCItemStackMutable;
import java.util.List;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.text.TextUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_RECIPE_SAWING)
public class SawmillRecipeManager extends MekanismRecipeManager {

    public static final SawmillRecipeManager INSTANCE = new SawmillRecipeManager();

    private SawmillRecipeManager() {
    }

    @Override
    public IRecipeType<SawmillRecipe> getRecipeType() {
        return MekanismRecipeType.SAWING;
    }

    private static class ActionAddSawmillRecipe extends ActionAddMekanismRecipe<SawmillRecipe> {

        protected ActionAddSawmillRecipe(MekanismRecipeManager recipeManager, SawmillRecipe recipe) {
            super(recipeManager, recipe);
        }

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
    }
}