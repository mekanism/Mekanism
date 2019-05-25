package mekanism.client.jei.machine;

import java.util.Arrays;
import javax.annotation.Nonnull;
import mekanism.common.recipe.machines.ChanceMachineRecipe;
import mekanism.common.recipe.outputs.ChanceOutput;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class ChanceMachineRecipeWrapper<RECIPE extends ChanceMachineRecipe<RECIPE>> extends MekanismRecipeWrapper<RECIPE> {

    public ChanceMachineRecipeWrapper(RECIPE recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ChanceOutput output = recipe.getOutput();
        ingredients.setInput(VanillaTypes.ITEM, recipe.getInput().ingredient);
        ingredients.setOutputs(VanillaTypes.ITEM, Arrays.asList(output.primaryOutput, output.secondaryOutput));
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        ChanceOutput output = recipe.getOutput();
        if (output.hasSecondary()) {
            FontRenderer fontRendererObj = minecraft.fontRenderer;
            fontRendererObj.drawString(Math.round(output.secondaryChance * 100) + "%", 104, 41, 0x404040, false);
        }
    }
}