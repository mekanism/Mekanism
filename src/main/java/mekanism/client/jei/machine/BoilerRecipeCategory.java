package mekanism.client.jei.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.attribute.GasAttributes.HeatedCoolant;
import mekanism.api.heat.HeatAPI;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.machine.BoilerRecipeCategory.BoilerJEIRecipe;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.TextUtils;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;

public class BoilerRecipeCategory extends BaseRecipeCategory<BoilerJEIRecipe> {

    private static final ResourceLocation iconRL = MekanismUtils.getResource(ResourceType.GUI, "heat.png");
    private final GuiGauge<?> superHeatedCoolantTank;
    private final GuiGauge<?> waterTank;
    private final GuiGauge<?> steamTank;
    private final GuiGauge<?> cooledCoolantTank;
    @Nullable
    private BoilerJEIRecipe recipe;

    public BoilerRecipeCategory(IGuiHelper helper, ResourceLocation id) {
        super(helper, id, MekanismLang.BOILER.translate(), createIcon(helper, iconRL), 6, 13, 180, 60);
        //Note: All these elements except for the heatedCoolantTank and waterTank are in slightly different x positions than in the normal GUI
        // so that they fit properly in JEI
        addElement(new GuiInnerScreen(this, 48, 23, 96, 40, () -> {
            double temperature;
            int boilRate;
            if (recipe == null) {
                temperature = HeatAPI.AMBIENT_TEMP;
                boilRate = 0;
            } else {
                temperature = recipe.temperature;
                boilRate = MathUtils.clampToInt(recipe.steam.getAmount());
            }
            return Arrays.asList(MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(temperature, TemperatureUnit.KELVIN, true)),
                  MekanismLang.BOIL_RATE.translate(TextUtils.format(boilRate)));
        }
        ));
        superHeatedCoolantTank = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 6, 13).setLabel(MekanismLang.BOILER_HEATED_COOLANT_TANK.translateColored(EnumColor.ORANGE)));
        waterTank = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 26, 13).setLabel(MekanismLang.BOILER_WATER_TANK.translateColored(EnumColor.INDIGO)));
        steamTank = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 148, 13).setLabel(MekanismLang.BOILER_STEAM_TANK.translateColored(EnumColor.GRAY)));
        cooledCoolantTank = addElement(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 168, 13).setLabel(MekanismLang.BOILER_COOLANT_TANK.translateColored(EnumColor.AQUA)));
    }

    @Nonnull
    @Override
    public Class<? extends BoilerJEIRecipe> getRecipeClass() {
        return BoilerJEIRecipe.class;
    }

    @Override
    public void draw(BoilerJEIRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        //Update what the current recipe is so that we have the proper values for temperature and the like
        this.recipe = recipe;
        super.draw(recipe, matrixStack, mouseX, mouseY);
        if (recipe.superHeatedCoolant == null) {
            superHeatedCoolantTank.drawBarOverlay(matrixStack);
            cooledCoolantTank.drawBarOverlay(matrixStack);
        }
        this.recipe = null;
    }

    @Override
    public void setIngredients(BoilerJEIRecipe recipe, @Nonnull IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.water.getRepresentations()));
        if (recipe.superHeatedCoolant == null) {
            ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.steam);
        } else {
            ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.superHeatedCoolant.getRepresentations()));
            ingredients.setOutputs(MekanismJEI.TYPE_GAS, Arrays.asList(recipe.steam, recipe.cooledCoolant));
        }
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, BoilerJEIRecipe recipe, @Nonnull IIngredients ingredients) {
        initFluid(recipeLayout.getFluidStacks(), 0, true, waterTank, recipe.water.getRepresentations());
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        if (recipe.superHeatedCoolant == null) {
            initChemical(gasStacks, 0, false, steamTank, Collections.singletonList(recipe.steam));
        } else {
            initChemical(gasStacks, 0, true, superHeatedCoolantTank, recipe.superHeatedCoolant.getRepresentations());
            initChemical(gasStacks, 1, false, steamTank, Collections.singletonList(recipe.steam));
            initChemical(gasStacks, 2, false, cooledCoolantTank, Collections.singletonList(recipe.cooledCoolant));
        }
    }

    public static List<BoilerJEIRecipe> getBoilerRecipes() {
        //Note: The recipes below ignore the boiler's efficiency and rounds the amount of coolant
        int waterAmount = 1;
        double waterToSteamEfficiency = HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getSteamEnergyEfficiency();
        List<BoilerJEIRecipe> recipes = new ArrayList<>();
        //Special case heat only recipe
        double temperature = waterAmount * waterToSteamEfficiency / (BoilerMultiblockData.CASING_HEAT_CAPACITY * MekanismConfig.general.boilerWaterConductivity.get()) +
                             HeatUtils.BASE_BOIL_TEMP;
        recipes.add(new BoilerJEIRecipe(null, FluidStackIngredient.from(FluidTags.WATER, waterAmount),
              MekanismGases.STEAM.getStack(waterAmount), GasStack.EMPTY, temperature));
        //Go through all gases and add each coolant
        for (Gas gas : MekanismAPI.gasRegistry()) {
            HeatedCoolant heatedCoolant = gas.get(HeatedCoolant.class);
            if (heatedCoolant != null) {
                //If it is a cooled coolant add a recipe for it
                Gas cooledCoolant = heatedCoolant.getCooledGas();
                long coolantAmount = Math.round(waterAmount * waterToSteamEfficiency / heatedCoolant.getThermalEnthalpy());
                recipes.add(new BoilerJEIRecipe(GasStackIngredient.from(gas, coolantAmount), FluidStackIngredient.from(FluidTags.WATER, waterAmount),
                      MekanismGases.STEAM.getStack(waterAmount), cooledCoolant.getStack(coolantAmount), HeatUtils.BASE_BOIL_TEMP));
            }
        }
        return recipes;
    }

    public static class BoilerJEIRecipe {

        @Nullable
        private final GasStackIngredient superHeatedCoolant;
        private final FluidStackIngredient water;
        private final GasStack steam;
        private final GasStack cooledCoolant;
        private final double temperature;

        public BoilerJEIRecipe(@Nullable GasStackIngredient superHeatedCoolant, FluidStackIngredient water, GasStack steam, GasStack cooledCoolant, double temperature) {
            this.superHeatedCoolant = superHeatedCoolant;
            this.water = water;
            this.steam = steam;
            this.cooledCoolant = cooledCoolant;
            this.temperature = temperature;
        }
    }
}