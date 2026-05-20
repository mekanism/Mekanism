package mekanism.client.gui.element.custom;

import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class GuiEntityPreview extends GuiElement {

    private static final Vector3f PREVIEW_TRANSLATION = new Vector3f();
    private static final Quaternionf PREVIEW_ANGLE = new Quaternionf().rotateZ(Mth.PI);

    private final Supplier<LivingEntity> preview;
    private final int scale;
    private final float xOffset;
    private final float yOffset;

    private float rotation;

    public GuiEntityPreview(IGuiWrapper gui, int x, int y, int size, Supplier<LivingEntity> preview) {
        this(gui, x, y, size, size, preview);
    }

    public GuiEntityPreview(IGuiWrapper gui, int x, int y, int width, int height, Supplier<LivingEntity> preview) {
        super(gui, x, y, width, height);
        int size = Math.min(this.width, this.height);
        this.scale = size / 2;
        this.xOffset = this.width / 2F;
        this.yOffset = this.height - 2 - (this.height - size) / 2F;
        this.preview = preview;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, GuiInnerScreen.SCREEN, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight());
    }

    @Override
    public void renderForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        LivingEntity preview = this.preview.get();
        float oldBodyRot = preview.yBodyRot;
        float oldYRot = preview.getYRot();
        //Apply our rotation to the entity
        preview.yBodyRot = 180.0F + rotation * 20.0F;
        preview.setYRot(180.0F + rotation * 40.0F);
        //TODO - 26.1: InventoryScreen.renderEntityInInventory(guiGraphics, relativeX + xOffset, relativeY + yOffset, scale, PREVIEW_TRANSLATION, PREVIEW_ANGLE, null, preview);
        //Reset the values to what they were before we applied the rotation, even though our one use case doesn't actually care
        // as we only use the preview entity for rendering, so the correct rotation gets set every time before it is rendered
        preview.yBodyRot = oldBodyRot;
        preview.setYRot(oldYRot);
    }

    @Override
    public void onClick(@NotNull MouseButtonEvent event, boolean isDoubleClick) {
        super.onClick(event, isDoubleClick);
        setDragging(true);
    }

    @Override
    protected void onDrag(@NotNull MouseButtonEvent event, double deltaX, double deltaY) {
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