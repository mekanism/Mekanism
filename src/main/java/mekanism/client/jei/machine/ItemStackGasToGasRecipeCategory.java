package mekanism.client.jei.machine;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
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
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class ItemStackGasToGasRecipeCategory extends BaseRecipeCategory<ItemStackGasToGasRecipe> {

    public ItemStackGasToGasRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, 3, 3, 170, 79);
        //Note: This previously had a lang key for a shorter string. Though ideally especially due to translations
        // we will eventually instead just make the text scale
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 7, 4));
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 131, 13));
        guiElements.add(new GuiSlot(SlotType.POWER, this, 151, 4).with(SlotOverlay.POWER));
        guiElements.add(new GuiSlot(SlotType.INPUT, this, 27, 35));
        guiElements.add(new GuiSlot(SlotType.EXTRA, this, 151, 24).with(SlotOverlay.PLUS));
        guiElements.add(new GuiSlot(SlotType.EXTRA, this, 7, 64).with(SlotOverlay.MINUS));
        guiElements.add(new GuiProgress(() -> timer.getValue() / 20D, ProgressType.LARGE_RIGHT, this, 64, 40));
    }

    @Override
    public Class<? extends ItemStackGasToGasRecipe> getRecipeClass() {
        return ItemStackGasToGasRecipe.class;
    }

    @Override
    public void setIngredients(ItemStackGasToGasRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getItemInput().getRepresentations()));
        List<@NonNull GasStack> gasInputs = recipe.getGasInput().getRepresentations();
        int scale = TileEntityChemicalDissolutionChamber.BASE_INJECT_USAGE * TileEntityChemicalDissolutionChamber.BASE_TICKS_REQUIRED;
        List<GasStack> scaledGases = gasInputs.stream().map(gas -> new GasStack(gas, scale)).collect(Collectors.toList());
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(scaledGases));
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.getOutputDefinition());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ItemStackGasToGasRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 27 - xOffset, 35 - yOffset);
        itemStacks.set(0, recipe.getItemInput().getRepresentations());
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        List<@NonNull GasStack> gasInputs = recipe.getGasInput().getRepresentations();
        int scale = TileEntityChemicalDissolutionChamber.BASE_INJECT_USAGE * TileEntityChemicalDissolutionChamber.BASE_TICKS_REQUIRED;
        List<GasStack> scaledGases = gasInputs.stream().map(gas -> new GasStack(gas, scale)).collect(Collectors.toList());
        initGas(gasStacks, 0, true, 8 - xOffset, 5 - yOffset, 16, 58, scaledGases, true);
        initGas(gasStacks, 1, false, 132 - xOffset, 14 - yOffset, 16, 58, Collections.singletonList(recipe.getOutputDefinition()), true);
    }
}