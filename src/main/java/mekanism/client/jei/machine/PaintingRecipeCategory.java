package mekanism.client.jei.machine;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.gauge.GuiPigmentGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.JEIColorDetails;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;

public class PaintingRecipeCategory extends BaseRecipeCategory<PaintingRecipe> {

    private static final String PIGMENT_INPUT = "pigmentInput";

    private final PigmentColorDetails colorDetails;
    private final GuiGauge<?> inputPigment;
    private final GuiSlot inputSlot;
    private final GuiSlot output;

    public PaintingRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<PaintingRecipe> recipeType) {
        super(helper, recipeType, MekanismBlocks.PAINTING_MACHINE, 25, 13, 146, 60);
        inputSlot = addSlot(SlotType.INPUT, 45, 35);
        addSlot(SlotType.POWER, 144, 35).with(SlotOverlay.POWER);
        output = addSlot(SlotType.OUTPUT, 116, 35);
        addElement(new GuiVerticalPowerBar(this, FULL_BAR, 164, 15));
        inputPigment = addElement(GuiPigmentGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 25, 13));
        addSimpleProgress(ProgressType.LARGE_RIGHT, 64, 39).colored(colorDetails = new PigmentColorDetails());
    }

    @Override
    public void draw(PaintingRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack matrixStack, double mouseX, double mouseY) {
        //Set what the "current" recipe is for our color details, before bothering to draw the arrow
        colorDetails.ingredient = getDisplayedStack(recipeSlotsView, PIGMENT_INPUT, MekanismJEI.TYPE_PIGMENT, PigmentStack.EMPTY);
        super.draw(recipe, recipeSlotsView, matrixStack, mouseX, mouseY);
        colorDetails.reset();
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, PaintingRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        initItem(builder, RecipeIngredientRole.INPUT, inputSlot, recipe.getItemInput().getRepresentations());
        initChemical(builder, MekanismJEI.TYPE_PIGMENT, RecipeIngredientRole.INPUT, inputPigment, recipe.getChemicalInput().getRepresentations())
              .setSlotName(PIGMENT_INPUT);
        initItem(builder, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDefinition());
    }

    private static class PigmentColorDetails extends JEIColorDetails<Pigment, PigmentStack> {

        private PigmentColorDetails() {
            super(PigmentStack.EMPTY);
        }

        @Override
        public int getColorFrom() {
            return getColor(ingredient);
        }

        @Override
        public int getColorTo() {
            return 0xFFFFFFFF;
        }
    }
}