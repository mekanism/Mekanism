package mekanism.client.jei.machine.chemical;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.MekanismBlock;
import mekanism.common.MekanismGases;
import mekanism.common.recipe.machines.DissolutionRecipe;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class ChemicalDissolutionChamberRecipeCategory extends BaseRecipeCategory<DissolutionRecipe> {

    public ChemicalDissolutionChamberRecipeCategory(IGuiHelper helper) {
        //TODO: previously had a lang entry for a shorter path
        super(helper, "mekanism:gui/nei/chemical_dissolution_chamber.png", MekanismBlock.CHEMICAL_DISSOLUTION_CHAMBER, null, 3, 3, 170, 79);
    }

    @Override
    public void draw(DissolutionRecipe recipe, double mouseX, double mouseY) {
        super.draw(recipe, mouseX, mouseY);
        drawTexturedRect(64 - xOffset, 40 - yOffset, 176, 63, (int) (48 * ((float) timer.getValue() / 20F)), 8);
    }

    @Override
    public Class<? extends DissolutionRecipe> getRecipeClass() {
        return DissolutionRecipe.class;
    }

    @Override
    public void setIngredients(DissolutionRecipe recipe, IIngredients ingredients) {
        ingredients.setInput(MekanismJEI.TYPE_GAS, new GasStack(MekanismGases.SULFURIC_ACID, TileEntityChemicalDissolutionChamber.BASE_INJECT_USAGE * TileEntityChemicalDissolutionChamber.BASE_TICKS_REQUIRED));
        ingredients.setInput(VanillaTypes.ITEM, recipe.recipeInput.ingredient);
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.recipeOutput.output);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, DissolutionRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 25 - xOffset, 35 - yOffset);
        itemStacks.set(0, recipe.getInput().ingredient);
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initGas(gasStacks, 0, true, 6 - xOffset, 5 - yOffset, 16, 58,
              new GasStack(MekanismGases.SULFURIC_ACID, TileEntityChemicalDissolutionChamber.BASE_INJECT_USAGE * TileEntityChemicalDissolutionChamber.BASE_TICKS_REQUIRED),
              true);
        initGas(gasStacks, 1, false, 134 - xOffset, 14 - yOffset, 16, 58, recipe.getOutput().output, true);
    }
}