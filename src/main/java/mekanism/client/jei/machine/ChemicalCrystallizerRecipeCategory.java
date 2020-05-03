package mekanism.client.jei.machine;

import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.GasToItemStackRecipe;
import mekanism.client.gui.element.custom.GuiCrystallizerScreen;
import mekanism.client.gui.element.custom.GuiCrystallizerScreen.IOreInfo;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class ChemicalCrystallizerRecipeCategory extends BaseRecipeCategory<GasToItemStackRecipe> {

    public ChemicalCrystallizerRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.CHEMICAL_CRYSTALLIZER, 5, 3, 147, 79);
    }

    @Override
    protected void addGuiElements() {
        //TODO: Eventually make this be able to display the proper ores cycling at some point
        guiElements.add(new GuiCrystallizerScreen(this, 31, 13, new IOreInfo() {
            @Nonnull
            @Override
            public GasStack getInputGas() {
                return GasStack.EMPTY;
            }

            @Nullable
            @Override
            public GasToItemStackRecipe getRecipe() {
                return null;
            }
        }));
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 7, 4));
        guiElements.add(new GuiSlot(SlotType.EXTRA, this, 7, 64).with(SlotOverlay.PLUS));
        guiElements.add(new GuiSlot(SlotType.OUTPUT, this, 128, 56));
        guiElements.add(new GuiProgress(() -> timer.getValue() / 20D, ProgressType.LARGE_RIGHT, this, 53, 61));
    }

    @Override
    public Class<? extends GasToItemStackRecipe> getRecipeClass() {
        return GasToItemStackRecipe.class;
    }

    @Override
    public void setIngredients(GasToItemStackRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, GasToItemStackRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, false, 128 - xOffset, 56 - yOffset);
        itemStacks.set(0, recipe.getOutputDefinition());
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initGas(gasStacks, 0, true, 8 - xOffset, 5 - yOffset, 16, 58, recipe.getInput().getRepresentations(), true);
    }
}