package mekanism.client.jei.machine;

import java.util.Collections;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.gauge.GuiSlurryGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class FluidSlurryToSlurryRecipeCategory extends BaseRecipeCategory<FluidSlurryToSlurryRecipe> {

    private final GuiGauge<?> fluidInput;
    private final GuiGauge<?> slurryInput;
    private final GuiGauge<?> output;

    public FluidSlurryToSlurryRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.CHEMICAL_WASHER, 7, 13, 162, 60);
        fluidInput = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 7, 13));
        slurryInput = addElement(GuiSlurryGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 28, 13));
        output = addElement(GuiSlurryGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 131, 13));
        addSlot(SlotType.POWER, 152, 14).with(SlotOverlay.POWER);
        addSlot(SlotType.OUTPUT, 152, 56).with(SlotOverlay.MINUS);
        addConstantProgress(ProgressType.LARGE_RIGHT, 64, 39);
    }

    @Override
    public Class<? extends FluidSlurryToSlurryRecipe> getRecipeClass() {
        return FluidSlurryToSlurryRecipe.class;
    }

    @Override
    public void setIngredients(FluidSlurryToSlurryRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getFluidInput().getRepresentations()));
        ingredients.setInputLists(MekanismJEI.TYPE_SLURRY, Collections.singletonList(recipe.getChemicalInput().getRepresentations()));
        ingredients.setOutputLists(MekanismJEI.TYPE_SLURRY, Collections.singletonList(recipe.getOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, FluidSlurryToSlurryRecipe recipe, IIngredients ingredients) {
        initFluid(recipeLayout.getFluidStacks(), 0, true, fluidInput, recipe.getFluidInput().getRepresentations());
        IGuiIngredientGroup<SlurryStack> slurryStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_SLURRY);
        initChemical(slurryStacks, 0, true, slurryInput, recipe.getChemicalInput().getRepresentations());
        initChemical(slurryStacks, 1, false, output, recipe.getOutputDefinition());
    }
}