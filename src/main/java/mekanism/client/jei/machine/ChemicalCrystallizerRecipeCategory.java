package mekanism.client.jei.machine;

import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.api.recipes.inputs.chemical.SlurryStackIngredient;
import mekanism.client.gui.element.custom.GuiCrystallizerScreen;
import mekanism.client.gui.element.custom.GuiCrystallizerScreen.IOreInfo;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;

public class ChemicalCrystallizerRecipeCategory extends BaseRecipeCategory<ChemicalCrystallizerRecipe> {

    public ChemicalCrystallizerRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.CHEMICAL_CRYSTALLIZER, 5, 3, 147, 79);
    }

    @Override
    protected void addGuiElements() {
        //TODO: Eventually make this be able to display the proper ores cycling at some point
        guiElements.add(new GuiCrystallizerScreen(this, 31, 13, new IOreInfo() {
            @Nonnull
            @Override
            public BoxedChemicalStack getInputChemical() {
                return BoxedChemicalStack.EMPTY;
            }

            @Nullable
            @Override
            public ChemicalCrystallizerRecipe getRecipe() {
                return null;
            }
        }));
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 7, 4));
        guiElements.add(new GuiSlot(SlotType.EXTRA, this, 7, 64).with(SlotOverlay.PLUS));
        guiElements.add(new GuiSlot(SlotType.OUTPUT, this, 128, 56));
        guiElements.add(new GuiProgress(() -> timer.getValue() / 20D, ProgressType.LARGE_RIGHT, this, 53, 61));
    }

    @Override
    public Class<? extends ChemicalCrystallizerRecipe> getRecipeClass() {
        return ChemicalCrystallizerRecipe.class;
    }

    @Override
    public void setIngredients(ChemicalCrystallizerRecipe recipe, IIngredients ingredients) {
        IChemicalStackIngredient<?, ?> input = recipe.getInput();
        if (input instanceof GasStackIngredient) {
            ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(((GasStackIngredient) input).getRepresentations()));
        } else if (input instanceof InfusionStackIngredient) {
            ingredients.setInputLists(MekanismJEI.TYPE_INFUSION, Collections.singletonList(((InfusionStackIngredient) input).getRepresentations()));
        } else if (input instanceof PigmentStackIngredient) {
            ingredients.setInputLists(MekanismJEI.TYPE_PIGMENT, Collections.singletonList(((PigmentStackIngredient) input).getRepresentations()));
        } else if (input instanceof SlurryStackIngredient) {
            ingredients.setInputLists(MekanismJEI.TYPE_SLURRY, Collections.singletonList(((SlurryStackIngredient) input).getRepresentations()));
        }
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ChemicalCrystallizerRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, false, 128 - xOffset, 56 - yOffset);
        itemStacks.set(0, recipe.getOutputDefinition());
        IChemicalStackIngredient<?, ?> input = recipe.getInput();
        if (input instanceof GasStackIngredient) {
            initChemical(recipeLayout, MekanismJEI.TYPE_GAS, (GasStackIngredient) input);
        } else if (input instanceof InfusionStackIngredient) {
            initChemical(recipeLayout, MekanismJEI.TYPE_INFUSION, (InfusionStackIngredient) input);
        } else if (input instanceof PigmentStackIngredient) {
            initChemical(recipeLayout, MekanismJEI.TYPE_PIGMENT, (PigmentStackIngredient) input);
        } else if (input instanceof SlurryStackIngredient) {
            initChemical(recipeLayout, MekanismJEI.TYPE_SLURRY, (SlurryStackIngredient) input);
        }
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void initChemical(IRecipeLayout recipeLayout, IIngredientType<STACK> type,
          IChemicalStackIngredient<CHEMICAL, STACK> ingredient) {
        IGuiIngredientGroup<STACK> stacks = recipeLayout.getIngredientsGroup(type);
        initChemical(stacks, 0, true, 8 - xOffset, 5 - yOffset, 16, 58, ingredient.getRepresentations(), true);
    }
}