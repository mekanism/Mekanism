package mekanism.client.jei.machine;

import java.util.Collections;
import java.util.List;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraftforge.fluids.FluidStack;

public class FluidToFluidRecipeCategory extends BaseRecipeCategory<FluidToFluidRecipe> {

    public FluidToFluidRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, 3, 12, 170, 62);
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(new GuiInnerScreen(this, 48, 19, 80, 40));
        guiElements.add(new GuiDownArrow(this, 32, 39));
        guiElements.add(new GuiDownArrow(this, 136, 39));
        guiElements.add(new GuiHorizontalRateBar(this, () -> 1, 48, 63));
        guiElements.add(new GuiSlot(SlotType.INPUT, this, 28, 20));
        guiElements.add(new GuiSlot(SlotType.OUTPUT, this, 28, 51));
        guiElements.add(new GuiSlot(SlotType.INPUT, this, 132, 20));
        guiElements.add(new GuiSlot(SlotType.OUTPUT, this, 132, 51));
        guiElements.add(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 6, 13));
        guiElements.add(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 152, 13));
    }

    @Override
    public Class<? extends FluidToFluidRecipe> getRecipeClass() {
        return FluidToFluidRecipe.class;
    }

    @Override
    public void setIngredients(FluidToFluidRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutput(VanillaTypes.FLUID, recipe.getOutputRepresentation());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, FluidToFluidRecipe recipe, IIngredients ingredients) {
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        List<FluidStack> fluidInputs = recipe.getInput().getRepresentations();
        int max = fluidInputs.stream().mapToInt(FluidStack::getAmount).filter(input -> input >= 0).max().orElse(0);
        fluidStacks.init(0, true, 7 - xOffset, 14 - yOffset, 16, 58, max, false, fluidOverlayLarge);
        fluidStacks.init(1, false, 153 - xOffset, 14 - yOffset, 16, 58, recipe.getOutputRepresentation().getAmount(), false,
              fluidOverlayLarge);
        fluidStacks.set(0, fluidInputs);
        fluidStacks.set(1, recipe.getOutputRepresentation());
    }
}