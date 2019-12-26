/*package mekanism.client.jei.machine;

import java.util.Collections;
import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.util.text.Translation;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class RotaryCondensentratorRecipeCategory extends BaseRecipeCategory<RotaryRecipe> {

    private final boolean condensentrating;
    private final ResourceLocation uid;
    private final String title;

    public RotaryCondensentratorRecipeCategory(IGuiHelper helper, boolean condensentrating) {
        //We override the things that reference the provider
        super(helper, "mekanism:gui/nei/rotary_condensentrator.png", MekanismBlock.ROTARY_CONDENSENTRATOR, null, 3, 12, 170, 71);
        this.condensentrating = condensentrating;
        uid = new ResourceLocation(Mekanism.MODID, condensentrating ? "rotary_condensentrator_condensentrating" : "rotary_condensentrator_decondensentrating");
        this.title = (condensentrating ? MekanismLang.CONDENSENTRATING : MekanismLang.DECONDENSENTRATING).translate().getFormattedText();
    }

    @Override
    public ResourceLocation getUid() {
        return uid;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void draw(RotaryRecipe recipe, double mouseX, double mouseY) {
        super.draw(recipe, mouseX, mouseY);
        drawTexturedRect(64 - xOffset, 39 - yOffset, 176, condensentrating ? 123 : 115, 48, 8);
    }

    @Override
    public Class<? extends RotaryRecipe> getRecipeClass() {
        return RotaryRecipe.class;
    }

    @Override
    public void setIngredients(RotaryRecipe recipe, IIngredients ingredients) {
        if (condensentrating) {
            if (recipe.hasGasToFluid()) {
                ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getGasInput().getRepresentations()));
                ingredients.setOutput(VanillaTypes.FLUID, recipe.getFluidOutputRepresentation());
            }
        } else if (recipe.hasFluidToGas()) {
            ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getFluidInput().getRepresentations()));
            ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.getGasOutputRepresentation());
        }
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, RotaryRecipe recipe, IIngredients ingredients) {
        if (condensentrating) {
            if (recipe.hasGasToFluid()) {
                //Setup gas
                IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
                initGas(gasStacks, 0, true, 26 - xOffset, 14 - yOffset, 16, 58, recipe.getGasInput().getRepresentations(), true);
                //Setup fluid
                IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
                fluidStacks.init(0, true, 134 - xOffset, 14 - yOffset, 16, 58, recipe.getFluidOutputRepresentation().getAmount(), false, fluidOverlayLarge);
                fluidStacks.set(0, recipe.getFluidOutputRepresentation());
            }
        } else if (recipe.hasFluidToGas()) {
            //Setup fluid
            IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
            @NonNull List<FluidStack> fluidInputs = recipe.getFluidInput().getRepresentations();
            int max = fluidInputs.stream().mapToInt(FluidStack::getAmount).filter(input -> input >= 0).max().orElse(0);
            fluidStacks.init(0, false, 134 - xOffset, 14 - yOffset, 16, 58, max, false, fluidOverlayLarge);
            fluidStacks.set(0, fluidInputs);
            //Setup gas
            IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
            initGas(gasStacks, 0, false, 26 - xOffset, 14 - yOffset, 16, 58, recipe.getGasOutputRepresentation(), true);
            gasStacks.set(0, recipe.getGasOutputRepresentation());
        }
    }
}*/