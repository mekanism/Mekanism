package mekanism.generators.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import mekanism.api.heat.HeatAPI;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.client.recipe_viewer.emi.recipe.MekanismEmiRecipe;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.BooleanStateDisplay.ActiveDisabled;
import mekanism.common.util.text.TextUtils;
import mekanism.generators.client.recipe_viewer.recipe.FissionRecipeViewerRecipe;
import mekanism.generators.common.GeneratorsLang;
import net.minecraft.resources.ResourceLocation;

public class FissionReactorEmiRecipe extends MekanismEmiRecipe<FissionRecipeViewerRecipe> {

    public FissionReactorEmiRecipe(MekanismEmiRecipeCategory category, ResourceLocation id, FissionRecipeViewerRecipe recipe) {
        super(category, id, recipe);
        if (recipe.inputCoolant() == null) {
            addInputDefinition(recipe.waterInput());
        } else {
            addInputDefinition(recipe.inputCoolant());
        }
        addInputDefinition(recipe.fuel());
        addChemicalOutputDefinition(List.of(recipe.outputCoolant()));
        addChemicalOutputDefinition(List.of(recipe.waste()));
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        addElement(widgetHolder, new GuiInnerScreen(this, 45, 17, 105, 56, () -> List.of(
              MekanismLang.STATUS.translate(EnumColor.BRIGHT_GREEN, ActiveDisabled.of(true)),
              GeneratorsLang.GAS_BURN_RATE.translate(1.0),
              GeneratorsLang.FISSION_HEATING_RATE.translate(0),
              MekanismLang.TEMPERATURE.translate(EnumColor.BRIGHT_GREEN, MekanismUtils.getTemperatureDisplay(HeatAPI.AMBIENT_TEMP, TemperatureUnit.KELVIN, true)),
              GeneratorsLang.FISSION_DAMAGE.translate(EnumColor.BRIGHT_GREEN, TextUtils.getPercent(0))
        )).spacing(1));
        initTank(widgetHolder, GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 6, 13).setLabel(GeneratorsLang.FISSION_COOLANT_TANK.translateColored(EnumColor.AQUA)), input(0));
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD, this, 25, 13).setLabel(GeneratorsLang.FISSION_FUEL_TANK.translateColored(EnumColor.DARK_GREEN)), input(1));
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD, this, 152, 13).setLabel(GeneratorsLang.FISSION_HEATED_COOLANT_TANK.translateColored(EnumColor.GRAY)), output(0)).recipeContext(this);
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD, this, 171, 13).setLabel(GeneratorsLang.FISSION_WASTE_TANK.translateColored(EnumColor.BROWN)), output(1)).recipeContext(this);
    }
}