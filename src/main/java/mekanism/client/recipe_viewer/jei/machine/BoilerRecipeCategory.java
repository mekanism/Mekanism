package mekanism.client.recipe_viewer.jei.machine;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import mekanism.api.heat.HeatAPI;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.recipe_viewer.jei.BaseRecipeCategory;
import mekanism.client.recipe_viewer.recipe.BoilerRecipeViewerRecipe;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.TextUtils;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoilerRecipeCategory extends BaseRecipeCategory<BoilerRecipeViewerRecipe> {

    private final GuiGauge<?> superHeatedCoolantTank;
    private final GuiGauge<?> waterTank;
    private final GuiGauge<?> steamTank;
    private final GuiGauge<?> cooledCoolantTank;
    @Nullable
    private BoilerRecipeViewerRecipe recipe;

    public BoilerRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<BoilerRecipeViewerRecipe> recipeType) {
        super(helper, recipeType);
        //Note: All these elements except for the heatedCoolantTank and waterTank are in slightly different x positions than in the normal GUI
        // so that they fit properly in JEI
        addElement(new GuiInnerScreen(this, 48, 23, 96, 40, () -> {
            double temperature;
            int boilRate;
            if (recipe == null) {
                temperature = HeatAPI.AMBIENT_TEMP;
                boilRate = 0;
            } else {
                temperature = recipe.temperature();
                boilRate = MathUtils.clampToInt(recipe.steam().getAmount());
            }
            return List.of(MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(temperature, TemperatureUnit.KELVIN, true)),
                  MekanismLang.BOIL_RATE.translate(TextUtils.format(boilRate)));
        }
        ));
        superHeatedCoolantTank = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD, this, 6, 13).setLabel(MekanismLang.BOILER_HEATED_COOLANT_TANK.translateColored(EnumColor.ORANGE)));
        waterTank = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 26, 13).setLabel(MekanismLang.BOILER_WATER_TANK.translateColored(EnumColor.INDIGO)));
        steamTank = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD, this, 148, 13).setLabel(MekanismLang.BOILER_STEAM_TANK.translateColored(EnumColor.GRAY)));
        cooledCoolantTank = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD, this, 168, 13).setLabel(MekanismLang.BOILER_COOLANT_TANK.translateColored(EnumColor.AQUA)));
    }

    @Override
    public void draw(BoilerRecipeViewerRecipe recipe, IRecipeSlotsView recipeSlotView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        //Update what the current recipe is so that we have the proper values for temperature and the like
        this.recipe = recipe;
        super.draw(recipe, recipeSlotView, guiGraphics, mouseX, mouseY);
        this.recipe = null;
    }

    @Override
    protected void renderElements(BoilerRecipeViewerRecipe recipe, IRecipeSlotsView recipeSlotView, GuiGraphics guiGraphics, int x, int y) {
        super.renderElements(recipe, recipeSlotView, guiGraphics, x, y);
        if (recipe.superHeatedCoolant() == null) {
            superHeatedCoolantTank.drawBarOverlay(guiGraphics);
            cooledCoolantTank.drawBarOverlay(guiGraphics);
        }
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName(@NotNull BoilerRecipeViewerRecipe recipe) {
        return recipe.id();
    }

    @NotNull
    @Override
    public Codec<BoilerRecipeViewerRecipe> getCodec(@NotNull ICodecHelper codecHelper, @NotNull IRecipeManager recipeManager) {
        return BoilerRecipeViewerRecipe.CODEC;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, BoilerRecipeViewerRecipe recipe, @NotNull IFocusGroup focusGroup) {
        initFluid(builder, RecipeIngredientRole.INPUT, waterTank, recipe.water().getRepresentations());
        if (recipe.superHeatedCoolant() == null) {
            initChemical(builder, RecipeIngredientRole.OUTPUT, steamTank, Collections.singletonList(recipe.steam()));
        } else {
            initChemical(builder, RecipeIngredientRole.INPUT, superHeatedCoolantTank, recipe.superHeatedCoolant().getRepresentations());
            initChemical(builder, RecipeIngredientRole.OUTPUT, steamTank, Collections.singletonList(recipe.steam()));
            initChemical(builder, RecipeIngredientRole.OUTPUT, cooledCoolantTank, Collections.singletonList(recipe.cooledCoolant()));
        }
    }
}