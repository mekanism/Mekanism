package mekanism.client.jei.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
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
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.lib.Color;
import mekanism.common.lib.Color.ColorFunction;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.config.DataType;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class NucleosynthesizingRecipeCategory extends BaseRecipeCategory<NucleosynthesizingRecipe> {

    private final GuiDynamicHorizontalRateBar rateBar;
    private final GuiSlot input;
    private final GuiSlot extra;
    private final GuiSlot output;
    private final GuiGauge<?> gasInput;

    public NucleosynthesizingRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<NucleosynthesizingRecipe> recipeType) {
        super(helper, recipeType, MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER, 6, 18, 182, 80);
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
    public List<Component> getTooltipStrings(NucleosynthesizingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (rateBar.isMouseOver(mouseX, mouseY)) {
            return Collections.singletonList(MekanismLang.TICKS_REQUIRED.translate(recipe.getDuration()));
        }
        return Collections.emptyList();
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, NucleosynthesizingRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        initItem(builder, RecipeIngredientRole.INPUT, input, recipe.getItemInput().getRepresentations());
        List<@NonNull GasStack> gasInputs = recipe.getChemicalInput().getRepresentations();
        initChemical(builder, MekanismJEI.TYPE_GAS, RecipeIngredientRole.INPUT, gasInput, gasInputs);
        initItem(builder, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDefinition());
        List<ItemStack> gasItemProviders = new ArrayList<>();
        for (GasStack gas : gasInputs) {
            gasItemProviders.addAll(MekanismJEI.GAS_STACK_HELPER.getStacksFor(gas.getType(), true));
        }
        initItem(builder, RecipeIngredientRole.CATALYST, extra, gasItemProviders);
    }
}