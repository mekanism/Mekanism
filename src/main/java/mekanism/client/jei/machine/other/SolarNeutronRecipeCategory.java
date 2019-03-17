package mekanism.client.jei.machine.other;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.recipe.machines.SolarNeutronRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class SolarNeutronRecipeCategory extends BaseRecipeCategory {

    private final IDrawable background;

    public SolarNeutronRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/nei/GuiSolarNeutronActivator.png", "solar_neutron_activator",
              "tile.MachineBlock3.SolarNeutronActivator.name", null);

        xOffset = 3;
        yOffset = 12;

        background = guiHelper.createDrawable(new ResourceLocation(guiTexture), xOffset, yOffset, 170, 70);
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        super.drawExtras(minecraft);

        drawTexturedRect(64 - xOffset, 39 - yOffset, 176, 58, 55, 8);
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) {
        if (!(recipeWrapper instanceof SolarNeutronRecipeWrapper)) {
            return;
        }

        SolarNeutronRecipe tempRecipe = ((SolarNeutronRecipeWrapper) recipeWrapper).getRecipe();

        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);

        initGas(gasStacks, 0, true, 26 - xOffset, 14 - yOffset, 16, 58, tempRecipe.recipeInput.ingredient, true);
        initGas(gasStacks, 1, false, 134 - xOffset, 14 - yOffset, 16, 58, tempRecipe.recipeOutput.output, true);
    }
}
