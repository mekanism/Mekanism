package mekanism.client.jei.machine;

import java.util.Arrays;
import java.util.Collections;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class SawmillRecipeCategory extends BaseRecipeCategory<SawmillRecipe> {

    public SawmillRecipeCategory(IGuiHelper helper, IBlockProvider mekanismBlock) {
        super(helper, "mekanism:gui/basic_machine.png", mekanismBlock, 28, 16, 144, 54);
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(new GuiSlot(SlotType.INPUT, this, 55, 16));
        guiElements.add(new GuiSlot(SlotType.POWER, this, 55, 52).with(SlotOverlay.POWER));
        guiElements.add(new GuiSlot(SlotType.OUTPUT_WIDE, this, 111, 30));
        guiElements.add(new GuiVerticalPowerBar(this, () -> 1F, 164, 15));
        guiElements.add(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return (double) timer.getValue() / 20F;
            }
        }, ProgressBar.BAR, this, 77, 37));
    }

    @Override
    public Class<? extends SawmillRecipe> getRecipeClass() {
        return SawmillRecipe.class;
    }

    @Override
    public void setIngredients(SawmillRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutputLists(VanillaTypes.ITEM, Arrays.asList(recipe.getMainOutputDefinition(), recipe.getSecondaryOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, SawmillRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 27, 0);
        itemStacks.init(1, false, 87, 18);
        itemStacks.init(2, false, 103, 18);
        itemStacks.set(0, recipe.getInput().getRepresentations());
        itemStacks.set(1, recipe.getMainOutputDefinition());
        itemStacks.set(2, recipe.getSecondaryOutputDefinition());
    }

    @Override
    public void draw(SawmillRecipe recipe, double mouseX, double mouseY) {
        super.draw(recipe, mouseX, mouseY);
        double secondaryChance = recipe.getSecondaryChance();
        if (secondaryChance > 0) {
            getFont().drawString(Math.round(secondaryChance * 100) + "%", 104, 41, 0x404040);
        }
    }
}