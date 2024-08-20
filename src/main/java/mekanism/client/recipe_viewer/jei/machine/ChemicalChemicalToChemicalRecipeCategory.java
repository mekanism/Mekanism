package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.GuiProgress;
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

public class ChemicalChemicalToChemicalRecipeCategory extends HolderRecipeCategory<ChemicalChemicalToChemicalRecipe> {

    protected static final String LEFT_INPUT = "leftInput";
    protected static final String RIGHT_INPUT = "rightInput";
    protected static final String OUTPUT = "output";

    private final GuiGauge<?> leftInputGauge;
    private final GuiGauge<?> rightInputGauge;
    private final GuiGauge<?> outputGauge;
    protected final GuiProgress rightArrow;
    protected final GuiProgress leftArrow;

    public ChemicalChemicalToChemicalRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ChemicalChemicalToChemicalRecipe> recipeType) {
        super(helper, recipeType);
        leftInputGauge = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT_1), this, 25, 13));
        outputGauge = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 79, 4));
        rightInputGauge = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT_2), this, 133, 13));
        addSlot(SlotType.INPUT, 6, 56).with(SlotOverlay.MINUS);
        addSlot(SlotType.INPUT_2, 154, 56).with(SlotOverlay.MINUS);
        addSlot(SlotType.OUTPUT, 80, 65).with(SlotOverlay.PLUS);
        addSlot(SlotType.POWER, 154, 14).with(SlotOverlay.POWER);
        rightArrow = addConstantProgress(ProgressType.SMALL_RIGHT, 47, 39);
        leftArrow = addConstantProgress(ProgressType.SMALL_LEFT, 101, 39);
        addElement(new GuiHorizontalPowerBar(this, RecipeViewerUtils.FULL_BAR, 115, 75));
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RecipeHolder<ChemicalChemicalToChemicalRecipe> recipeHolder, @NotNull IFocusGroup focusGroup) {
        ChemicalChemicalToChemicalRecipe recipe = recipeHolder.value();
        builder.setShapeless();
        initChemical(builder, RecipeIngredientRole.INPUT, leftInputGauge, recipe.getLeftInput().getRepresentations())
              .setSlotName(LEFT_INPUT);
        initChemical(builder, RecipeIngredientRole.INPUT, rightInputGauge, recipe.getRightInput().getRepresentations())
              .setSlotName(RIGHT_INPUT);
        initChemical(builder, RecipeIngredientRole.OUTPUT, outputGauge, recipe.getOutputDefinition())
              .setSlotName(OUTPUT);
    }
}