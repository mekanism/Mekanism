package mekanism.client.jei.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge.IEnergyInfoHandler;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.lib.Color;
import mekanism.common.lib.Color.ColorFunction;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class NucleosynthesizingRecipeCategory extends BaseRecipeCategory<NucleosynthesizingRecipe> {

    private final GuiDynamicHorizontalRateBar rateBar;
    private final GuiSlot input;
    private final GuiSlot extra;
    private final GuiSlot output;
    private final GuiGauge<?> gasInput;

    public NucleosynthesizingRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER, 6, 18, 182, 80);
        input = addSlot(SlotType.INPUT, 26, 40);
        extra = addSlot(SlotType.EXTRA, 6, 69);
        output = addSlot(SlotType.OUTPUT, 152, 40);
        addSlot(SlotType.POWER, 173, 69).with(SlotOverlay.POWER);
        addElement(new GuiInnerScreen(this, 45, 18, 104, 68));
        gasInput = addElement(GuiGasGauge.getDummy(GaugeType.SMALL_MED.with(DataType.INPUT), this, 5, 18));
        addElement(new GuiEnergyGauge(new IEnergyInfoHandler() {
            @Override
            public FloatingLong getEnergy() {
                return FloatingLong.ONE;
            }

            @Override
            public FloatingLong getMaxEnergy() {
                return FloatingLong.ONE;
            }
        }, GaugeType.SMALL_MED, this, 172, 18));
        rateBar = addElement(new GuiDynamicHorizontalRateBar(this, getBarProgressTimer(), 5, 88, 183,
              ColorFunction.scale(Color.rgbi(60, 45, 74), Color.rgbi(100, 30, 170))));
    }

    @Override
    public Class<? extends NucleosynthesizingRecipe> getRecipeClass() {
        return NucleosynthesizingRecipe.class;
    }

    @Override
    public List<ITextComponent> getTooltipStrings(NucleosynthesizingRecipe recipe, double mouseX, double mouseY) {
        if (rateBar.isMouseOver(mouseX, mouseY)) {
            return Collections.singletonList(MekanismLang.TICKS_REQUIRED.translate(recipe.getDuration()));
        }
        return Collections.emptyList();
    }

    @Override
    public void setIngredients(NucleosynthesizingRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getItemInput().getRepresentations()));
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getChemicalInput().getRepresentations()));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, NucleosynthesizingRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        initItem(itemStacks, 0, true, input, recipe.getItemInput().getRepresentations());
        initItem(itemStacks, 1, false, output, recipe.getOutputDefinition());
        List<ItemStack> gasItemProviders = new ArrayList<>();
        List<@NonNull GasStack> gasInputs = recipe.getChemicalInput().getRepresentations();
        for (GasStack gas : gasInputs) {
            gasItemProviders.addAll(MekanismJEI.GAS_STACK_HELPER.getStacksFor(gas.getType(), true));
        }
        initItem(itemStacks, 2, true, extra, gasItemProviders);
        initChemical(recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS), 0, true, gasInput, gasInputs);
    }
}