package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import java.util.Collections;
import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.client.recipe_viewer.recipe.BoilerRecipeViewerRecipe;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.TextUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;

public class BoilerEmiRecipe extends MekanismEmiRecipe<BoilerRecipeViewerRecipe> {

    public BoilerEmiRecipe(MekanismEmiRecipeCategory category, ResourceLocation id, BoilerRecipeViewerRecipe recipe) {
        super(category, id, recipe);
        addInputDefinition(recipe.water());
        addChemicalOutputDefinition(List.of(recipe.steam()));
        if (recipe.heatedCoolant() == null) {
            addEmptyInput();
            addOutputDefinition(Collections.emptyList());
        } else {
            final InputIngredient<?> heatedCoolant = recipe.heatedCoolant();
            switch (heatedCoolant) {
                case FluidStackIngredient fluid -> addInputDefinition(fluid);
                case ChemicalStackIngredient chemical -> addInputDefinition(chemical);
                default -> throw new IllegalStateException("Bad heated coolant: expected fluid or chemical, got " + heatedCoolant);
            }
            final Object cooledCoolant = recipe.cooledCoolant();
            switch (cooledCoolant) {
                case FluidStack fluid -> addFluidOutputDefinition(List.of(fluid));
                case ChemicalStack chemical -> addChemicalOutputDefinition(List.of(chemical));
                default -> throw new IllegalStateException("Bad cooled coolant: expected fluid or chemical, got " + cooledCoolant);
            }
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        //Note: All these elements except for the heatedCoolantTank and waterTank are in slightly different x positions than in the normal GUI
        // so that they fit properly in emi
        addElement(widgetHolder, new GuiInnerScreen(this, 48, 23, 96, 40, () -> List.of(
              MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(recipe.temperature(), TemperatureUnit.KELVIN, true)),
              MekanismLang.BOIL_RATE.translate(TextUtils.format(MathUtils.clampToInt(recipe.steam().getAmount())))
        )));
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD, this, 6, 13).setLabel(MekanismLang.BOILER_HEATED_COOLANT_TANK.translateColored(EnumColor.ORANGE)), input(1));
        initTank(widgetHolder, GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 26, 13).setLabel(MekanismLang.BOILER_WATER_TANK.translateColored(EnumColor.INDIGO)), input(0));
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD, this, 148, 13).setLabel(MekanismLang.BOILER_STEAM_TANK.translateColored(EnumColor.GRAY)), output(0)).recipeContext(this);
        initTank(widgetHolder, GuiChemicalGauge.getDummy(GaugeType.STANDARD, this, 168, 13).setLabel(MekanismLang.BOILER_COOLANT_TANK.translateColored(EnumColor.AQUA)), output(1)).recipeContext(this);
    }
}