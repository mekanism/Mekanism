package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.color.PaintingColorDetails;
import mekanism.client.recipe_viewer.jei.HolderRecipeCategory;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class PaintingRecipeCategory extends HolderRecipeCategory<PaintingRecipe> {

    private static final String PIGMENT_INPUT = "pigmentInput";

    private final PaintingColorDetails colorDetails;
    private final GuiGauge<?> inputPigment;
    private final GuiSlot inputSlot;
    private final GuiSlot output;

    public PaintingRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<PaintingRecipe> recipeType) {
        super(helper, recipeType);
        inputSlot = addSlot(SlotType.INPUT, 45, 35);
        addSlot(SlotType.POWER, 144, 35).with(SlotOverlay.POWER);
        output = addSlot(SlotType.OUTPUT, 116, 35);
        addElement(new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 15));
        inputPigment = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 25, 13));
        addSimpleProgress(ProgressType.LARGE_RIGHT, 64, 39).colored(colorDetails = new PaintingColorDetails());
    }

    @Override
    public void draw(RecipeHolder<PaintingRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        //Set what the "current" recipe is for our color details, before bothering to draw the arrow
        colorDetails.setIngredient(getDisplayedStack(recipeSlotsView, PIGMENT_INPUT, MekanismJEI.TYPE_CHEMICAL, ChemicalStack.EMPTY));
        super.draw(recipeHolder, recipeSlotsView, guiGraphics, mouseX, mouseY);
        colorDetails.reset();
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RecipeHolder<PaintingRecipe> recipeHolder, @NotNull IFocusGroup focusGroup) {
        PaintingRecipe recipe = recipeHolder.value();
        initItem(builder, RecipeIngredientRole.INPUT, inputSlot, recipe.getItemInput().getRepresentations());
        initChemical(builder, MekanismJEI.TYPE_CHEMICAL, RecipeIngredientRole.INPUT, inputPigment, recipe.getChemicalInput().getRepresentations())
              .setSlotName(PIGMENT_INPUT);
        initItem(builder, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDefinition());
    }
}