package mekanism.client.jei.machine.other;

import mekanism.api.gas.GasStack;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiPowerBar.IPowerInfoHandler;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.machines.PressurizedRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class PRCRecipeCategory extends BaseRecipeCategory<PressurizedRecipe> {

    public PRCRecipeCategory(IGuiHelper helper) {
        //TODO: Had a lang key for shorter name
        super(helper, "mekanism:gui/nei/pressurized_reaction_chamber.png", MekanismBlock.PRESSURIZED_REACTION_CHAMBER, ProgressBar.MEDIUM, 3, 11, 170, 68);
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(new GuiSlot(SlotType.INPUT, this, guiLocation, 53, 34));
        guiElements.add(new GuiSlot(SlotType.POWER, this, guiLocation, 140, 18).with(SlotOverlay.POWER));
        guiElements.add(new GuiSlot(SlotType.OUTPUT, this, guiLocation, 115, 34));
        guiElements.add(GuiFluidGauge.getDummy(GuiGauge.Type.STANDARD_YELLOW, this, guiLocation, 5, 10));
        guiElements.add(GuiGasGauge.getDummy(GuiGauge.Type.STANDARD_RED, this, guiLocation, 28, 10));
        guiElements.add(GuiGasGauge.getDummy(GuiGauge.Type.SMALL_BLUE, this, guiLocation, 140, 40));
        guiElements.add(new GuiPowerBar(this, new IPowerInfoHandler() {
            @Override
            public double getLevel() {
                return 1F;
            }
        }, guiLocation, 164, 15));
        guiElements.add(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return (float) timer.getValue() / 20F;
            }
        }, progressBar, this, guiLocation, 75, 37));
    }

    @Override
    public Class<? extends PressurizedRecipe> getRecipeClass() {
        return PressurizedRecipe.class;
    }

    @Override
    public void setIngredients(PressurizedRecipe recipe, IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, recipe.recipeInput.getSolid());
        ingredients.setInput(VanillaTypes.FLUID, recipe.recipeInput.getFluid());
        ingredients.setInput(MekanismJEI.TYPE_GAS, recipe.recipeInput.getGas());
        ingredients.setOutput(VanillaTypes.ITEM, recipe.recipeOutput.getItemOutput());
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.recipeOutput.getGasOutput());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, PressurizedRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 53 - xOffset, 34 - yOffset);
        itemStacks.init(1, false, 115 - xOffset, 34 - yOffset);
        itemStacks.set(0, recipe.recipeInput.getSolid());
        itemStacks.set(1, recipe.recipeOutput.getItemOutput());
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        fluidStacks.init(0, true, 3, 0, 16, 58, recipe.getInput().getFluid().getAmount(), false, fluidOverlayLarge);
        fluidStacks.set(0, recipe.recipeInput.getFluid());
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initGas(gasStacks, 0, true, 29 - xOffset, 11 - yOffset, 16, 58, recipe.recipeInput.getGas(), true);
        initGas(gasStacks, 1, false, 141 - xOffset, 41 - yOffset, 16, 28, recipe.recipeOutput.getGasOutput(), true);
    }
}