package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.jei.HolderRecipeCategory;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class FluidSlurryToSlurryRecipeCategory extends HolderRecipeCategory<FluidSlurryToSlurryRecipe> {

    private final GuiGauge<?> fluidInput;
    private final GuiGauge<?> slurryInput;
    private final GuiGauge<?> output;

    public FluidSlurryToSlurryRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<FluidSlurryToSlurryRecipe> recipeType) {
        super(helper, recipeType);
        fluidInput = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 7, 13));
        slurryInput = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 28, 13));
        output = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 131, 13));
        addSlot(SlotType.POWER, 152, 14).with(SlotOverlay.POWER);
        addSlot(SlotType.OUTPUT, 152, 56).with(SlotOverlay.MINUS);
        addConstantProgress(ProgressType.LARGE_RIGHT, 64, 39);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RecipeHolder<FluidSlurryToSlurryRecipe> recipeHolder, @NotNull IFocusGroup focusGroup) {
        FluidSlurryToSlurryRecipe recipe = recipeHolder.value();
        initFluid(builder, RecipeIngredientRole.INPUT, fluidInput, recipe.getFluidInput().getRepresentations());
        initChemical(builder, MekanismJEI.TYPE_CHEMICAL, RecipeIngredientRole.INPUT, slurryInput, recipe.getChemicalInput().getRepresentations());
        initChemical(builder, MekanismJEI.TYPE_CHEMICAL, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDefinition());
    }
}