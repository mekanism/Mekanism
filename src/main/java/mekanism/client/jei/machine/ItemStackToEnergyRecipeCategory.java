package mekanism.client.jei.machine;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ItemStackToEnergyRecipeCategory extends BaseRecipeCategory<ItemStackToEnergyRecipe> {

    private static final ResourceLocation iconRL = MekanismUtils.getResource(ResourceType.GUI, "energy.png");
    private static final String INPUT = "input";

    private final GuiEnergyGauge gauge;
    private final GuiSlot input;

    public ItemStackToEnergyRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<ItemStackToEnergyRecipe> recipeType) {
        super(helper, recipeType, MekanismLang.CONVERSION_ENERGY.translate(), createIcon(helper, iconRL), 20, 12, 132, 62);
        gauge = addElement(GuiEnergyGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 133, 13));
        input = addSlot(SlotType.INPUT, 26, 36);
        addConstantProgress(ProgressType.LARGE_RIGHT, 64, 40);
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, ItemStackToEnergyRecipe recipe, @Nonnull IFocusGroup focusGroup) {
        initItem(builder, RecipeIngredientRole.INPUT, input, recipe.getInput().getRepresentations())
              .setSlotName(INPUT);
    }

    @Override
    public void draw(ItemStackToEnergyRecipe recipe, IRecipeSlotsView recipeSlotView, PoseStack matrix, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotView, matrix, mouseX, mouseY);
        if (!getOutputEnergy(recipe, recipeSlotView).isZero()) {
            //Manually draw the contents of the recipe
            gauge.renderContents(matrix);
        }
    }

    @Override
    public List<Component> getTooltipStrings(ItemStackToEnergyRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (gauge.isMouseOver(mouseX, mouseY)) {
            FloatingLong energy = getOutputEnergy(recipe, recipeSlotsView);
            if (!energy.isZero()) {
                //Manually add the tooltip showing the amounts if the mouse is over the energy gauge
                Component energyOutput = EnergyDisplay.of(energy).getTextComponent();
                if (Minecraft.getInstance().options.advancedItemTooltips || Screen.hasShiftDown()) {
                    return List.of(energyOutput, TextComponentUtil.build(ChatFormatting.DARK_GRAY, MekanismLang.JEI_RECIPE_ID.translate(recipe.getId())));
                }
                return Collections.singletonList(energyOutput);
            }
        }
        return Collections.emptyList();
    }

    private FloatingLong getOutputEnergy(ItemStackToEnergyRecipe recipe, IRecipeSlotsView recipeSlotsView) {
        ItemStack displayedIngredient = getDisplayedStack(recipeSlotsView, INPUT, VanillaTypes.ITEM_STACK, ItemStack.EMPTY);
        if (displayedIngredient.isEmpty()) {
            //Shouldn't happen but if it does just return no energy known so nothing will really show
            return FloatingLong.ZERO;
        }
        return recipe.getOutput(displayedIngredient);
    }
}