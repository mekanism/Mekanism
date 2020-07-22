package mekanism.client.gui.element.scroll;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.IntSupplier;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiScrollBar extends GuiScrollableElement {

    private static final ResourceLocation BAR = MekanismUtils.getResource(ResourceType.GUI, "scroll_bar.png");
    private static final int TEXTURE_WIDTH = 24;
    private static final int TEXTURE_HEIGHT = 15;

    private final GuiElementHolder holder;
    private final IntSupplier maxElements;
    private final IntSupplier focusedElements;

    public GuiScrollBar(IGuiWrapper gui, int x, int y, int height, IntSupplier maxElements, IntSupplier focusedElements) {
        super(BAR, gui, x, y, 14, height, 1, 1, TEXTURE_WIDTH / 2, TEXTURE_HEIGHT, height - 2);
        holder = new GuiElementHolder(gui, x, y, barWidth + 2, height);
        this.maxElements = maxElements;
        this.focusedElements = focusedElements;
    }

    @Override
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        //Draw background and border
        holder.renderButton(matrix, mouseX, mouseY, partialTicks);
        holder.drawBackground(matrix, mouseX, mouseY, partialTicks);
        GuiElement.minecraft.textureManager.bindTexture(getResource());
        blit(matrix, barX, barY + getScroll(), needsScrollBars() ? 0 : barWidth, 0, barWidth, barHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    protected int getMaxElements() {
        return maxElements.getAsInt();
    }

    @Override
    protected int getFocusedElements() {
        return focusedElements.getAsInt();
    }
}