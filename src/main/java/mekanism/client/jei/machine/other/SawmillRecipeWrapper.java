package mekanism.client.jei.machine.other;

import java.util.Arrays;
import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.client.jei.machine.MekanismRecipeWrapper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class SawmillRecipeWrapper extends MekanismRecipeWrapper<SawmillRecipe> {

    public SawmillRecipeWrapper(SawmillRecipe recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(Arrays.asList(recipe.getInput().getMatchingStacks())));
        ingredients.setOutputLists(VanillaTypes.ITEM, Arrays.asList(recipe.getMainOutputDefinition(), recipe.getSecondaryOutputDefinition()));
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        double secondaryChance = recipe.getSecondaryChance();
        if (secondaryChance > 0) {
            FontRenderer fontRendererObj = minecraft.fontRenderer;
            fontRendererObj.drawString(Math.round(secondaryChance * 100) + "%", 104, 41, 0x404040, false);
        }
    }
}