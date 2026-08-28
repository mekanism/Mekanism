package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.display.slot.WithAmountSlotDisplay;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.jei.HolderRecipeCategory;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.machine.TileEntityChemicalDissolutionChamber;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ChemicalDissolutionRecipeCategory extends HolderRecipeCategory<ChemicalDissolutionRecipe> {

    private final GuiGauge<?> inputGauge;
    private final GuiGauge<?> outputGauge;
    private final GuiSlot inputSlot;

    public ChemicalDissolutionRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ChemicalDissolutionRecipe> recipeType) {
        super(helper, recipeType);
        GaugeType type1 = GaugeType.STANDARD.with(DataType.INPUT);
        inputGauge = addElement(GuiChemicalGauge.getDummy(type1, this, 7, 4));
        GaugeType type = GaugeType.STANDARD.with(DataType.OUTPUT);
        outputGauge = addElement(GuiChemicalGauge.getDummy(type, this, 131, 13));
        inputSlot = addSlot(SlotType.INPUT, 28, 36);
        addSlot(SlotType.EXTRA, 8, 65).with(SlotOverlay.MINUS);
        addSlot(SlotType.OUTPUT, 152, 55).with(SlotOverlay.PLUS);
        addSlot(SlotType.POWER, 152, 14).with(SlotOverlay.POWER);
        addSimpleProgress(ProgressType.LARGE_RIGHT, 64, 40);
        addElement(new GuiHorizontalPowerBar(this, RecipeViewerUtils.FULL_BAR, 115, 75));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ChemicalDissolutionRecipe> recipeHolder, IFocusGroup focusGroup) {
        ChemicalDissolutionRecipe recipe = recipeHolder.value();
        initItem(builder, RecipeIngredientRole.INPUT, inputSlot, recipe.getItemInput().display());
        WithAmountSlotDisplay chemicalInput = recipe.getChemicalInput().display();
        if (recipe.perTickUsage()) {
            chemicalInput = chemicalInput.scale(TileEntityChemicalDissolutionChamber.BASE_TICKS_REQUIRED);
        }
        initChemical(builder, RecipeIngredientRole.INPUT, inputGauge, chemicalInput);
        initChemical(builder, RecipeIngredientRole.OUTPUT, outputGauge, recipe.getOutputDisplay());
    }

}