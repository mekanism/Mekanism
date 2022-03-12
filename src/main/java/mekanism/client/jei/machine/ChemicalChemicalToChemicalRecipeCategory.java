package mekanism.client.jei.machine;

import javax.annotation.Nonnull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;

public abstract class ChemicalChemicalToChemicalRecipeCategory<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      RECIPE extends ChemicalChemicalToChemicalRecipe<CHEMICAL, STACK, ?>> extends BaseRecipeCategory<RECIPE> {

    protected static final String LEFT_INPUT = "leftInput";
    protected static final String RIGHT_INPUT = "rightInput";
    protected static final String OUTPUT = "output";

    private final IIngredientType<STACK> ingredientType;
    private final GuiGauge<?> leftInputGauge;
    private final GuiGauge<?> rightInputGauge;
    private final GuiGauge<?> outputGauge;
    protected final GuiProgress rightArrow;
    protected final GuiProgress leftArrow;

    protected ChemicalChemicalToChemicalRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<RECIPE> recipeType, IItemProvider provider,
          IIngredientType<STACK> ingredientType, int xOffset, int yOffset, int width, int height) {
        super(helper, recipeType, provider, xOffset, yOffset, width, height);
        this.ingredientType = ingredientType;
        leftInputGauge = addElement(getGauge(GaugeType.STANDARD.with(DataType.INPUT_1), 25, 13));
        outputGauge = addElement(getGauge(GaugeType.STANDARD.with(DataType.OUTPUT), 79, 4));
        rightInputGauge = addElement(getGauge(GaugeType.STANDARD.with(DataType.INPUT_2), 133, 13));
        addSlot(SlotType.INPUT, 6, 56).with(SlotOverlay.MINUS);
        addSlot(SlotType.INPUT_2, 154, 56).with(SlotOverlay.MINUS);
        addSlot(SlotType.OUTPUT, 80, 65).with(SlotOverlay.PLUS);
        addSlot(SlotType.POWER, 154, 14).with(SlotOverlay.POWER);
        rightArrow = addConstantProgress(ProgressType.SMALL_RIGHT, 47, 39);
        leftArrow = addConstantProgress(ProgressType.SMALL_LEFT, 101, 39);
        addElement(new GuiHorizontalPowerBar(this, FULL_BAR, 115, 75));
    }

    protected abstract GuiChemicalGauge<CHEMICAL, STACK, ?> getGauge(GaugeType type, int x, int y);

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, RECIPE recipe, @Nonnull IFocusGroup focusGroup) {
        builder.setShapeless();
        initChemical(builder, ingredientType, RecipeIngredientRole.INPUT, leftInputGauge, recipe.getLeftInput().getRepresentations())
              .setSlotName(LEFT_INPUT);
        initChemical(builder, ingredientType, RecipeIngredientRole.INPUT, rightInputGauge, recipe.getRightInput().getRepresentations())
              .setSlotName(RIGHT_INPUT);
        initChemical(builder, ingredientType, RecipeIngredientRole.OUTPUT, outputGauge, recipe.getOutputDefinition())
              .setSlotName(OUTPUT);
    }
}