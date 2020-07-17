package mekanism.client.jei.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Collections;
import java.util.List;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class ItemStackToEnergyRecipeCategory extends BaseRecipeCategory<ItemStackToEnergyRecipe> {

    private static final ResourceLocation iconRL = MekanismUtils.getResource(ResourceType.GUI, "energy.png");
    private GuiEnergyGauge gauge;

    public ItemStackToEnergyRecipeCategory(IGuiHelper helper, ResourceLocation id) {
        super(helper, id, MekanismLang.CONVERSION_ENERGY.translate(), 20, 12, 132, 62);
        icon = helper.drawableBuilder(iconRL, 0, 0, 18, 18)
              .setTextureSize(18, 18)
              .build();
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(gauge = GuiEnergyGauge.getDummy(GaugeType.STANDARD, this, 133, 13));
        guiElements.add(new GuiSlot(SlotType.INPUT, this, 25, 35));
        guiElements.add(new GuiProgress(() -> 1, ProgressType.LARGE_RIGHT, this, 64, 40));
    }

    @Override
    public Class<? extends ItemStackToEnergyRecipe> getRecipeClass() {
        return ItemStackToEnergyRecipe.class;
    }

    @Override
    public void setIngredients(ItemStackToEnergyRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getInput().getRepresentations()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ItemStackToEnergyRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 25 - xOffset, 35 - yOffset);
        itemStacks.set(0, recipe.getInput().getRepresentations());
    }

    @Override
    public void draw(ItemStackToEnergyRecipe recipe, MatrixStack matrix, double mouseX, double mouseY) {
        super.draw(recipe, matrix, mouseX, mouseY);
        if (!recipe.getOutputDefinition().isZero()) {
            //Manually draw the contents of the recipe
            gauge.renderContents(matrix);
        }
    }

    @Override
    public List<ITextComponent> getTooltipStrings(ItemStackToEnergyRecipe recipe, double mouseX, double mouseY) {
        if (gauge.isMouseOver(mouseX, mouseY) && !recipe.getOutputDefinition().isZero()) {
            //Manually add the tooltip showing the amounts if the mouse is over the energy gauge
            return Collections.singletonList(EnergyDisplay.of(recipe.getOutputDefinition()).getTextComponent());
        }
        return Collections.emptyList();
    }
}