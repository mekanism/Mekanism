package mekanism.client.jei.machine.chemical;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.MekanismFluids;
import mekanism.common.recipe.machines.DissolutionRecipe;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class ChemicalDissolutionChamberRecipeCategory extends BaseRecipeCategory {

    private final IDrawable background;

    public ChemicalDissolutionChamberRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/nei/GuiChemicalDissolutionChamber.png", "chemical_dissolution_chamber",
              "gui.chemicalDissolutionChamber.short", null);

        xOffset = 3;
        yOffset = 3;

        background = guiHelper.createDrawable(new ResourceLocation(guiTexture), xOffset, yOffset, 170, 79);
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        super.drawExtras(minecraft);

        float f = (float) timer.getValue() / 20F;
        drawTexturedRect(64 - xOffset, 40 - yOffset, 176, 63, (int) (48 * f), 8);
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) {
        if (!(recipeWrapper instanceof ChemicalDissolutionChamberRecipeWrapper)) {
            return;
        }

        DissolutionRecipe tempRecipe = ((ChemicalDissolutionChamberRecipeWrapper) recipeWrapper).getRecipe();

        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

        itemStacks.init(0, true, 25 - xOffset, 35 - yOffset);
        itemStacks.set(0, tempRecipe.getInput().ingredient);

        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);

        initGas(gasStacks, 0, true, 6 - xOffset, 5 - yOffset, 16, 58,
              new GasStack(MekanismFluids.SulfuricAcid, TileEntityChemicalDissolutionChamber.BASE_INJECT_USAGE
                    * TileEntityChemicalDissolutionChamber.BASE_TICKS_REQUIRED),
              true);
        initGas(gasStacks, 1, false, 134 - xOffset, 14 - yOffset, 16, 58, tempRecipe.getOutput().output, true);
    }
}
