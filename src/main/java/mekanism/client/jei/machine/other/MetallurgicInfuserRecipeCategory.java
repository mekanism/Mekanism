package mekanism.client.jei.machine.other;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiPowerBar.IPowerInfoHandler;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

public class MetallurgicInfuserRecipeCategory extends BaseRecipeCategory {

    public MetallurgicInfuserRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/GuiMetallurgicInfuser.png", Recipe.METALLURGIC_INFUSER.getJEICategory(),
              "tile.MachineBlock.MetallurgicInfuser.name", ProgressBar.MEDIUM, 5, 16, 166, 54);
    }

    public static List<ItemStack> getInfuseStacks(InfuseType type) {
        return InfuseRegistry.getObjectMap().entrySet().stream()
              .filter(obj -> obj.getValue().type == type).map(Entry::getKey).collect(Collectors.toList());
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(new GuiSlot(SlotType.EXTRA, this, guiLocation, 16, 34));
        guiElements.add(new GuiSlot(SlotType.INPUT, this, guiLocation, 50, 42));
        guiElements.add(new GuiSlot(SlotType.POWER, this, guiLocation, 142, 34).with(SlotOverlay.POWER));
        guiElements.add(new GuiSlot(SlotType.OUTPUT, this, guiLocation, 108, 42));
        guiElements.add(new GuiPowerBar(this, new IPowerInfoHandler() {
            @Override
            public double getLevel() {
                return 1F;
            }
        }, guiLocation, 164, 15));
        guiElements.add(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return (double) timer.getValue() / 20F;
            }
        }, ProgressBar.MEDIUM, this, guiLocation, 70, 46));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) {
        if (recipeWrapper instanceof MetallurgicInfuserRecipeWrapper) {
            MetallurgicInfuserRecipe tempRecipe = ((MetallurgicInfuserRecipeWrapper) recipeWrapper).getRecipe();
            IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
            itemStacks.init(0, true, 45, 26);
            itemStacks.init(1, false, 103, 26);
            itemStacks.init(2, true, 11, 18);
            itemStacks.set(0, tempRecipe.getInput().inputStack);
            itemStacks.set(1, tempRecipe.getOutput().output);
            itemStacks.set(2, getInfuseStacks(tempRecipe.getInput().infuse.type));
        }
    }
}