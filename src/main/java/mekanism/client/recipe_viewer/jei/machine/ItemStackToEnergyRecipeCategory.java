package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.jei.HolderRecipeCategory;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.text.EnergyDisplay;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class ItemStackToEnergyRecipeCategory extends HolderRecipeCategory<ItemStackToEnergyRecipe> {

    private static final String INPUT = "input";

    private final GuiEnergyGauge gauge;
    private final GuiSlot input;

    public ItemStackToEnergyRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ItemStackToEnergyRecipe> recipeType) {
        super(helper, recipeType);
        gauge = addElement(GuiEnergyGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 133, 13));
        input = addSlot(SlotType.INPUT, 26, 36);
        addConstantProgress(ProgressType.LARGE_RIGHT, 64, 40);
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RecipeHolder<ItemStackToEnergyRecipe> recipeHolder, @NotNull IFocusGroup focusGroup) {
        initItem(builder, RecipeIngredientRole.INPUT, input, recipeHolder.value().getInput().getRepresentations())
              .setSlotName(INPUT);
    }

    @Override
    protected void renderElements(RecipeHolder<ItemStackToEnergyRecipe> recipeHolder, IRecipeSlotsView recipeSlotView, GuiGraphics guiGraphics, int x, int y) {
        super.renderElements(recipeHolder, recipeSlotView, guiGraphics, x, y);
        if (getOutputEnergy(recipeHolder, recipeSlotView) != 0L) {
            //Manually draw the contents of the recipe
            gauge.renderContents(guiGraphics);
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<ItemStackToEnergyRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (gauge.isMouseOver(mouseX, mouseY)) {
            long energy = getOutputEnergy(recipeHolder, recipeSlotsView);
            if (energy != 0L) {
                //Manually add the tooltip showing the amounts if the mouse is over the energy gauge
                tooltip.add(EnergyDisplay.of(energy).getTextComponent());
                if (Minecraft.getInstance().options.advancedItemTooltips || Screen.hasShiftDown()) {
                    tooltip.add(TextComponentUtil.build(ChatFormatting.DARK_GRAY, MekanismLang.JEI_RECIPE_ID.translate(recipeHolder.id())));
                }
            }
        }
    }

    private long getOutputEnergy(RecipeHolder<ItemStackToEnergyRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView) {
        ItemStack displayedIngredient = getDisplayedStack(recipeSlotsView, INPUT, VanillaTypes.ITEM_STACK, ItemStack.EMPTY);
        if (displayedIngredient.isEmpty()) {
            //Shouldn't happen but if it does just return no energy known so nothing will really show
            return 0L;
        }
        return recipeHolder.value().getOutput(displayedIngredient);
    }
}