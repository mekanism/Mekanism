package mekanism.client.jei.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nullable;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiPigmentGauge;
import mekanism.client.gui.element.progress.GuiProgress.ColorDetails;
import mekanism.client.jei.MekanismJEI;
import mezz.jei.api.helpers.IGuiHelper;

public class ItemStackToPigmentRecipeCategory extends ItemStackToChemicalRecipeCategory<Pigment, PigmentStack, ItemStackToPigmentRecipe> {

    private final PigmentColorDetails currentDetails;

    public ItemStackToPigmentRecipeCategory(IGuiHelper helper, IBlockProvider mekanismBlock) {
        super(helper, mekanismBlock.getRegistryName(), mekanismBlock.getTextComponent(), MekanismJEI.TYPE_PIGMENT, false);
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
        currentDetails.currentRecipe = recipe;
        super.draw(recipe, matrixStack, mouseX, mouseY);
        currentDetails.currentRecipe = null;
    }

    private static class PigmentColorDetails implements ColorDetails {

        @Nullable
        private ItemStackToPigmentRecipe currentRecipe;

        @Override
        public int getColorFrom() {
            return 0xFFFFFFFF;
        }

        @Override
        public int getColorTo() {
            if (currentRecipe == null) {
                return 0xFFFFFFFF;
            }
            int tint = currentRecipe.getOutputDefinition().getChemicalTint();
            if ((tint & 0xFF000000) == 0) {
                return 0xFF000000 | tint;
            }
            return tint;
        }
    }
}