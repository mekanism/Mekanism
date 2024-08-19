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
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class MetallurgicInfuserRecipeCategory extends HolderRecipeCategory<ItemStackChemicalToItemStackRecipe> {

    private final GuiSlot input;
    private final GuiSlot extra;
    private final GuiSlot output;
    private final GuiBar<?> infusionBar;

    public MetallurgicInfuserRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ItemStackChemicalToItemStackRecipe> recipeType) {
        super(helper, recipeType);
        extra = addSlot(SlotType.EXTRA, 17, 35);
        input = addSlot(SlotType.INPUT, 51, 43);
        output = addSlot(SlotType.OUTPUT, 109, 43);
        addSlot(SlotType.POWER, 143, 35).with(SlotOverlay.POWER);
        addElement(new GuiVerticalPowerBar(this, RecipeViewerUtils.FULL_BAR, 164, 15));
        addSimpleProgress(ProgressType.RIGHT, 72, 47);
        infusionBar = addElement(new GuiEmptyBar(this, 7, 15, 4, 52));
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RecipeHolder<ItemStackChemicalToItemStackRecipe> recipeHolder, @NotNull IFocusGroup focusGroup) {
        ItemStackChemicalToItemStackRecipe recipe = recipeHolder.value();
        initItem(builder, RecipeIngredientRole.INPUT, input, recipe.getItemInput().getRepresentations());
        List<ChemicalStack> scaledChemicals = recipe.getChemicalInput().getRepresentations();
        if (recipe.perTickUsage()) {
            scaledChemicals = scaledChemicals.stream()
                  .map(chemical -> chemical.copyWithAmount(chemical.getAmount() * TileEntityMetallurgicInfuser.BASE_TICKS_REQUIRED))
                  .toList();
        }
        initChemical(builder, RecipeIngredientRole.INPUT, infusionBar, scaledChemicals);
        initItem(builder, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDefinition());
        initItem(builder, RecipeIngredientRole.CATALYST, extra, RecipeViewerUtils.getStacksFor(recipe.getChemicalInput(), true));
    }
}