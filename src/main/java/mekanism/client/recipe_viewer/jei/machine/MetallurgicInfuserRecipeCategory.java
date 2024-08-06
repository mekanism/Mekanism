package mekanism.client.recipe_viewer.jei.machine;

import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.client.gui.element.bar.GuiEmptyBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.jei.HolderRecipeCategory;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

public class MetallurgicInfuserRecipeCategory extends HolderRecipeCategory<MetallurgicInfuserRecipe> {

    private final GuiSlot input;
    private final GuiSlot extra;
    private final GuiSlot output;
    private final GuiBar<?> infusionBar;

    public MetallurgicInfuserRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<MetallurgicInfuserRecipe> recipeType) {
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
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, RecipeHolder<MetallurgicInfuserRecipe> recipeHolder, @NotNull IFocusGroup focusGroup) {
        MetallurgicInfuserRecipe recipe = recipeHolder.value();
        initItem(builder, RecipeIngredientRole.INPUT, input, recipe.getItemInput().getRepresentations());
        List<@NotNull ChemicalStack> infusionStacks = recipe.getChemicalInput().getRepresentations();
        initChemical(builder, MekanismJEI.TYPE_INFUSION, RecipeIngredientRole.INPUT, infusionBar, infusionStacks);
        initItem(builder, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDefinition());
        initItem(builder, RecipeIngredientRole.CATALYST, extra, RecipeViewerUtils.getStacksFor(recipe.getChemicalInput(), true));
    }
}