package mekanism.client.jei.machine.chemical;

import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.machine.ItemStackGasToGasRecipeWrapper;
import mekanism.common.MekanismFluids;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import scala.actors.threadpool.Arrays;

public class ChemicalDissolutionChamberRecipeCategory extends BaseRecipeCategory<ItemStackGasToGasRecipeWrapper> {

    public ChemicalDissolutionChamberRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/nei/GuiChemicalDissolutionChamber.png",
              Recipe.CHEMICAL_DISSOLUTION_CHAMBER.getJEICategory(), "gui.chemicalDissolutionChamber.short", null, 3, 3, 170, 79);
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        super.drawExtras(minecraft);
        drawTexturedRect(64 - xOffset, 40 - yOffset, 176, 63, (int) (48 * ((float) timer.getValue() / 20F)), 8);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ItemStackGasToGasRecipeWrapper recipeWrapper, IIngredients ingredients) {
        ItemStackGasToGasRecipe tempRecipe = recipeWrapper.getRecipe();
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 25 - xOffset, 35 - yOffset);
        itemStacks.set(0, tempRecipe.getItemInput().getRepresentations());
        //TODO: Show gas input as well
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initGas(gasStacks, 0, true, 6 - xOffset, 5 - yOffset, 16, 58,
              new GasStack(MekanismFluids.SulfuricAcid, TileEntityChemicalDissolutionChamber.BASE_INJECT_USAGE * TileEntityChemicalDissolutionChamber.BASE_TICKS_REQUIRED),
              true);
        initGas(gasStacks, 1, false, 134 - xOffset, 14 - yOffset, 16, 58, tempRecipe.getOutputDefinition(), true);
    }
}