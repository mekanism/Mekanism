package mekanism.client.gui.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.jei.interfaces.IJEIRecipeArea;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiInnerScreen extends GuiScalableElement implements IJEIRecipeArea<GuiInnerScreen> {

    public static final ResourceLocation SCREEN = MekanismUtils.getResource(ResourceType.GUI, "inner_screen.png");

    private Supplier<List<ITextComponent>> renderStrings;
    private Supplier<List<ITextComponent>> tooltipStrings;

    private ResourceLocation[] recipeCategories;
    private boolean centerY;
    private int spacing = 1;
    private int padding = 3;
    private float textScale = 1.0F;

    public GuiInnerScreen(IGuiWrapper gui, int x, int y, int width, int height) {
        super(SCREEN, gui, x, y, width, height, 32, 32);
    }

    public GuiInnerScreen(IGuiWrapper gui, int x, int y, int width, int height, Supplier<List<ITextComponent>> renderStrings) {
        this(gui, x, y, width, height);
        this.renderStrings = renderStrings;
        defaultFormat();
    }

    public GuiInnerScreen tooltip(Supplier<List<ITextComponent>> tooltipStrings) {
        this.tooltipStrings = tooltipStrings;
        active = true;
        return this;
    }

    public GuiInnerScreen spacing(int spacing) {
        this.spacing = spacing;
        return this;
    }

    public GuiInnerScreen padding(int padding) {
        this.padding = padding;
        return this;
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
        return padding(5).spacing(3).textScale(0.8F).centerY();
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);

        if (renderStrings != null) {
            List<ITextComponent> list = renderStrings.get();
            float startY = relativeY + padding;
            if (centerY) {
                int totalHeight = list.size() * 8 + spacing * (list.size() - 1);
                startY = relativeY + getHeightRealms() / 2F - totalHeight / 2F;
            }
            for (ITextComponent text : renderStrings.get()) {
                drawText(matrix, text, relativeX + padding, startY);
                startY += 8 + spacing;
            }
        }
    }

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        if (tooltipStrings != null) {
            List<ITextComponent> list = tooltipStrings.get();
            if (list != null && !list.isEmpty()) {
                displayTooltips(matrix, list, mouseX, mouseY);
            }
        }
    }

    private void drawText(MatrixStack matrix, ITextComponent text, float x, float y) {
        drawScaledTextScaledBound(matrix, text, x, y, screenTextColor(), getWidth() - padding * 2, textScale);
    }

    @Nonnull
    @Override
    public GuiInnerScreen jeiCategories(@Nullable ResourceLocation... recipeCategories) {
        this.recipeCategories = recipeCategories;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation[] getRecipeCategories() {
        return recipeCategories;
    }

    @Override
    public boolean isMouseOverJEIArea(double mouseX, double mouseY) {
        return visible && mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}