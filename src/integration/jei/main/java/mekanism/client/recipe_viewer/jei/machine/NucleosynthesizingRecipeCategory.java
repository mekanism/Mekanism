package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.display.slot.WithAmountSlotDisplay;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge.IEnergyInfoHandler;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.jei.HolderRecipeCategory;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.lib.Color;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.machine.TileEntityAntiprotonicNucleosynthesizer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.crafting.RecipeHolder;

public class NucleosynthesizingRecipeCategory extends HolderRecipeCategory<NucleosynthesizingRecipe> {

    private final GuiDynamicHorizontalRateBar rateBar;
    private final GuiSlot input;
    private final GuiSlot extra;
    private final GuiSlot output;
    private final GuiGauge<?> inputChemical;

    public NucleosynthesizingRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<NucleosynthesizingRecipe> recipeType) {
        super(helper, recipeType);
        input = addSlot(SlotType.INPUT, 26, 40);
        extra = addSlot(SlotType.EXTRA, 6, 69);
        output = addSlot(SlotType.OUTPUT, 152, 40);
        addSlot(SlotType.POWER, 173, 69).with(SlotOverlay.POWER);
        addElement(new GuiInnerScreen(this, 45, 18, 104, 68));
        GaugeType type = GaugeType.SMALL_MED.with(DataType.INPUT);
        inputChemical = addElement(GuiChemicalGauge.getDummy(type, this, 5, 18));
        addElement(new GuiEnergyGauge(IEnergyInfoHandler.ALWAYS_FULL, GaugeType.SMALL_MED, this, 172, 18));
        rateBar = addElement(new GuiDynamicHorizontalRateBar(this, getBarProgressTimer(), 5, 88, 183,
              Color.rgbi(60, 45, 74), Color.rgbi(100, 30, 170)));
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<NucleosynthesizingRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (rateBar.isMouseOver(mouseX, mouseY)) {
            tooltip.add(MekanismLang.TICKS_REQUIRED.translate(recipeHolder.value().getDuration()));
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<NucleosynthesizingRecipe> recipeHolder, IFocusGroup focusGroup) {
        NucleosynthesizingRecipe recipe = recipeHolder.value();
        initItem(builder, RecipeIngredientRole.INPUT, input, recipe.getItemInput().display());
        WithAmountSlotDisplay chemicalInput = recipe.getChemicalInput().display();
        if (recipe.perTickUsage()) {
            chemicalInput = chemicalInput.scale(TileEntityAntiprotonicNucleosynthesizer.BASE_TICKS_REQUIRED);
        }
        initChemical(builder, RecipeIngredientRole.INPUT, inputChemical, chemicalInput);
        initItem(builder, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDisplay());
        initItem(builder, RecipeIngredientRole.CRAFTING_STATION, extra, RecipeViewerUtils.getStacksFor(recipe.getChemicalInput(), true));
    }
}