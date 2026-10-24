package mekanism.client.gui.element.window;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.recipe_book.VirtualRecipeBookComponent;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.SelectedWindowData;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.PreeditEvent;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

public class GuiRecipeBookWindow extends GuiWindow {

    private static final int Y_OFFSET = 10;
    //Account for the width of the tabs
    public static final int WIDTH_WITH_TABS = RecipeBookComponent.IMAGE_WIDTH + 30;
    public static final int HEIGHT = RecipeBookComponent.IMAGE_HEIGHT + Y_OFFSET;

    private final VirtualRecipeBookComponent<?> recipeBook;
    private final boolean isPrecisionSawmill;

    public GuiRecipeBookWindow(IGuiWrapper gui, int x, int y, VirtualRecipeBookComponent<?> recipeBook, boolean isPrecisionSawmill, SelectedWindowData windowData) {
        this.isPrecisionSawmill = isPrecisionSawmill;
        super(gui, x, y, RecipeBookComponent.IMAGE_WIDTH, HEIGHT, windowData);
        interactionStrategy = InteractionStrategy.ALL;
        this.recipeBook = recipeBook;
        if (!isPinned() && this.recipeBook.isVisibleAccordingToBookData()) {
            //Ensure we are marked as pinned (this has to happen before we adopt the position as that sets it to being visible)
            togglePinned();
        }
        this.recipeBook.adoptPosition(getX(), getY() + Y_OFFSET);
    }

    @Override
    protected boolean canPin() {
        return true;
    }

    @Override
    public void resize(int prevLeft, int prevTop, int left, int top) {
        super.resize(prevLeft, prevTop, left, top);
        recipeBook.move(left - prevLeft, top - prevTop);
    }

    @Override
    public void move(int changeX, int changeY) {
        super.move(changeX, changeY);
        recipeBook.move(changeX, changeY);
    }

    @Override
    public void renderForeground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(graphics, mouseX, mouseY, partialTicks);
        drawTitleText(graphics, MekanismLang.RECIPE_BOOK.translate(), 5);

        Matrix3x2fStack matrix = graphics.pose();
        matrix.pushMatrix();
        //Translate to the top left before attempting to render the text field as vanilla renders widgets from the top left
        matrix.translate(-getGuiLeft(), -getGuiTop());
        this.recipeBook.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        matrix.popMatrix();

        //Note: This normally happens during extractSlots so we don't need to shift it to the top left
        this.recipeBook.extractGhostRecipe(graphics, isPrecisionSawmill);
    }

    @Override
    public void close() {
        super.close();
        if (!isPinned()) {//Don't toggle it if it is pinned as then we want it to be staying open
            this.recipeBook.toggleVisibility();
        }
    }

    @Override
    public void renderToolTip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.renderToolTip(graphics, mouseX, mouseY);
        this.recipeBook.extractTooltip(graphics, mouseX, mouseY, null);
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return this.visible ? this.recipeBook.narrationPriority() : NarratableEntry.NarrationPriority.NONE;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        super.updateWidgetNarration(output);
        this.recipeBook.updateNarration(output);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return this.recipeBook.keyPressed(event) || super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        return this.recipeBook.keyReleased(event) || super.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return this.recipeBook.charTyped(event) || super.charTyped(event);
    }

    @Override
    public boolean preeditUpdated(@Nullable PreeditEvent event) {
        return this.recipeBook.preeditUpdated(event) || super.preeditUpdated(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.recipeBook.mouseClicked(event, doubleClick)) {
            setFocused(this.recipeBook);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        return this.recipeBook.mouseDragged(event, dx, dy) || super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return super.isMouseOver(mouseX, mouseY) || this.recipeBook.isMouseOverTab(mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();
        this.recipeBook.tick();
    }
}