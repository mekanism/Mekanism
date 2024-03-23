package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerRecipeArea;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiRightArrow extends GuiTextureOnlyElement implements IRecipeViewerRecipeArea<GuiRightArrow> {

    private static final ResourceLocation ARROW = MekanismUtils.getResource(ResourceType.GUI, "right_arrow.png");

    private IRecipeViewerRecipeType<?>[] recipeCategories;

    public GuiRightArrow(IGuiWrapper gui, int x, int y) {
        super(ARROW, gui, x, y, 22, 15);
    }

    @NotNull
    @Override
    public GuiRightArrow recipeViewerCategories(@NotNull IRecipeViewerRecipeType<?>... recipeCategories) {
        this.recipeCategories = recipeCategories;
        return this;
    }

    @Nullable
    @Override
    public IRecipeViewerRecipeType<?>[] getRecipeCategories() {
        return recipeCategories;
    }
}