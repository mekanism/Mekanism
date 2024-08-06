package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.client.recipe_viewer.recipe.SPSRecipeViewerRecipe;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.Color;
import mekanism.common.lib.Color.ColorFunction;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SPSEmiRecipe extends MekanismEmiRecipe<SPSRecipeViewerRecipe> {

    public SPSEmiRecipe(MekanismEmiRecipeCategory category, ResourceLocation id, SPSRecipeViewerRecipe recipe) {
        super(category, id, recipe);
        addInputDefinition(recipe.input());
        addChemicalOutputDefinition(List.of(recipe.output()));
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        addElement(widgetHolder, new GuiInnerScreen(this, 26, 13, 122, 60, () -> {
            List<Component> list = new ArrayList<>();
            list.add(MekanismLang.STATUS.translate(MekanismLang.ACTIVE));
            list.add(MekanismLang.SPS_ENERGY_INPUT.translate(EnergyDisplay.of(
                  MathUtils.multiplyClamped(MekanismConfig.general.spsEnergyPerInput.get(), MekanismConfig.general.spsInputPerAntimatter.get()))));
            list.add(MekanismLang.PROCESS_RATE_MB.translate(1.0));
            return list;
        }));
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD, this, 6, 13), input(0));
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD, this, 150, 13), output(0)).recipeContext(this);
        addElement(widgetHolder, new GuiDynamicHorizontalRateBar(this, RecipeViewerUtils.barProgressHandler(SharedConstants.TICKS_PER_SECOND), 6, 75, 160,
              ColorFunction.scale(Color.rgbi(60, 45, 74), Color.rgbi(100, 30, 170))));
    }
}