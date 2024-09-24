package mekanism.client.recipe_viewer.jei.machine;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.functions.ConstantPredicates;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

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
    protected void renderElements(RecipeHolder<PressurizedReactionRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, int x, int y) {
        super.renderElements(recipeHolder, recipeSlotsView, guiGraphics, x, y);
        if (recipeSlotsView.findSlotByName(OUTPUT_CHEMICAL).isEmpty()) {
            //If we don't have an output chemical at all for this recipe, draw the bar overlay manually
            outputChemical.drawBarOverlay(guiGraphics);
        }
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RecipeHolder<PressurizedReactionRecipe> recipeHolder, @NotNull IFocusGroup focusGroup) {
        PressurizedReactionRecipe recipe = recipeHolder.value();
        initItem(builder, RecipeIngredientRole.INPUT, inputItem, recipe.getInputSolid().getRepresentations());
        initFluid(builder, RecipeIngredientRole.INPUT, inputFluid, recipe.getInputFluid().getRepresentations());
        initChemical(builder, RecipeIngredientRole.INPUT, inputChemical, recipe.getInputChemical().getRepresentations());
        List<ItemStack> itemOutputs = new ArrayList<>();
        List<ChemicalStack> chemicalOutputs = new ArrayList<>();
        for (PressurizedReactionRecipeOutput output : recipe.getOutputDefinition()) {
            itemOutputs.add(output.item());
            chemicalOutputs.add(output.chemical());
        }
        if (!itemOutputs.stream().allMatch(ConstantPredicates.ITEM_EMPTY)) {
            initItem(builder, RecipeIngredientRole.OUTPUT, outputItem, itemOutputs);
        }
        if (!chemicalOutputs.stream().allMatch(ConstantPredicates.CHEMICAL_EMPTY)) {
            initChemical(builder, RecipeIngredientRole.OUTPUT, outputChemical, chemicalOutputs)
                  .setSlotName(OUTPUT_CHEMICAL);
        }
    }
}