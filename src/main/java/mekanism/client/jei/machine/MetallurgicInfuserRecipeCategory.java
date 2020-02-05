package mekanism.client.jei.machine;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NonNull;
import mekanism.api.infuse.InfuseType;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.inputs.InfusionIngredient;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.bar.GuiVerticalChemicalBar;
import mekanism.client.gui.element.bar.GuiVerticalChemicalBar.ChemicalInfoProvider;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.MekanismLang;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.text.TextComponentUtil;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class MetallurgicInfuserRecipeCategory extends BaseRecipeCategory<MetallurgicInfuserRecipe> {

    private RecipeInfuseInfoProvider infoProvider;
    private GuiVerticalChemicalBar<InfuseType> infuseBar;

    public MetallurgicInfuserRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/blank.png", MekanismBlocks.METALLURGIC_INFUSER, ProgressBar.MEDIUM, 5, 16, 166, 54);
    }

    /**
     * Helper method for JEI to get the stacks to display for a specific infusion ingredient
     */
    private static List<ItemStack> getInfuseStacks(@NonNull InfusionIngredient infusionIngredient) {
        World world = Minecraft.getInstance().world;
        if (world == null) {
            return Collections.emptyList();
        }
        List<InfusionStack> infuseObjects = infusionIngredient.getRepresentations();
        if (infuseObjects.isEmpty()) {
            return Collections.emptyList();
        }
        //TODO: See if this can be improved
        List<ItemStackToInfuseTypeRecipe> recipes = MekanismRecipeType.INFUSION_CONVERSION.getRecipes(world);
        List<ItemStack> infuseStacks = new ArrayList<>();
        Set<InfuseType> checkedTypes = new ObjectOpenHashSet<>();
        for (InfusionStack infusionStack : infuseObjects) {
            InfuseType type = infusionStack.getType();
            if (checkedTypes.add(type)) {
                //If we haven't already seen the type add all the stacks that produce it
                for (ItemStackToInfuseTypeRecipe recipe : recipes) {
                    if (recipe.getOutputDefinition().isTypeEqual(type)) {
                        infuseStacks.addAll(recipe.getInput().getRepresentations());
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
        guiElements.add(new GuiVerticalPowerBar(this, () -> 1F, guiLocation, 164, 15));
        guiElements.add(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return (double) timer.getValue() / 20F;
            }
        }, ProgressBar.MEDIUM, this, guiLocation, 70, 46));
        guiElements.add(infuseBar = new GuiVerticalChemicalBar<>(this, infoProvider = new RecipeInfuseInfoProvider(), guiLocation, 7, 15));
    }

    @Override
    public Class<? extends MetallurgicInfuserRecipe> getRecipeClass() {
        return MetallurgicInfuserRecipe.class;
    }

    @Override
    public void setIngredients(MetallurgicInfuserRecipe recipe, IIngredients ingredients) {
        List<ItemStack> inputStacks = recipe.getItemInput().getRepresentations();
        List<ItemStack> infuseStacks = getInfuseStacks(recipe.getInfusionInput());
        ingredients.setInputLists(VanillaTypes.ITEM, Arrays.asList(inputStacks, infuseStacks));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, MetallurgicInfuserRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 45, 26);
        itemStacks.init(1, false, 103, 26);
        itemStacks.init(2, true, 11, 18);
        itemStacks.set(0, recipe.getItemInput().getRepresentations());
        itemStacks.set(1, recipe.getOutputDefinition());
        itemStacks.set(2, getInfuseStacks(recipe.getInfusionInput()));
    }

    @Override
    public List<ITextComponent> getTooltipComponents(MetallurgicInfuserRecipe recipe, double mouseX, double mouseY) {
        List<ITextComponent> tooltips = new ArrayList<>();
        //TODO: Use isHovered, fix the mouseX and mouseY that get passed to draw
        if (infuseBar.isMouseOver(mouseX, mouseY)) {
            infoProvider.cachedRecipe = recipe;
            tooltips.add(infoProvider.getTooltip());
            infoProvider.cachedRecipe = null;
        }
        return tooltips;
    }

    @Override
    public void draw(MetallurgicInfuserRecipe recipe, double mouseX, double mouseY) {
        infoProvider.cachedRecipe = recipe;
        super.draw(recipe, mouseX, mouseY);
        infoProvider.cachedRecipe = null;
    }

    private static class RecipeInfuseInfoProvider implements ChemicalInfoProvider<InfuseType> {

        @Nullable
        private MetallurgicInfuserRecipe cachedRecipe;

        @Override
        public InfuseType getType() {
            if (cachedRecipe == null) {
                return MekanismAPI.EMPTY_INFUSE_TYPE;
            }
            @NonNull List<@NonNull InfusionStack> representations = cachedRecipe.getInfusionInput().getRepresentations();
            if (representations.isEmpty()) {
                return MekanismAPI.EMPTY_INFUSE_TYPE;
            }
            //TODO: Make it so we can cycle
            return representations.get(0).getType();
        }

        @Override
        public ITextComponent getTooltip() {
            if (cachedRecipe != null) {
                //TODO: Make it so we can cycle
                @NonNull List<@NonNull InfusionStack> representations = cachedRecipe.getInfusionInput().getRepresentations();
                if (!representations.isEmpty()) {
                    InfusionStack infuse = representations.get(0);
                    if (!infuse.isEmpty()) {
                        return TextComponentUtil.build(infuse.getType(), ": " + infuse.getAmount());
                    }
                }
            }
            return MekanismLang.EMPTY.translate();
        }

        @Override
        public double getLevel() {
            return 1;
        }
    }
}