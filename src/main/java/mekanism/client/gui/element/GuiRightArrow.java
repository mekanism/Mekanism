package mekanism.client.gui.element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.client.jei.interfaces.IJEIRecipeArea;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.resources.ResourceLocation;

public class GuiRightArrow extends GuiTextureOnlyElement implements IJEIRecipeArea<GuiRightArrow> {

    private static final ResourceLocation ARROW = MekanismUtils.getResource(ResourceType.GUI, "right_arrow.png");

    private MekanismJEIRecipeType<?>[] recipeCategories;

    public GuiRightArrow(IGuiWrapper gui, int x, int y) {
        super(ARROW, gui, x, y, 22, 15);
    }

    @Nonnull
    @Override
    public GuiRightArrow jeiCategories(@Nonnull MekanismJEIRecipeType<?>... recipeCategories) {
        this.recipeCategories = recipeCategories;
        return this;
    }

    @Nullable
    @Override
    public MekanismJEIRecipeType<?>[] getRecipeCategories() {
        return recipeCategories;
    }
}