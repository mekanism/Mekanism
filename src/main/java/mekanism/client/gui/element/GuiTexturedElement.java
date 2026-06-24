package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerRecipeArea;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.Mekanism;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class GuiTexturedElement extends GuiElement implements IRecipeViewerRecipeArea<GuiTexturedElement> {

    private static final Identifier DOWN_ARROW = Mekanism.rl("arrow/down");
    private static final Identifier UP_ARROW = Mekanism.rl("arrow/up");
    private static final Identifier RIGHT_ARROW = Mekanism.rl("arrow/right");

    public static GuiTexturedElement textureOnly(Identifier texture, IGuiWrapper gui, int x, int y, int width, int height) {
        GuiTexturedElement element = new GuiTexturedElement(texture, gui, x, y, width, height);
        element.active = false;
        return element;
    }

    public static GuiTexturedElement upArrow(IGuiWrapper gui, int x, int y) {
        return textureOnly(UP_ARROW, gui, x, y, 8, 10);
    }

    public static GuiTexturedElement downArrow(IGuiWrapper gui, int x, int y) {
        return textureOnly(DOWN_ARROW, gui, x, y, 8, 9);
    }

    public static GuiTexturedElement rightArrow(IGuiWrapper gui, int x, int y) {
        return textureOnly(RIGHT_ARROW, gui, x, y, 22, 15);
    }

    private final Identifier resource;

    private IRecipeViewerRecipeType<?> @Nullable [] recipeCategories;

    public GuiTexturedElement(Identifier resource, IGuiWrapper gui, int x, int y, int width, int height) {
        super(gui, x, y, width, height);
        this.resource = resource;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, getResource(), getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight());
    }

    protected Identifier getResource() {
        return resource;
    }

    @Override
    public GuiTexturedElement recipeViewerCategories(IRecipeViewerRecipeType<?>... recipeCategories) {
        this.recipeCategories = recipeCategories;
        return this;
    }

    @Override
    public IRecipeViewerRecipeType<?> @Nullable [] getRecipeCategories() {
        return recipeCategories;
    }
}