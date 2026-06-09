package mekanism.client.recipe_viewer.jei.machine;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe.PressurizedReactionRecipeOutput;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.jei.HolderRecipeCategory;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeHolder;

public class PressurizedReactionRecipeCategory extends HolderRecipeCategory<PressurizedReactionRecipe> {

    private static final String OUTPUT_CHEMICAL = "outputChemical";

    private final GuiGauge<?> inputChemical;
    private final GuiGauge<?> inputFluid;
    private final GuiSlot inputItem;
    private final GuiSlot outputItem;
    private final GuiGauge<?> outputChemical;

    public PressurizedReactionRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<PressurizedReactionRecipe> recipeType) {
        super(helper, recipeType);
        inputItem = addSlot(SlotType.INPUT, 54, 40);
        outputItem = addSlot(SlotType.OUTPUT, 116, 40);
        addSlot(SlotType.POWER, 141, 22).with(SlotOverlay.POWER);
        inputFluid = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 5, 15));
        GaugeType type1 = GaugeType.STANDARD.with(DataType.INPUT);
        inputChemical = addElement(GuiChemicalGauge.getDummy(type1, this, 28, 15));
        GaugeType type = GaugeType.SMALL.with(DataType.OUTPUT);
        outputChemical = addElement(GuiChemicalGauge.getDummy(type, this, 140, 45));
        addElement(new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 21));
        addSimpleProgress(ProgressType.RIGHT, 77, 43);
    }

    @Override
    protected void renderElements(RecipeHolder<PressurizedReactionRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, int x, int y) {
        super.renderElements(recipeHolder, recipeSlotsView, guiGraphics, x, y);
        if (recipeSlotsView.findSlotByName(OUTPUT_CHEMICAL).isEmpty()) {
            //If we don't have an output chemical at all for this recipe, draw the bar overlay manually
            outputChemical.drawBarOverlay(guiGraphics);
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<PressurizedReactionRecipe> recipeHolder, IFocusGroup focusGroup) {
        PressurizedReactionRecipe recipe = recipeHolder.value();
        ContextMap slotDisplayContext = getSlotDisplayContext();
        initItem(builder, RecipeIngredientRole.INPUT, inputItem, recipe.getInputSolid().getRepresentations(slotDisplayContext));
        initFluid(builder, RecipeIngredientRole.INPUT, inputFluid, recipe.getInputFluid().getRepresentations(slotDisplayContext));
        initChemical(builder, RecipeIngredientRole.INPUT, inputChemical, recipe.getInputChemical().getRepresentations(slotDisplayContext));
        List<ItemStackTemplate> itemOutputs = new ArrayList<>();
        List<ChemicalStackTemplate> chemicalOutputs = new ArrayList<>();
        for (PressurizedReactionRecipeOutput output : recipe.getOutputDefinition()) {
            if (output.item() != null) {
                itemOutputs.add(output.item());
            }
            if (output.chemical() != null) {
                chemicalOutputs.add(output.chemical());
            }
        }
        if (!itemOutputs.isEmpty()) {
            initItem(builder, outputItem, itemOutputs);
        }
        if (!chemicalOutputs.isEmpty()) {
            initChemical(builder, outputChemical, chemicalOutputs)
                  .setSlotName(OUTPUT_CHEMICAL);
        }
    }
}