package mekanism.client.recipe_viewer.jei.machine;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.recipe_viewer.jei.BaseRecipeCategory;
import mekanism.client.recipe_viewer.recipe.SPSRecipeViewerRecipe;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.Color;
import mekanism.common.lib.Color.ColorFunction;
import mekanism.common.util.text.EnergyDisplay;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SPSRecipeCategory extends BaseRecipeCategory<SPSRecipeViewerRecipe> {

    private final GuiGauge<?> input;
    private final GuiGauge<?> output;

    public SPSRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<SPSRecipeViewerRecipe> recipeType) {
        super(helper, recipeType);
        addElement(new GuiInnerScreen(this, 26, 13, 122, 60, () -> {
            List<Component> list = new ArrayList<>();
            list.add(MekanismLang.STATUS.translate(MekanismLang.ACTIVE));
            list.add(MekanismLang.SPS_ENERGY_INPUT.translate(EnergyDisplay.of(
                  MathUtils.multiplyClamped(MekanismConfig.general.spsEnergyPerInput.get(), MekanismConfig.general.spsInputPerAntimatter.get()))));
            list.add(MekanismLang.PROCESS_RATE_MB.translate(1.0));
            return list;
        }));
        input = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD, this, 6, 13));
        output = addElement(GuiChemicalGauge.getDummy(GaugeType.STANDARD, this, 150, 13));
        addElement(new GuiDynamicHorizontalRateBar(this, getBarProgressTimer(), 6, 75, 160,
              ColorFunction.scale(Color.rgbi(60, 45, 74), Color.rgbi(100, 30, 170))));
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, SPSRecipeViewerRecipe recipe, @NotNull IFocusGroup focusGroup) {
        initChemical(builder, RecipeIngredientRole.INPUT, input, recipe.input().getRepresentations());
        initChemical(builder, RecipeIngredientRole.OUTPUT, output, Collections.singletonList(recipe.output()));
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName(@NotNull SPSRecipeViewerRecipe recipe) {
        return recipe.id();
    }

    @NotNull
    @Override
    public Codec<SPSRecipeViewerRecipe> getCodec(@NotNull ICodecHelper codecHelper, @NotNull IRecipeManager recipeManager) {
        return SPSRecipeViewerRecipe.CODEC;
    }
}