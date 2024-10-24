package mekanism.client.gui.element;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerRecipeArea;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiInnerScreen extends GuiScalableElement implements IRecipeViewerRecipeArea<GuiInnerScreen> {

    public static final ResourceLocation SCREEN = MekanismUtils.getResource(ResourceType.GUI, "inner_screen.png");
    public static int SCREEN_SIZE = 32;

    private List<Component> lastInfo = Collections.emptyList();
    @Nullable
    private Tooltip lastTooltip;

    private Supplier<List<Component>> renderStrings;
    private Supplier<List<Component>> tooltipStrings;

    private IRecipeViewerRecipeType<?>[] recipeCategories;
    private boolean centerY;
    private int spacing;
    private int padding = 3;
    private float textScale = 1.0F;

    public GuiInnerScreen(IGuiWrapper gui, int x, int y, int width, int height) {
        super(SCREEN, gui, x, y, width, height, SCREEN_SIZE, SCREEN_SIZE);
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

    public GuiInnerScreen spacing(int spacing) {
        this.spacing = spacing;
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

    public GuiInnerScreen centerY() {
        centerY = true;
        return this;
    }

    public GuiInnerScreen clearFormat() {
        centerY = false;
        return this;
    }

    public GuiInnerScreen defaultFormat() {
        return padding(5).spacing(2).textScale(0.8F).centerY();
    }

    protected List<Component> getRenderStrings() {
        return renderStrings == null ? Collections.emptyList() : renderStrings.get();
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        List<Component> list = getRenderStrings();
        if (!list.isEmpty()) {
            int lineHeight = font().lineHeight;
            int minY = relativeY + padding;
            int maxY = minY + lineHeight;
            int heightToNextLine = lineHeight + spacing;
            if (centerY) {
                int totalHeight = heightToNextLine * list.size() - spacing;
                float center = (getHeight() - totalHeight) / 2F;
                //If center is not evenly divisible, this will make it so that when we divide to find the target y in scrolling string
                // it gets the correct position
                minY = relativeY + Mth.floor(center);
                maxY = relativeY + lineHeight + Mth.ceil(center);
            }
            int minX = relativeX + padding;
            int screenTextColor = screenTextColor();
            for (int i = 0, size = list.size(); i < size; i++) {
                Component text = list.get(i);
                int maxX = relativeX + getMaxTextWidth(i) - padding;
                drawScaledScrollingString(guiGraphics, text, minX, minY, maxX, maxY, TextAlignment.LEFT, screenTextColor, false, textScale, getTimeOpened());
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

    @NotNull
    @Override
    public GuiInnerScreen recipeViewerCategories(@NotNull IRecipeViewerRecipeType<?>... recipeCategories) {
        this.recipeCategories = recipeCategories;
        return this;
    }

    @Nullable
    @Override
    public IRecipeViewerRecipeType<?>[] getRecipeCategories() {
        return recipeCategories;
    }

    @Override
    public boolean isMouseOverRecipeViewerArea(double mouseX, double mouseY) {
        //Override as active is occasionally false here so isMouseOver would return false
        return visible && mouseX >= getX() && mouseY >= getY() && mouseX < getRight() && mouseY < getBottom();
    }
}