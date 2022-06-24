package mekanism.client.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.client.jei.interfaces.IJEIRecipeArea;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiInnerScreen extends GuiScalableElement implements IJEIRecipeArea<GuiInnerScreen> {

    public static final ResourceLocation SCREEN = MekanismUtils.getResource(ResourceType.GUI, "inner_screen.png");
    public static int SCREEN_SIZE = 32;

    private Supplier<List<Component>> renderStrings;
    private Supplier<List<Component>> tooltipStrings;

    private MekanismJEIRecipeType<?>[] recipeCategories;
    private boolean centerY;
    private int spacing = 1;
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
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        if (renderStrings != null) {
            List<Component> list = renderStrings.get();
            float startY = relativeY + padding;
            if (centerY) {
                int listSize = list.size();
                int totalHeight = listSize * 8 + spacing * (listSize - 1);
                startY = relativeY + (getHeight() - totalHeight) / 2F;
            }
            for (Component text : renderStrings.get()) {
                drawText(matrix, text, relativeX + padding, startY);
                startY += 8 + spacing;
            }
        }
    }

    @Override
    public void renderToolTip(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        if (tooltipStrings != null) {
            List<Component> list = tooltipStrings.get();
            if (list != null && !list.isEmpty()) {
                displayTooltips(matrix, mouseX, mouseY, list);
            }
        }
    }

    private void drawText(PoseStack matrix, Component text, float x, float y) {
        drawScaledTextScaledBound(matrix, text, x, y, screenTextColor(), getWidth() - padding * 2, textScale);
    }

    @NotNull
    @Override
    public GuiInnerScreen jeiCategories(@NotNull MekanismJEIRecipeType<?>... recipeCategories) {
        this.recipeCategories = recipeCategories;
        return this;
    }

    @Nullable
    @Override
    public MekanismJEIRecipeType<?>[] getRecipeCategories() {
        return recipeCategories;
    }

    @Override
    public boolean isMouseOverJEIArea(double mouseX, double mouseY) {
        return visible && mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}