package mekanism.client.jei.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.client.gui.element.bar.GuiEmptyBar;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
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

    private final GuiSlot input;
    private final GuiSlot extra;
    private final GuiSlot output;
    private final GuiBar<?> infusionBar;

    public MetallurgicInfuserRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.METALLURGIC_INFUSER, 5, 16, 166, 54);
        extra = addSlot(SlotType.EXTRA, 17, 35);
        input = addSlot(SlotType.INPUT, 51, 43);
        output = addSlot(SlotType.OUTPUT, 109, 43);
        addSlot(SlotType.POWER, 143, 35).with(SlotOverlay.POWER);
        addElement(new GuiVerticalPowerBar(this, FULL_BAR, 164, 15));
        addSimpleProgress(ProgressType.RIGHT, 72, 47);
        infusionBar = addElement(new GuiEmptyBar(this, 7, 15, 4, 52));
    }

    @Override
    public Class<? extends MetallurgicInfuserRecipe> getRecipeClass() {
        return MetallurgicInfuserRecipe.class;
    }

    @Override
    public void setIngredients(MetallurgicInfuserRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getItemInput().getRepresentations()));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
        ingredients.setInputLists(MekanismJEI.TYPE_INFUSION, Collections.singletonList(recipe.getChemicalInput().getRepresentations()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, MetallurgicInfuserRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        initItem(itemStacks, 0, true, input, recipe.getItemInput().getRepresentations());
        initItem(itemStacks, 1, false, output, recipe.getOutputDefinition());
        List<ItemStack> infuseItemProviders = new ArrayList<>();
        List<@NonNull InfusionStack> infusionStacks = recipe.getChemicalInput().getRepresentations();
        for (InfusionStack infusionStack : infusionStacks) {
            infuseItemProviders.addAll(MekanismJEI.INFUSION_STACK_HELPER.getStacksFor(infusionStack.getType(), true));
        }
        initItem(itemStacks, 2, true, extra, infuseItemProviders);
        initChemical(recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_INFUSION), 0, true, infusionBar, infusionStacks);
    }
}