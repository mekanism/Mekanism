package mekanism.client.jei.machine;

import javax.annotation.Nonnull;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.gauge.GuiSlurryGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;

public class FluidSlurryToSlurryRecipeCategory extends BaseRecipeCategory<FluidSlurryToSlurryRecipe> {

    private final GuiGauge<?> fluidInput;
    private final GuiGauge<?> slurryInput;
    private final GuiGauge<?> output;

    public FluidSlurryToSlurryRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<FluidSlurryToSlurryRecipe> recipeType) {
        super(helper, recipeType, MekanismBlocks.CHEMICAL_WASHER, 7, 13, 162, 60);
        fluidInput = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 7, 13));
        slurryInput = addElement(GuiSlurryGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 28, 13));
        output = addElement(GuiSlurryGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 131, 13));
        addSlot(SlotType.POWER, 152, 14).with(SlotOverlay.POWER);
        addSlot(SlotType.OUTPUT, 152, 56).with(SlotOverlay.MINUS);
        addConstantProgress(ProgressType.LARGE_RIGHT, 64, 39);
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, FluidSlurryToSlurryRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        initFluid(builder, RecipeIngredientRole.INPUT, fluidInput, recipe.getFluidInput().getRepresentations());
        initChemical(builder, MekanismJEI.TYPE_SLURRY, RecipeIngredientRole.INPUT, slurryInput, recipe.getChemicalInput().getRepresentations());
        initChemical(builder, MekanismJEI.TYPE_SLURRY, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDefinition());
    }
}