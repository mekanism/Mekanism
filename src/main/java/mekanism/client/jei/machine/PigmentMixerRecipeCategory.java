package mekanism.client.jei.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.GuiProgress.ColorDetails;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
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

    public PigmentMixerRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.PIGMENT_MIXER, 3, 3, 170, 80);
        //Add the progress bars. addGuiElements gets called before we would be able to set the color details
        guiElements.add(new GuiProgress(() -> 1, ProgressType.SMALL_RIGHT, this, 47, 39).colored(leftColorDetails = new PigmentColorDetails()));
        guiElements.add(new GuiProgress(() -> 1, ProgressType.SMALL_LEFT, this, 101, 39).colored(rightColorDetails = new PigmentColorDetails()));
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 25, 13));
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 79, 4));
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 133, 13));
        guiElements.add(new GuiSlot(SlotType.POWER, this, 154, 4).with(SlotOverlay.POWER));
        guiElements.add(new GuiSlot(SlotType.INPUT, this, 154, 55).with(SlotOverlay.MINUS));
        guiElements.add(new GuiSlot(SlotType.INPUT, this, 4, 55).with(SlotOverlay.MINUS));
        guiElements.add(new GuiSlot(SlotType.OUTPUT, this, 79, 64).with(SlotOverlay.PLUS));
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
        initChemical(pigmentStacks, 0, true, 26 - xOffset, 14 - yOffset, 16, 58, recipe.getLeftInput().getRepresentations(), true);
        initChemical(pigmentStacks, 1, true, 134 - xOffset, 14 - yOffset, 16, 58, recipe.getRightInput().getRepresentations(), true);
        initChemical(pigmentStacks, 2, false, 80 - xOffset, 5 - yOffset, 16, 58, recipe.getOutputDefinition(), true);
        this.ingredients.put(recipe, pigmentStacks);
    }

    private static class PigmentColorDetails implements ColorDetails {

        @Nullable
        private IGuiIngredient<PigmentStack> ingredient;
        @Nullable
        private IGuiIngredient<PigmentStack> outputIngredient;

        private PigmentStack getCurrent(@Nullable IGuiIngredient<PigmentStack> ingredient) {
            if (ingredient == null) {
                return PigmentStack.EMPTY;
            }
            PigmentStack stack = ingredient.getDisplayedIngredient();
            return stack == null ? PigmentStack.EMPTY : stack;
        }

        @Override
        public int getColorFrom() {
            return getColor(getCurrent(ingredient).getChemicalTint());
        }

        @Override
        public int getColorTo() {
            return getColor(getCurrent(outputIngredient).getChemicalTint());
        }

        private int getColor(int tint) {
            if ((tint & 0xFF000000) == 0) {
                return 0xFF000000 | tint;
            }
            return tint;
        }
    }
}