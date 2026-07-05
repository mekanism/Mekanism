package mekanism.client.recipe_viewer.jei.machine;

import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.client.gui.element.bar.GuiEmptyBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.jei.HolderRecipeCategory;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ItemStackChemicalToItemStackRecipeCategory extends HolderRecipeCategory<ItemStackChemicalToItemStackRecipe> {

    private final GuiBar<?> chemicalInput;
    private final GuiSlot input;
    private final GuiSlot extra;
    private final GuiSlot output;

    public ItemStackChemicalToItemStackRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ItemStackChemicalToItemStackRecipe> recipeType) {
        super(helper, recipeType);
        input = addSlot(SlotType.INPUT, 64, 17);
        extra = addSlot(SlotType.EXTRA, 64, 53);
        output = addSlot(SlotType.OUTPUT, 116, 35);
        addSlot(SlotType.POWER, 39, 35).with(SlotOverlay.POWER);
        addElement(new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 16));
        chemicalInput = addElement(new GuiEmptyBar(this, 68, 36, 6, 12));
        addSimpleProgress(ProgressType.BAR, 86, 38);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ItemStackChemicalToItemStackRecipe> recipeHolder, IFocusGroup focusGroup) {
        ItemStackChemicalToItemStackRecipe recipe = recipeHolder.value();
        initItem(builder, RecipeIngredientRole.INPUT, input, recipe.getItemInput()::getRepresentations);
        initChemical(builder, RecipeIngredientRole.INPUT, chemicalInput, recipe, (r, context) -> {
            List<ChemicalStack> scaledChemicals = r.getChemicalInput().getRepresentations(context);
            if (r.perTickUsage()) {
                return scaledChemicals.stream()
                      .map(chemical -> chemical.copyWithAmount(chemical.amount() * TileEntityAdvancedElectricMachine.BASE_TICKS_REQUIRED))
                      .toList();
            }
            return scaledChemicals;
        });
        initItem(builder, output, recipe::getOutputDefinition);
        initItem(builder, RecipeIngredientRole.CRAFTING_STATION, extra, recipe.getChemicalInput(), (chemicalInput, context) -> RecipeViewerUtils.getStacksFor(chemicalInput, context, true));
    }
}