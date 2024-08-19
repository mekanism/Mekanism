package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.jei.HolderRecipeCategory;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class FluidChemicalToChemicalRecipeCategory extends HolderRecipeCategory<FluidChemicalToChemicalRecipe> {

    private final GuiGauge<?> fluidInput;
    private final GuiGauge<?> chemicalInput;
    private final GuiGauge<?> output;

    public FluidChemicalToChemicalRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<FluidChemicalToChemicalRecipe> recipeType) {
        super(helper, recipeType);
        fluidInput = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 7, 13));
        chemicalInput = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 28, 13));
        output = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 131, 13));
        addSlot(SlotType.POWER, 152, 14).with(SlotOverlay.POWER);
        addSlot(SlotType.OUTPUT, 152, 56).with(SlotOverlay.MINUS);
        addConstantProgress(ProgressType.LARGE_RIGHT, 64, 39);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RecipeHolder<FluidChemicalToChemicalRecipe> recipeHolder, @NotNull IFocusGroup focusGroup) {
        FluidChemicalToChemicalRecipe recipe = recipeHolder.value();
        initFluid(builder, RecipeIngredientRole.INPUT, fluidInput, recipe.getFluidInput().getRepresentations());
        initChemical(builder, RecipeIngredientRole.INPUT, chemicalInput, recipe.getChemicalInput().getRepresentations());
        initChemical(builder, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDefinition());
    }
}