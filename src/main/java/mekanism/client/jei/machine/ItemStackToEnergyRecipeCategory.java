package mekanism.client.jei.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiEnergyGauge;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.MekanismLang;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class ItemStackToEnergyRecipeCategory extends BaseRecipeCategory<ItemStackToEnergyRecipe> {

    private static final ResourceLocation iconRL = MekanismUtils.getResource(ResourceType.GUI, "energy.png");
    //Note: We use a weak hashmap so that when the recipe stops existing either due to disconnecting from the server
    // or because of a reload, then it can be properly garbage collected, but until then we keep track of the pairing
    // between the recipe and the ingredient group JEI has so that we can ensure the arrows are the proper color
    private final Map<ItemStackToEnergyRecipe, IGuiItemStackGroup> ingredients = new WeakHashMap<>();
    private final GuiEnergyGauge gauge;
    private final GuiSlot input;

    public ItemStackToEnergyRecipeCategory(IGuiHelper helper, ResourceLocation id) {
        super(helper, id, MekanismLang.CONVERSION_ENERGY.translate(), createIcon(helper, iconRL), 20, 12, 132, 62);
        gauge = addElement(GuiEnergyGauge.getDummy(GaugeType.STANDARD.with(DataType.OUTPUT), this, 133, 13));
        input = addSlot(SlotType.INPUT, 26, 36);
        addConstantProgress(ProgressType.LARGE_RIGHT, 64, 40);
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
        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
        initItem(itemStackGroup, 0, true, input, recipe.getInput().getRepresentations());
        this.ingredients.put(recipe, itemStackGroup);
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
        if (gauge.isMouseOver(mouseX, mouseY)) {
            FloatingLong energy = getOutputEnergy(recipe);
            if (!energy.isZero()) {
                //Manually add the tooltip showing the amounts if the mouse is over the energy gauge
                ITextComponent energyOutput = EnergyDisplay.of(energy).getTextComponent();
                if (Minecraft.getInstance().options.advancedItemTooltips || Screen.hasShiftDown()) {
                    return Arrays.asList(energyOutput, TextComponentUtil.build(TextFormatting.DARK_GRAY, MekanismLang.JEI_RECIPE_ID.translate(recipe.getId())));
                }
                return Collections.singletonList(energyOutput);
            }
        }
        return Collections.emptyList();
    }

    private FloatingLong getOutputEnergy(ItemStackToEnergyRecipe recipe) {
        IGuiIngredientGroup<ItemStack> group = ingredients.get(recipe);
        if (group != null) {
            IGuiIngredient<ItemStack> guiIngredient = group.getGuiIngredients().get(0);
            if (guiIngredient != null) {
                ItemStack displayedIngredient = guiIngredient.getDisplayedIngredient();
                if (displayedIngredient != null && !displayedIngredient.isEmpty()) {
                    return recipe.getOutput(displayedIngredient);
                }
            }
        }
        //TODO: Re-evaluate this, but for now just fallback to output definition
        return recipe.getOutputDefinition();
    }
}