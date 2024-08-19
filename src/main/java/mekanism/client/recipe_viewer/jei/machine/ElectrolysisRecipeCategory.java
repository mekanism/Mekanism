package mekanism.client.recipe_viewer.jei.machine;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.ElectrolysisRecipe.ElectrolysisRecipeOutput;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
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

public class ElectrolysisRecipeCategory extends HolderRecipeCategory<ElectrolysisRecipe> {

    private final GuiGauge<?> input;
    private final GuiGauge<?> leftOutput;
    private final GuiGauge<?> rightOutput;

    public ElectrolysisRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ElectrolysisRecipe> recipeType) {
        super(helper, recipeType);
        input = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 5, 10));
        GaugeType type1 = GaugeType.SMALL.with(DataType.OUTPUT_1);
        leftOutput = addElement(GuiChemicalGauge.getDummy(type1, this, 58, 18));
        GaugeType type = GaugeType.SMALL.with(DataType.OUTPUT_2);
        rightOutput = addElement(GuiChemicalGauge.getDummy(type, this, 100, 18));
        addElement(new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 15));
        addSlot(SlotType.INPUT, 26, 35);
        addSlot(SlotType.OUTPUT, 59, 52);
        addSlot(SlotType.OUTPUT_2, 101, 52);
        addSlot(SlotType.POWER, 143, 35).with(SlotOverlay.POWER);
        addConstantProgress(ProgressType.BI, 80, 30);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RecipeHolder<ElectrolysisRecipe> recipeHolder, @NotNull IFocusGroup focusGroup) {
        ElectrolysisRecipe recipe = recipeHolder.value();
        initFluid(builder, RecipeIngredientRole.INPUT, input, recipe.getInput().getRepresentations());
        List<ChemicalStack> leftDefinition = new ArrayList<>();
        List<ChemicalStack> rightDefinition = new ArrayList<>();
        for (ElectrolysisRecipeOutput output : recipe.getOutputDefinition()) {
            leftDefinition.add(output.left());
            rightDefinition.add(output.right());
        }
        initChemical(builder, RecipeIngredientRole.OUTPUT, leftOutput, leftDefinition);
        initChemical(builder, RecipeIngredientRole.OUTPUT, rightOutput, rightDefinition);
    }
}