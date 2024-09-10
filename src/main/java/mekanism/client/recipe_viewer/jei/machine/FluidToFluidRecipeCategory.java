package mekanism.client.recipe_viewer.jei.machine;

import java.util.List;
import mekanism.api.heat.HeatAPI;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.jei.HolderRecipeCategory;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class FluidToFluidRecipeCategory extends HolderRecipeCategory<FluidToFluidRecipe> {

    private final GuiGauge<?> input;
    private final GuiGauge<?> output;

    public FluidToFluidRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<FluidToFluidRecipe> recipeType) {
        super(helper, recipeType);
        addElement(new GuiInnerScreen(this, 48, 19, 80, 40, () -> List.of(
              MekanismLang.MULTIBLOCK_FORMED.translate(), MekanismLang.EVAPORATION_HEIGHT.translate(EvaporationMultiblockData.MAX_HEIGHT),
              MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(HeatAPI.AMBIENT_TEMP, TemperatureUnit.KELVIN, true)),
              MekanismLang.FLUID_PRODUCTION.translate(0.0))
        ).clearSpacing());
        addElement(new GuiDownArrow(this, 32, 39));
        addElement(new GuiDownArrow(this, 136, 39));
        addElement(new GuiHorizontalRateBar(this, RecipeViewerUtils.FULL_BAR, 48, 63));
        addSlot(SlotType.INPUT, 28, 20);
        addSlot(SlotType.OUTPUT, 28, 51);
        addSlot(SlotType.INPUT, 132, 20);
        addSlot(SlotType.OUTPUT, 132, 51);
        input = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 6, 13));
        output = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 152, 13));
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RecipeHolder<FluidToFluidRecipe> recipeHolder, @NotNull IFocusGroup focusGroup) {
        FluidToFluidRecipe recipe = recipeHolder.value();
        initFluid(builder, RecipeIngredientRole.INPUT, input, recipe.getInput().getRepresentations());
        initFluid(builder, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDefinition());
    }
}