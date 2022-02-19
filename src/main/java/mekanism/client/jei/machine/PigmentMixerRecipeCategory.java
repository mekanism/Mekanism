package mekanism.client.jei.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.gauge.GuiPigmentGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.JEIColorDetails;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class PigmentMixerRecipeCategory extends BaseRecipeCategory<PigmentMixingRecipe> {

    //Note: We use a weak hashmap so that when the recipe stops existing either due to disconnecting from the server
    // or because of a reload, then it can be properly garbage collected, but until then we keep track of the pairing
    // between the recipe and the ingredient group JEI has so that we can ensure the arrows are the proper color
    private final Map<PigmentMixingRecipe, IGuiIngredientGroup<PigmentStack>> ingredients = new WeakHashMap<>();
    private final PigmentColorDetails leftColorDetails;
    private final PigmentColorDetails rightColorDetails;
    private final GuiGauge<?> leftInputGauge;
    private final GuiGauge<?> rightInputGauge;
    private final GuiGauge<?> outputGauge;

    public PigmentMixerRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.PIGMENT_MIXER, 3, 3, 170, 80);
        leftInputGauge = addElement(GuiPigmentGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT_1), this, 25, 13));
        outputGauge = addElement(GuiPigmentGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 79, 4));
        rightInputGauge = addElement(GuiPigmentGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT_2), this, 133, 13));
        addSlot(SlotType.INPUT, 6, 56).with(SlotOverlay.MINUS);
        addSlot(SlotType.INPUT_2, 154, 56).with(SlotOverlay.MINUS);
        addSlot(SlotType.OUTPUT, 80, 65).with(SlotOverlay.PLUS);
        addSlot(SlotType.POWER, 154, 14).with(SlotOverlay.POWER);
        addConstantProgress(ProgressType.SMALL_RIGHT, 47, 39).colored(leftColorDetails = new PigmentColorDetails());
        addConstantProgress(ProgressType.SMALL_LEFT, 101, 39).colored(rightColorDetails = new PigmentColorDetails());
        addElement(new GuiHorizontalPowerBar(this, FULL_BAR, 115, 75));
    }

    @Override
    public Class<? extends PigmentMixingRecipe> getRecipeClass() {
        return PigmentMixingRecipe.class;
    }

    @Override
    public void draw(PigmentMixingRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        //Set what the "current" recipe is for our color details, before bothering to draw the arrow
        IGuiIngredientGroup<PigmentStack> group = ingredients.get(recipe);
        if (group != null) {
            Map<Integer, ? extends IGuiIngredient<PigmentStack>> guiIngredients = group.getGuiIngredients();
            leftColorDetails.ingredient = guiIngredients.get(0);
            rightColorDetails.ingredient = guiIngredients.get(1);
            leftColorDetails.outputIngredient = guiIngredients.get(2);
            rightColorDetails.outputIngredient = leftColorDetails.outputIngredient;
        }
        super.draw(recipe, matrixStack, mouseX, mouseY);
        leftColorDetails.ingredient = null;
        rightColorDetails.outputIngredient = null;
    }

    @Override
    public void setIngredients(PigmentMixingRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(MekanismJEI.TYPE_PIGMENT, Arrays.asList(recipe.getLeftInput().getRepresentations(), recipe.getRightInput().getRepresentations()));
        ingredients.setOutputLists(MekanismJEI.TYPE_PIGMENT, Collections.singletonList(recipe.getOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, PigmentMixingRecipe recipe, IIngredients ingredients) {
        IGuiIngredientGroup<PigmentStack> pigmentStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_PIGMENT);
        initChemical(pigmentStacks, 0, true, leftInputGauge, recipe.getLeftInput().getRepresentations());
        initChemical(pigmentStacks, 1, true, rightInputGauge, recipe.getRightInput().getRepresentations());
        initChemical(pigmentStacks, 2, false, outputGauge, recipe.getOutputDefinition());
        this.ingredients.put(recipe, pigmentStacks);
    }

    private static class PigmentColorDetails extends JEIColorDetails<Pigment, PigmentStack> {

        @Nullable
        private IGuiIngredient<PigmentStack> ingredient;
        @Nullable
        private IGuiIngredient<PigmentStack> outputIngredient;

        private PigmentColorDetails() {
            super(PigmentStack.EMPTY);
        }

        @Override
        public int getColorFrom() {
            return getColor(ingredient);
        }

        @Override
        public int getColorTo() {
            return getColor(outputIngredient);
        }
    }
}