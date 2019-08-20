package mekanism.client.jei.machine;

import java.util.List;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
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
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.GasConversionHandler;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;

public abstract class AdvancedMachineRecipeCategory<RECIPE extends AdvancedMachineRecipe<RECIPE>> extends BaseRecipeCategory<RECIPE> {

    public AdvancedMachineRecipeCategory(IGuiHelper helper, MekanismBlock mekanismBlock, ProgressBar progress) {
        super(helper, "mekanism:gui/advanced_machine.png", mekanismBlock, progress, 28, 16, 144, 54);
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
    public void setIngredients(RECIPE recipe, IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, recipe.getInput().itemStack);
        ingredients.setInput(MekanismJEI.TYPE_GAS, new GasStack(recipe.getInput().gasType,
              TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED * TileEntityAdvancedElectricMachine.BASE_GAS_PER_TICK));
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput().output);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, RECIPE recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 27, 0);
        itemStacks.init(1, false, 87, 18);
        itemStacks.init(2, false, 27, 36);
        itemStacks.set(0, recipe.recipeInput.itemStack);
        itemStacks.set(1, recipe.recipeOutput.output);
        itemStacks.set(2, getFuelStacks(recipe.recipeInput.gasType));
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initGas(gasStacks, 0, true, 33, 21, 6, 12, new GasStack(recipe.recipeInput.gasType, TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED
                                                                                            * TileEntityAdvancedElectricMachine.BASE_GAS_PER_TICK), false);
    }

    public List<ItemStack> getFuelStacks(Gas gasType) {
        return GasConversionHandler.getStacksForGas(gasType);
    }
}