package mekanism.client.gui.element;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerRecipeArea;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.Mekanism;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public class GuiInnerScreen extends GuiScalableElement implements IRecipeViewerRecipeArea<GuiInnerScreen> {

    public static final Identifier SCREEN = Mekanism.rl("inner_screen");

    private List<Component> lastInfo = Collections.emptyList();
    @Nullable
    private Tooltip lastTooltip;

    @Nullable
    private Supplier<List<Component>> renderStrings;
    @Nullable
    private Supplier<List<Component>> tooltipStrings;

    private IRecipeViewerRecipeType<?> @Nullable [] recipeCategories;
    private TextAlignment textAlignment = TextAlignment.LEFT;
    private VerticalPositioning verticalAlignment = VerticalPositioning.TOP;
    private int spacing;
    private int padding = 3;
    private float textScale = 1.0F;

    public GuiInnerScreen(IGuiWrapper gui, int x, int y, int width, int height) {
        super(SCREEN, gui, x, y, width, height);
    }

    public GuiInnerScreen(IGuiWrapper gui, int x, int y, int width, int height, Supplier<List<Component>> renderStrings) {
        this(gui, x, y, width, height);
        this.renderStrings = renderStrings;
        defaultFormat();
    }

    public GuiInnerScreen tooltip(Supplier<List<Component>> tooltipStrings) {
        this.tooltipStrings = tooltipStrings;
        active = true;
        return this;
    }

    public GuiInnerScreen text(Supplier<List<Component>> renderStrings) {
        this.renderStrings = renderStrings;
        return this;
    }

    public GuiInnerScreen spacing(int spacing) {
        this.spacing = spacing;
        return this;
    }

    public GuiInnerScreen alignment(TextAlignment alignment) {
        this.textAlignment = alignment;
        return this;
    }

    public GuiInnerScreen verticalAlignment(VerticalPositioning alignment) {
        this.verticalAlignment = alignment;
        return this;
    }

    public GuiInnerScreen clearSpacing() {
        return spacing(0);
    }

    public GuiInnerScreen padding(int padding) {
        this.padding = padding;
        return this;
    }

    public GuiInnerScreen clearScale() {
        return textScale(1);
    }

    public GuiInnerScreen textScale(float textScale) {
        this.textScale = textScale;
        return this;
    }

    public GuiInnerScreen clearFormat() {
        verticalAlignment = VerticalPositioning.TOP;
        return this;
    }

    public GuiInnerScreen defaultFormat() {
        return padding(5).spacing(2).textScale(0.8F).verticalAlignment(VerticalPositioning.CENTERED);
    }

    protected List<Component> getRenderStrings() {
        return renderStrings == null ? Collections.emptyList() : renderStrings.get();
    }

    @Override
    public void renderForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        List<Component> list = getRenderStrings();
        if (!list.isEmpty()) {
            int lineHeight = font().lineHeight;
            int minY = relativeY + padding;
            int maxY = minY + lineHeight;
            int heightToNextLine = lineHeight + spacing;
            if (verticalAlignment == VerticalPositioning.CENTERED) {
                int totalHeight = heightToNextLine * list.size() - spacing;
                float center = (getHeight() - totalHeight) / 2F;
                //If center is not evenly divisible, this will make it so that when we divide to find the target y in scrolling string
                // it gets the correct position
                minY = relativeY + Mth.floor(center);
                maxY = relativeY + lineHeight + Mth.ceil(center);
            } else if (verticalAlignment == VerticalPositioning.BOTTOM) {
                int totalHeight = heightToNextLine * list.size() - spacing;
                minY = getRelativeBottom() - padding - totalHeight;
                maxY = minY + lineHeight;
            }
            int minX = relativeX + padding;
            int screenTextColor = screenTextColor();
            for (int i = 0, size = list.size(); i < size; i++) {
                Component text = list.get(i);
                int maxX = relativeX + getMaxTextWidth(i) - padding;
                drawScaledScrollingString(guiGraphics, text, minX, minY, maxX, maxY, textAlignment, screenTextColor, false, textScale, getTimeOpened());
                minY += heightToNextLine;
                maxY += heightToNextLine;
            }
        }
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        if (tooltipStrings != null) {
            List<Component> list = tooltipStrings.get();
            if (!list.equals(lastInfo)) {
                lastInfo = list;
                lastTooltip = TooltipUtils.create(list);
            }
        } else {
            lastInfo = Collections.emptyList();
            lastTooltip = null;
        }
        setTooltip(lastTooltip);
    }

    protected int getMaxTextWidth(int row) {
        return getWidth();
    }

    @Override
    public GuiInnerScreen recipeViewerCategories(IRecipeViewerRecipeType<?>... recipeCategories) {
        this.recipeCategories = recipeCategories;
        return this;
    }

    @Override
    public IRecipeViewerRecipeType<?> @Nullable [] getRecipeCategories() {
        return recipeCategories;
    }

    @Override
    public boolean isMouseOverRecipeViewerArea(double mouseX, double mouseY) {
        //Override as active is occasionally false here so isMouseOver would return false
        return visible && mouseX >= getX() && mouseY >= getY() && mouseX < getRight() && mouseY < getBottom();
    }

    public enum VerticalPositioning {
        CENTERED,
        TOP,
        BOTTOM
    }
}