package mekanism.client.jei.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.client.gui.element.bar.GuiEmptyBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;

public class MetallurgicInfuserRecipeCategory extends BaseRecipeCategory<MetallurgicInfuserRecipe> {

    public MetallurgicInfuserRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.METALLURGIC_INFUSER, 5, 16, 166, 54);
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(new GuiSlot(SlotType.EXTRA, this, 16, 34));
        guiElements.add(new GuiSlot(SlotType.INPUT, this, 50, 42));
        guiElements.add(new GuiSlot(SlotType.POWER, this, 142, 34).with(SlotOverlay.POWER));
        guiElements.add(new GuiSlot(SlotType.OUTPUT, this, 108, 42));
        guiElements.add(new GuiVerticalPowerBar(this, () -> 1F, 164, 15));
        guiElements.add(new GuiProgress(() -> timer.getValue() / 20D, ProgressType.RIGHT, this, 72, 47));
        guiElements.add(new GuiEmptyBar(this, 7, 15, 4, 52));
    }

    @Override
    public Class<? extends MetallurgicInfuserRecipe> getRecipeClass() {
        return MetallurgicInfuserRecipe.class;
    }

    @Override
    public void setIngredients(MetallurgicInfuserRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getItemInput().getRepresentations()));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
        ingredients.setInputLists(MekanismJEI.TYPE_INFUSION, Collections.singletonList(recipe.getInfusionInput().getRepresentations()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, MetallurgicInfuserRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 45, 26);
        itemStacks.init(1, false, 103, 26);
        itemStacks.init(2, true, 11, 18);
        itemStacks.set(0, recipe.getItemInput().getRepresentations());
        itemStacks.set(1, recipe.getOutputDefinition());
        List<ItemStack> infuseItemProviders = new ArrayList<>();
        List<@NonNull InfusionStack> infusionStacks = recipe.getInfusionInput().getRepresentations();
        for (InfusionStack infusionStack : infusionStacks) {
            infuseItemProviders.addAll(MekanismJEI.INFUSION_STACK_HELPER.getStacksFor(infusionStack.getType(), true));
        }
        itemStacks.set(2, infuseItemProviders);
        initChemical(recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_INFUSION), 0, true, 3, 0, 4, 52, infusionStacks);
    }
}