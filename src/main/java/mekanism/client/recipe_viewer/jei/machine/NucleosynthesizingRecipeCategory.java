package mekanism.client.recipe_viewer.jei.machine;

import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiDynamicHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.gauge.GuiEnergyGauge.IEnergyInfoHandler;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.jei.HolderRecipeCategory;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.lib.Color;
import mekanism.common.lib.Color.ColorFunction;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.machine.TileEntityAntiprotonicNucleosynthesizer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class NucleosynthesizingRecipeCategory extends HolderRecipeCategory<NucleosynthesizingRecipe> {

    private final GuiDynamicHorizontalRateBar rateBar;
    private final GuiSlot input;
    private final GuiSlot extra;
    private final GuiSlot output;
    private final GuiGauge<?> chemicalInput;

    public NucleosynthesizingRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<NucleosynthesizingRecipe> recipeType) {
        super(helper, recipeType);
        input = addSlot(SlotType.INPUT, 26, 40);
        extra = addSlot(SlotType.EXTRA, 6, 69);
        output = addSlot(SlotType.OUTPUT, 152, 40);
        addSlot(SlotType.POWER, 173, 69).with(SlotOverlay.POWER);
        addElement(new GuiInnerScreen(this, 45, 18, 104, 68));
        GaugeType type = GaugeType.SMALL_MED.with(DataType.INPUT);
        chemicalInput = addElement(GuiChemicalGauge.getDummy(type, this, 5, 18));
        addElement(new GuiEnergyGauge(new IEnergyInfoHandler() {
            @Override
            public long getEnergy() {
                return 1L;
            }

            @Override
            public long getMaxEnergy() {
                return 1L;
            }
        }, GaugeType.SMALL_MED, this, 172, 18));
        rateBar = addElement(new GuiDynamicHorizontalRateBar(this, getBarProgressTimer(), 5, 88, 183,
              ColorFunction.scale(Color.rgbi(60, 45, 74), Color.rgbi(100, 30, 170))));
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<NucleosynthesizingRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (rateBar.isMouseOver(mouseX, mouseY)) {
            tooltip.add(MekanismLang.TICKS_REQUIRED.translate(recipeHolder.value().getDuration()));
        }
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RecipeHolder<NucleosynthesizingRecipe> recipeHolder, @NotNull IFocusGroup focusGroup) {
        NucleosynthesizingRecipe recipe = recipeHolder.value();
        initItem(builder, RecipeIngredientRole.INPUT, input, recipe.getItemInput().getRepresentations());
        List<ChemicalStack> scaledChemicals = recipe.getChemicalInput().getRepresentations();
        if (recipe.perTickUsage()) {
            scaledChemicals = scaledChemicals.stream()
                  .map(chemical -> chemical.copyWithAmount(chemical.getAmount() * TileEntityAntiprotonicNucleosynthesizer.BASE_TICKS_REQUIRED))
                  .toList();
        }
        initChemical(builder, RecipeIngredientRole.INPUT, chemicalInput, scaledChemicals);
        initItem(builder, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDefinition());
        initItem(builder, RecipeIngredientRole.CATALYST, extra, RecipeViewerUtils.getStacksFor(recipe.getChemicalInput(), true));
    }
}