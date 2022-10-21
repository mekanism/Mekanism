package mekanism.client.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Supplier;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiArrowSelection extends GuiTexturedElement {

    private static final ResourceLocation ARROW = MekanismUtils.getResource(ResourceType.GUI, "arrow_selection.png");

    private final Supplier<Component> textComponentSupplier;

    public GuiArrowSelection(IGuiWrapper gui, int x, int y, Supplier<Component> textComponentSupplier) {
        super(ARROW, gui, x, y, 33, 19);
        this.textComponentSupplier = textComponentSupplier;
    }

    @Override
    public boolean isMouseOver(double xAxis, double yAxis) {
        //TODO: override isHovered
        return this.active && this.visible && xAxis >= x + 16 && xAxis < x + width - 1 && yAxis >= y + 1 && yAxis < y + height - 1;
    }

    @Override
    public void renderToolTip(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        Component component = textComponentSupplier.get();
        if (component != null) {
            int tooltipX = mouseX + 5;
            int tooltipY = mouseY - 5;
            GuiUtils.renderBackgroundTexture(matrix, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE, GuiInnerScreen.SCREEN_SIZE, tooltipX - 3, tooltipY - 4, getStringWidth(component) + 6, 16, 256, 256);
            matrix.pushPose();
            //Make sure the text is above other renders like JEI
            matrix.translate(0, 0, 300);
            drawString(matrix, component, tooltipX, tooltipY, screenTextColor());
            matrix.popPose();
        }
    }

    @Override
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        RenderSystem.setShaderTexture(0, getResource());
        blit(matrix, x, y, 0, 0, width, height, width, height);
    }
}