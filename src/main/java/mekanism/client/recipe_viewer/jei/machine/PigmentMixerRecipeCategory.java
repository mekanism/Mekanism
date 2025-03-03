package mekanism.client.recipe_viewer.jei.machine;

import java.util.function.Supplier;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
import mekanism.client.recipe_viewer.color.PigmentMixerColorDetails;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.RecipeHolder;

public class PigmentMixerRecipeCategory extends ChemicalChemicalToChemicalRecipeCategory {

    private final PigmentMixerColorDetails leftColorDetails;
    private final PigmentMixerColorDetails rightColorDetails;
    private final OutputSupplier outputSupplier = new OutputSupplier();

    public PigmentMixerRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ChemicalChemicalToChemicalRecipe> recipeType) {
        super(helper, recipeType);
        rightArrow.colored(leftColorDetails = new PigmentMixerColorDetails(outputSupplier));
        leftArrow.colored(rightColorDetails = new PigmentMixerColorDetails(outputSupplier));
    }

    @Override
    public void draw(RecipeHolder<ChemicalChemicalToChemicalRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        //Set what the "current" recipe is for our color details, before bothering to draw the arrow
        leftColorDetails.setIngredient(getDisplayedStack(recipeSlotsView, LEFT_INPUT, MekanismJEI.TYPE_CHEMICAL, ChemicalStack.EMPTY));
        rightColorDetails.setIngredient(getDisplayedStack(recipeSlotsView, RIGHT_INPUT, MekanismJEI.TYPE_CHEMICAL, ChemicalStack.EMPTY));
        outputSupplier.output = getDisplayedStack(recipeSlotsView, OUTPUT, MekanismJEI.TYPE_CHEMICAL, ChemicalStack.EMPTY);
        super.draw(recipeHolder, recipeSlotsView, guiGraphics, mouseX, mouseY);
        leftColorDetails.reset();
        rightColorDetails.reset();
        outputSupplier.output = ChemicalStack.EMPTY;
    }

    private static class OutputSupplier implements Supplier<ChemicalStack> {

        private ChemicalStack output = ChemicalStack.EMPTY;

        @Override
        public ChemicalStack get() {
            return output;
        }
    }
}