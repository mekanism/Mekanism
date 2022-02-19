package mekanism.client.jei.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiPigmentGauge;
import mekanism.client.jei.JEIColorDetails;
import mekanism.client.jei.MekanismJEI;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class ItemStackToPigmentRecipeCategory extends ItemStackToChemicalRecipeCategory<Pigment, PigmentStack, ItemStackToPigmentRecipe> {

    //Note: We use a weak hashmap so that when the recipe stops existing either due to disconnecting from the server
    // or because of a reload, then it can be properly garbage collected, but until then we keep track of the pairing
    // between the recipe and the ingredient group JEI has so that we can ensure the arrows are the proper color
    private final Map<ItemStackToPigmentRecipe, IGuiIngredientGroup<PigmentStack>> ingredients = new WeakHashMap<>();
    private final PigmentColorDetails currentDetails;

    public ItemStackToPigmentRecipeCategory(IGuiHelper helper, IItemProvider mekanismBlock) {
        super(helper, mekanismBlock, MekanismJEI.TYPE_PIGMENT, false);
        progressBar.colored(currentDetails = new PigmentColorDetails());
    }

    @Override
    protected GuiPigmentGauge getGauge(GaugeType type, int x, int y) {
        return GuiPigmentGauge.getDummy(type, this, x, y);
    }

    @Override
    public Class<? extends ItemStackToPigmentRecipe> getRecipeClass() {
        return ItemStackToPigmentRecipe.class;
    }

    @Override
    public void draw(ItemStackToPigmentRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        //Set what the "current" recipe is for our color details, before bothering to draw the arrow
        IGuiIngredientGroup<PigmentStack> group = ingredients.get(recipe);
        if (group != null) {
            currentDetails.outputIngredient = group.getGuiIngredients().get(0);
        }
        super.draw(recipe, matrixStack, mouseX, mouseY);
        currentDetails.outputIngredient = null;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ItemStackToPigmentRecipe recipe, IIngredients ingredients) {
        super.setRecipe(recipeLayout, recipe, ingredients);
        this.ingredients.put(recipe, recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_PIGMENT));
    }

    private static class PigmentColorDetails extends JEIColorDetails<Pigment, PigmentStack> {

        @Nullable
        private IGuiIngredient<PigmentStack> outputIngredient;

        private PigmentColorDetails() {
            super(PigmentStack.EMPTY);
        }

        @Override
        public int getColorFrom() {
            return 0xFFFFFFFF;
        }

        @Override
        public int getColorTo() {
            return getColor(outputIngredient);
        }
    }
}