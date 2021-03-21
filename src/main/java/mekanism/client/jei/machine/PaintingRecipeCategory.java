package mekanism.client.jei.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiPigmentGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.GuiProgress.ColorDetails;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class PaintingRecipeCategory extends BaseRecipeCategory<PaintingRecipe> {

    //Note: We use a weak hashmap so that when the recipe stops existing either due to disconnecting from the server
    // or because of a reload, then it can be properly garbage collected, but until then we keep track of the pairing
    // between the recipe and the ingredient group JEI has so that we can ensure the arrows are the proper color
    private final Map<PaintingRecipe, IGuiIngredientGroup<PigmentStack>> ingredients = new WeakHashMap<>();
    private final PigmentColorDetails colorDetails;

    public PaintingRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.PAINTING_MACHINE, 5, 10, 166, 64);
        //Add the progress bar. addGuiElements gets called before we would be able to set the color details
        guiElements.add(new GuiProgress(() -> timer.getValue() / 20D, ProgressType.LARGE_RIGHT, this, 64, 40).colored(colorDetails = new PigmentColorDetails()));
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(new GuiSlot(SlotType.INPUT, this, 45, 35));
        guiElements.add(new GuiSlot(SlotType.POWER, this, 142, 35).with(SlotOverlay.POWER));
        guiElements.add(new GuiSlot(SlotType.OUTPUT, this, 116, 35));
        guiElements.add(new GuiVerticalPowerBar(this, () -> 1F, 164, 15));
        guiElements.add(GuiPigmentGauge.getDummy(GaugeType.STANDARD, this, 25, 13));
    }

    @Override
    public Class<? extends PaintingRecipe> getRecipeClass() {
        return PaintingRecipe.class;
    }

    @Override
    public void draw(PaintingRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        //Set what the "current" recipe is for our color details, before bothering to draw the arrow
        IGuiIngredientGroup<PigmentStack> group = ingredients.get(recipe);
        if (group != null) {
            colorDetails.ingredient = group.getGuiIngredients().get(0);
        }
        super.draw(recipe, matrixStack, mouseX, mouseY);
        colorDetails.ingredient = null;
    }

    @Override
    public void setIngredients(PaintingRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getItemInput().getRepresentations()));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
        ingredients.setInputLists(MekanismJEI.TYPE_PIGMENT, Collections.singletonList(recipe.getChemicalInput().getRepresentations()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, PaintingRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 45 - xOffset, 35 - yOffset);
        itemStacks.init(1, false, 116 - xOffset, 35 - yOffset);
        itemStacks.set(0, recipe.getItemInput().getRepresentations());
        itemStacks.set(1, recipe.getOutputDefinition());
        IGuiIngredientGroup<PigmentStack> pigmentStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_PIGMENT);
        initChemical(pigmentStacks, 0, true, 26 - xOffset, 14 - yOffset, 16, 58, recipe.getChemicalInput().getRepresentations(), true);
        this.ingredients.put(recipe, pigmentStacks);
    }

    private static class PigmentColorDetails implements ColorDetails {

        @Nullable
        private IGuiIngredient<PigmentStack> ingredient;

        @Override
        public int getColorFrom() {
            if (ingredient != null) {
                PigmentStack stack = ingredient.getDisplayedIngredient();
                if (stack != null) {
                    int tint = stack.getChemicalTint();
                    if ((tint & 0xFF000000) == 0) {
                        return 0xFF000000 | tint;
                    }
                    return tint;
                }
            }
            return 0xFFFFFFFF;
        }

        @Override
        public int getColorTo() {
            return 0xFFFFFFFF;
        }
    }
}