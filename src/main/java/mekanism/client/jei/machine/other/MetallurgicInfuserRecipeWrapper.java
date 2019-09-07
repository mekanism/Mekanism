package mekanism.client.jei.machine.other;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.client.jei.machine.MekanismRecipeWrapper;
import mekanism.common.InfuseStorage;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;

public class MetallurgicInfuserRecipeWrapper extends MekanismRecipeWrapper<MetallurgicInfuserRecipe> {

    public MetallurgicInfuserRecipeWrapper(MetallurgicInfuserRecipe recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<ItemStack> inputStacks = Arrays.asList(recipe.getItemInput().getMatchingStacks());
        List<ItemStack> infuseStacks = MetallurgicInfuserRecipeCategory.getInfuseStacks(recipe.getInfusionInput().getRepresentations());
        //TODO: Check
        //ingredients.setInput(VanillaTypes.ITEM, recipe.recipeInput.inputStack);
        ingredients.setInputLists(VanillaTypes.ITEM, Arrays.asList(inputStacks, infuseStacks));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
    }

    @Override
    public void drawInfo(Minecraft mc, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        if (mc.currentScreen != null) {
            mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
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