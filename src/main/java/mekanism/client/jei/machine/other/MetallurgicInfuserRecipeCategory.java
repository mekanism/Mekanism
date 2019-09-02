package mekanism.client.jei.machine.other;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.bar.GuiInfuseBar;
import mekanism.client.gui.element.bar.GuiInfuseBar.InfuseInfoProvider;
import mekanism.client.gui.element.bar.GuiPowerBar;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.InfuseStorage;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
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
    private GuiInfuseBar infuseBar;

    public MetallurgicInfuserRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/metallurgic_infuser.png", MekanismBlock.METALLURGIC_INFUSER, ProgressBar.MEDIUM, 5, 16, 166, 54);
    }

    public static List<ItemStack> getInfuseStacks(InfuseType type) {
        return InfuseRegistry.getObjectMap().entrySet().stream().filter(obj -> obj.getValue().type == type).map(Entry::getKey).collect(Collectors.toList());
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(new GuiSlot(SlotType.EXTRA, this, guiLocation, 16, 34));
        guiElements.add(new GuiSlot(SlotType.INPUT, this, guiLocation, 50, 42));
        guiElements.add(new GuiSlot(SlotType.POWER, this, guiLocation, 142, 34).with(SlotOverlay.POWER));
        guiElements.add(new GuiSlot(SlotType.OUTPUT, this, guiLocation, 108, 42));
        guiElements.add(new GuiPowerBar(this, () -> 1F, guiLocation, 164, 15));
        guiElements.add(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return (double) timer.getValue() / 20F;
            }
        }, ProgressBar.MEDIUM, this, guiLocation, 70, 46));
        guiElements.add(infuseBar = new GuiInfuseBar(this, infoProvider = new RecipeInfuseInfoProvider(), guiLocation, 7, 15));
    }

    @Override
    public Class<? extends MetallurgicInfuserRecipe> getRecipeClass() {
        return MetallurgicInfuserRecipe.class;
    }

    @Override
    public void setIngredients(MetallurgicInfuserRecipe recipe, IIngredients ingredients) {
        List<ItemStack> inputStacks = Collections.singletonList(recipe.recipeInput.inputStack);
        List<ItemStack> infuseStacks = MetallurgicInfuserRecipeCategory.getInfuseStacks(recipe.getInput().infuse.getType());
        ingredients.setInput(VanillaTypes.ITEM, recipe.recipeInput.inputStack);
        ingredients.setInputLists(VanillaTypes.ITEM, Arrays.asList(inputStacks, infuseStacks));
        ingredients.setOutput(VanillaTypes.ITEM, recipe.recipeOutput.output);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, MetallurgicInfuserRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 45, 26);
        itemStacks.init(1, false, 103, 26);
        itemStacks.init(2, true, 11, 18);
        itemStacks.set(0, recipe.getInput().inputStack);
        itemStacks.set(1, recipe.getOutput().output);
        itemStacks.set(2, getInfuseStacks(recipe.getInput().infuse.getType()));
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
            return cachedRecipe == null ? null : cachedRecipe.getInput().infuse.getType().sprite;
        }

        @Override
        public ITextComponent getTooltip() {
            if (cachedRecipe != null) {
                InfuseStorage storage = cachedRecipe.getInput().infuse;
                InfuseType type = storage.getType();
                if (type != null) {
                    return TextComponentUtil.build(type, ": " + storage.getAmount());
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