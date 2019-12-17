/*package mekanism.client.jei.machine;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.MekanismBlock;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class ItemStackGasToGasRecipeCategory extends BaseRecipeCategory<ItemStackGasToGasRecipe> {

    public ItemStackGasToGasRecipeCategory(IGuiHelper helper) {
        //TODO: previously had a lang entry for a shorter path
        super(helper, "mekanism:gui/nei/chemical_dissolution_chamber.png", MekanismBlock.CHEMICAL_DISSOLUTION_CHAMBER, null, 3, 3, 170, 79);
    }

    @Override
    public void draw(ItemStackGasToGasRecipe recipe, double mouseX, double mouseY) {
        super.draw(recipe, mouseX, mouseY);
        drawTexturedRect(64 - xOffset, 40 - yOffset, 176, 63, (int) (48 * ((float) timer.getValue() / 20F)), 8);
    }

    @Override
    public Class<? extends ItemStackGasToGasRecipe> getRecipeClass() {
        return ItemStackGasToGasRecipe.class;
    }

    @Override
    public void setIngredients(ItemStackGasToGasRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getItemInput().getRepresentations()));
        @NonNull List<@NonNull GasStack> gasInputs = recipe.getGasInput().getRepresentations();
        //TODO: Should this be "generalized" to some values that are not stored in the chemical dissolution chamber class
        int scale = TileEntityChemicalDissolutionChamber.BASE_INJECT_USAGE * TileEntityChemicalDissolutionChamber.BASE_TICKS_REQUIRED;
        List<GasStack> scaledGases = gasInputs.stream().map(gas -> new GasStack(gas, scale)).collect(Collectors.toList());
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(scaledGases));
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.getOutputDefinition());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ItemStackGasToGasRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 25 - xOffset, 35 - yOffset);
        itemStacks.set(0, recipe.getItemInput().getRepresentations());
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        @NonNull List<@NonNull GasStack> gasInputs = recipe.getGasInput().getRepresentations();
        //TODO: Should this be "generalized" to some values that are not stored in the chemical dissolution chamber class
        int scale = TileEntityChemicalDissolutionChamber.BASE_INJECT_USAGE * TileEntityChemicalDissolutionChamber.BASE_TICKS_REQUIRED;
        List<GasStack> scaledGases = gasInputs.stream().map(gas -> new GasStack(gas, scale)).collect(Collectors.toList());
        initGas(gasStacks, 0, true, 6 - xOffset, 5 - yOffset, 16, 58, scaledGases, true);
        initGas(gasStacks, 1, false, 134 - xOffset, 14 - yOffset, 16, 58, recipe.getOutputDefinition(), true);
    }
}*/