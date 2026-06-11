package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerRecipeArea;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class GuiRightArrow extends GuiTextureOnlyElement implements IRecipeViewerRecipeArea<GuiRightArrow> {

    private static final Identifier ARROW = MekanismUtils.getResource(ResourceType.GUI, "right_arrow.png");

    private IRecipeViewerRecipeType<?> @Nullable [] recipeCategories;

    public GuiRightArrow(IGuiWrapper gui, int x, int y) {
        super(ARROW, gui, x, y, 22, 15);
    }

    @Override
    public GuiRightArrow recipeViewerCategories(IRecipeViewerRecipeType<?>... recipeCategories) {
        this.recipeCategories = recipeCategories;
        return this;
    }

    @Override
    public IRecipeViewerRecipeType<?> @Nullable [] getRecipeCategories() {
        return recipeCategories;
    }
}