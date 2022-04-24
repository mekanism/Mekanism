package mekanism.client.gui.element.scroll;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.IntSupplier;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.resources.ResourceLocation;

public class GuiScrollBar extends GuiScrollableElement {

    private static final ResourceLocation BAR = MekanismUtils.getResource(ResourceType.GUI, "scroll_bar.png");
    private static final int TEXTURE_WIDTH = 24;
    private static final int TEXTURE_HEIGHT = 15;

    private final IntSupplier maxElements;
    private final IntSupplier focusedElements;

    public GuiScrollBar(IGuiWrapper gui, int x, int y, int height, IntSupplier maxElements, IntSupplier focusedElements) {
        super(BAR, gui, x, y, 14, height, 1, 1, TEXTURE_WIDTH / 2, TEXTURE_HEIGHT, height - 2);
        this.maxElements = maxElements;
        this.focusedElements = focusedElements;
    }

    @Override
    public void drawBackground(@Nonnull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        //Draw background and border
        GuiUtils.renderBackgroundTexture(matrix, GuiElementHolder.HOLDER, GuiElementHolder.HOLDER_SIZE, GuiElementHolder.HOLDER_SIZE, getButtonX(), getButtonY(),
              barWidth + 2, getButtonHeight(), 256, 256);
        RenderSystem.setShaderTexture(0, getResource());
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