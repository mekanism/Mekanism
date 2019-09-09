package mekanism.client.jei.machine.other;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mekanism.api.annotations.NonNull;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
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
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import org.apache.commons.lang3.tuple.Pair;

public class MetallurgicInfuserRecipeCategory<WRAPPER extends MetallurgicInfuserRecipeWrapper> extends BaseRecipeCategory<WRAPPER> {

    public MetallurgicInfuserRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/GuiMetallurgicInfuser.png", Recipe.METALLURGIC_INFUSER.getJEICategory(),
              "tile.MachineBlock.MetallurgicInfuser.name", ProgressBar.MEDIUM, 5, 16, 166, 54);
    }

    public static List<ItemStack> getInfuseStacks(@NonNull List<InfuseObject> infuseObjects) {
        List<ItemStack> infuseStacks = new ArrayList<>();
        Set<InfuseType> checkedTypes = new HashSet<>();
        Collection<Pair<InfuseObject, Ingredient>> registeredInfuseObjects = InfuseRegistry.getInfuseObjects();
        for (InfuseObject infuseObject : infuseObjects) {
            InfuseType type = infuseObject.getType();
            if (checkedTypes.add(type)) {
                //If we haven't already seen the type add all the stacks that produce it
                for (Pair<InfuseObject, Ingredient> obj : registeredInfuseObjects) {
                    if (obj.getLeft().type == type) {
                        Collections.addAll(infuseStacks, obj.getRight().getMatchingStacks());
                    }
                }
            }
        }
        return infuseStacks;
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
    public void setRecipe(IRecipeLayout recipeLayout, WRAPPER recipeWrapper, IIngredients ingredients) {
        MetallurgicInfuserRecipe tempRecipe = recipeWrapper.getRecipe();
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 45, 26);
        itemStacks.init(1, false, 103, 26);
        itemStacks.init(2, true, 11, 18);
        itemStacks.set(0, tempRecipe.getItemInput().getRepresentations());
        itemStacks.set(1, tempRecipe.getOutputDefinition());
        itemStacks.set(2, getInfuseStacks(tempRecipe.getInfusionInput().getRepresentations()));
    }
}