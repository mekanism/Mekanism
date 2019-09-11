package mekanism.client.jei.machine;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiPowerBar.IPowerInfoHandler;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.recipe.GasConversionHandler;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;

public class ItemStackGasToItemStackRecipeCategory extends BaseRecipeCategory<ItemStackGasToItemStackRecipeWrapper> {

    public ItemStackGasToItemStackRecipeCategory(IGuiHelper helper, String name, String unlocalized, ProgressBar progress) {
        super(helper, "mekanism:gui/GuiAdvancedMachine.png", name, unlocalized, progress, 28, 16, 144, 54);
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(new GuiSlot(SlotType.INPUT, this, guiLocation, 55, 16));
        guiElements.add(new GuiSlot(SlotType.POWER, this, guiLocation, 30, 34).with(SlotOverlay.POWER));
        guiElements.add(new GuiSlot(SlotType.EXTRA, this, guiLocation, 55, 52));
        guiElements.add(new GuiSlot(SlotType.OUTPUT_LARGE, this, guiLocation, 111, 30));
        guiElements.add(new GuiPowerBar(this, new IPowerInfoHandler() {
            @Override
            public double getLevel() {
                return 1F;
            }
        }, guiLocation, 164, 15));
        guiElements.add(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return (double) timer.getValue() / 20F;
            }
        }, progressBar, this, guiLocation, 77, 37));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ItemStackGasToItemStackRecipeWrapper recipeWrapper, IIngredients ingredients) {
        ItemStackGasToItemStackRecipe tempRecipe = recipeWrapper.getRecipe();
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 27, 0);
        itemStacks.init(1, false, 87, 18);
        itemStacks.init(2, false, 27, 36);
        itemStacks.set(0, tempRecipe.getItemInput().getRepresentations());
        itemStacks.set(1, tempRecipe.getOutputDefinition());
        GasStackIngredient gasInput = tempRecipe.getGasInput();
        List<ItemStack> gasItemProviders = new ArrayList<>();
        @NonNull List<@NonNull GasStack> gasInputs = gasInput.getRepresentations();
        List<GasStack> scaledGases = new ArrayList<>();
        int scale = TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED * TileEntityAdvancedElectricMachine.BASE_GAS_PER_TICK;
        for (GasStack gas : gasInputs) {
            gasItemProviders.addAll(GasConversionHandler.getStacksForGas(gas.getGas()));
            //While we are already looping the gases ensure we scale it to get the average amount that will get used over all
            scaledGases.add(gas.copy().withAmount(scale));
        }
        itemStacks.set(2, gasItemProviders);
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initGas(gasStacks, 0, true, 33, 21, 6, 12, scaledGases, false);
    }
}