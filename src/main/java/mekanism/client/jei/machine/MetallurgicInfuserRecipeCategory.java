package mekanism.client.jei.machine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.bar.GuiVerticalInfuseBar;
import mekanism.client.gui.element.bar.GuiVerticalInfuseBar.InfuseInfoProvider;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.MekanismBlock;
import mekanism.common.util.text.TextComponentUtil;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class MetallurgicInfuserRecipeCategory extends BaseRecipeCategory<MetallurgicInfuserRecipe> {

    private RecipeInfuseInfoProvider infoProvider;
    private GuiVerticalInfuseBar infuseBar;

    public MetallurgicInfuserRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/blank.png", MekanismBlock.METALLURGIC_INFUSER, ProgressBar.MEDIUM, 5, 16, 166, 54);
        infuseBar = new GuiVerticalInfuseBar(this, infoProvider = new RecipeInfuseInfoProvider(), guiLocation, 7, 15);
    }

    public static List<ItemStack> getInfuseStacks(InfuseType type) {
        List<ItemStack> list = new ArrayList<>();
        InfuseRegistry.getObjectMap().forEach((key, value) -> {
            if (value.isInfusionEqual(type)) {
                list.addAll(key.getRepresentations());
            }
        });
        return list;
    }

    public static List<ItemStack> getInfuseStacks(@NonNull List<InfusionStack> infuseObjects) {
        List<ItemStack> infuseStacks = new ArrayList<>();
        Set<InfuseType> checkedTypes = new HashSet<>();
        for (InfusionStack infusionStack : infuseObjects) {
            InfuseType type = infusionStack.getType();
            if (checkedTypes.add(type)) {
                //If we haven't already seen the type add all the stacks that produce it
                infuseStacks.addAll(getInfuseStacks(infusionStack.getType()));
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
        guiElements.add(infuseBar);
    }

    @Override
    public Class<? extends MetallurgicInfuserRecipe> getRecipeClass() {
        return MetallurgicInfuserRecipe.class;
    }

    @Override
    public void setIngredients(MetallurgicInfuserRecipe recipe, IIngredients ingredients) {
        List<ItemStack> inputStacks = recipe.getItemInput().getRepresentations();
        List<ItemStack> infuseStacks = MetallurgicInfuserRecipeCategory.getInfuseStacks(recipe.getInfusionInput().getRepresentations());
        //TODO: Check
        //ingredients.setInput(VanillaTypes.ITEM, recipe.recipeInput.inputStack);
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
        itemStacks.set(2, getInfuseStacks(recipe.getInfusionInput().getRepresentations()));
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

    private static class RecipeInfuseInfoProvider implements InfuseInfoProvider {

        @Nullable
        private MetallurgicInfuserRecipe cachedRecipe;

        @Nullable
        @Override
        public TextureAtlasSprite getSprite() {
            if (cachedRecipe == null) {
                return null;
            }
            @NonNull List<@NonNull InfusionStack> representations = cachedRecipe.getInfusionInput().getRepresentations();
            if (representations.isEmpty()) {
                return null;
            }
            //TODO: Make it so we can cycle
            return representations.get(0).getType().sprite;
        }

        @Override
        public ITextComponent getTooltip() {
            if (cachedRecipe != null) {
                @NonNull List<@NonNull InfusionStack> representations = cachedRecipe.getInfusionInput().getRepresentations();
                if (!representations.isEmpty()) {
                    InfusionStack infuse = representations.get(0);
                    if (!infuse.isEmpty()) {
                        return TextComponentUtil.build(infuse.getType(), ": " + infuse.getAmount());
                    }
                }
            }
            return TextComponentUtil.translate("gui.mekanism.empty");
        }

        @Override
        public double getLevel() {
            return 1;
        }
    }
}