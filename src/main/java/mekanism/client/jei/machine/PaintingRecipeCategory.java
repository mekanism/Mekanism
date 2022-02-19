package mekanism.client.jei.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
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
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.config.DataType;
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
    private final GuiGauge<?> inputPigment;
    private final GuiSlot inputSlot;
    private final GuiSlot output;

    public PaintingRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.PAINTING_MACHINE, 25, 13, 146, 60);
        inputSlot = addSlot(SlotType.INPUT, 45, 35);
        addSlot(SlotType.POWER, 144, 35).with(SlotOverlay.POWER);
        output = addSlot(SlotType.OUTPUT, 116, 35);
        addElement(new GuiVerticalPowerBar(this, FULL_BAR, 164, 15));
        inputPigment = addElement(GuiPigmentGauge.getDummy(GaugeType.STANDARD.with(DataType.INPUT), this, 25, 13));
        addSimpleProgress(ProgressType.LARGE_RIGHT, 64, 39).colored(colorDetails = new PigmentColorDetails());
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
        initItem(itemStacks, 0, true, inputSlot, recipe.getItemInput().getRepresentations());
        initItem(itemStacks, 1, false, output, recipe.getOutputDefinition());
        IGuiIngredientGroup<PigmentStack> pigmentStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_PIGMENT);
        initChemical(pigmentStacks, 0, true, inputPigment, recipe.getChemicalInput().getRepresentations());
        this.ingredients.put(recipe, pigmentStacks);
    }

    private static class PigmentColorDetails extends JEIColorDetails<Pigment, PigmentStack> {

        @Nullable
        private IGuiIngredient<PigmentStack> ingredient;

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