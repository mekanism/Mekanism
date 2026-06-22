package mekanism.client.gui.element.custom;

import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class GuiEntityPreview extends GuiElement {

    private static final Vector3f PREVIEW_TRANSLATION = new Vector3f(0, 1, 0);
    private static final Quaternionf PREVIEW_ANGLE = new Quaternionf().rotateZ(Mth.PI);

    private final Supplier<? extends LivingEntityRenderState> preview;
    private final int scale;

    private float rotation;

    public GuiEntityPreview(IGuiWrapper gui, int x, int y, int size, Supplier<? extends LivingEntityRenderState> preview) {
        this(gui, x, y, size, size, preview);
    }

    public GuiEntityPreview(IGuiWrapper gui, int x, int y, int width, int height, Supplier<? extends LivingEntityRenderState> preview) {
        super(gui, x, y, width, height);
        int size = Math.min(this.width, this.height);
        this.scale = size / 2;
        this.preview = preview;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, GuiInnerScreen.SCREEN, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight());
    }

    @Override
    public void renderForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        LivingEntityRenderState preview = this.preview.get();
        //Apply our rotation to the render state (copied from InventoryScreen#renderEntityInInventory)
        preview.bodyRot = Mth.wrapDegrees(180.0F + rotation * 20.0F);
        preview.yRot = Mth.wrapDegrees(rotation * 20.0F);
        preview.xRot = 0;
        guiGraphics.entity(preview, scale, PREVIEW_TRANSLATION, PREVIEW_ANGLE, null, getX(), getY(), getRight(), getBottom());
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        super.onClick(event, isDoubleClick);
        setDragging(true);
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double deltaX, double deltaY) {
        super.onDrag(event, deltaX, deltaY);
        if (isDragging()) {
            rotation = Mth.wrapDegrees(rotation - (float) (deltaX / 10));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (isMouseOver(mouseX, mouseY)) {
            rotation = Mth.wrapDegrees(rotation + (float) deltaY);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }
}