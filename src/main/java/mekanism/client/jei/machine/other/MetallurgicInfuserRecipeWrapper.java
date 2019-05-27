package mekanism.client.jei.machine.other;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mekanism.client.jei.machine.MekanismRecipeWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.InfuseStorage;
import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class MetallurgicInfuserRecipeWrapper<RECIPE extends MetallurgicInfuserRecipe> extends MekanismRecipeWrapper<RECIPE> {

    public MetallurgicInfuserRecipeWrapper(RECIPE recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<ItemStack> inputStacks = Collections.singletonList(recipe.recipeInput.inputStack);
        List<ItemStack> infuseStacks = MetallurgicInfuserRecipeCategory.getInfuseStacks(recipe.getInput().infuse.getType());
        ingredients.setInput(VanillaTypes.ITEM, recipe.recipeInput.inputStack);
        ingredients.setInputLists(VanillaTypes.ITEM, Arrays.asList(inputStacks, infuseStacks));
        ingredients.setOutput(VanillaTypes.ITEM, recipe.recipeOutput.output);
    }

    @Override
    public void drawInfo(Minecraft mc, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        if (mc.currentScreen != null) {
            mc.renderEngine.bindTexture(MekanismRenderer.getBlocksTexture());
            mc.currentScreen.drawTexturedModalRect(2, 2, recipe.getInput().infuse.getType().sprite, 4, 52);
        }
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (mouseX >= 2 && mouseX < 6 && mouseY >= 2 && mouseY < 54) {
            InfuseStorage infuse = recipe.getInput().infuse;
            return Collections.singletonList(infuse.getType().getLocalizedName() + ": " + infuse.getAmount());
        }
        return Collections.emptyList();
    }
}