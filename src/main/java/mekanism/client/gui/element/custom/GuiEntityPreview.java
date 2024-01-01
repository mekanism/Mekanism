package mekanism.client.gui.element.custom;

import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class GuiEntityPreview extends GuiElement {

    private final Supplier<LivingEntity> preview;
    private final int scale;
    private final int border;
    private final int size;

    private boolean isDragging;
    private float rotation;

    public GuiEntityPreview(IGuiWrapper gui, int x, int y, int size, int border, Supplier<LivingEntity> preview) {
        this(gui, x, y, size, size, border, preview);
    }

    public GuiEntityPreview(IGuiWrapper gui, int x, int y, int width, int height, int border, Supplier<LivingEntity> preview) {
        super(gui, x, y, width, height);
        this.border = border;
        this.size = Math.min(this.width, this.height);
        this.scale = (this.size - 2 * this.border) / 2;
        this.preview = preview;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        renderBackgroundTexture(guiGraphics, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE, GuiInnerScreen.SCREEN_SIZE);
    }

    // renderEntityInInventoryFollowsAngle(
    //      GuiGraphics p_282802_,
    //      int pX,
    //      int pY,
    //      int pScale,
    //      int p_294406_, ??
    //      int p_294663_, ??
    //      float pMouseX,
    //      float angleXComponent,
    //      float angleYComponent,
    //      LivingEntity p_275689_
    //   )
    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        InventoryScreen.renderEntityInInventoryFollowsAngle(guiGraphics, relativeX + width / 2, relativeY + height - 2 - border - (height - size) / 2,
              scale, /* TODO - 1.20.2 work out what these 3 are (mouseX guessed from what param was mapped in .1) */0, 0, mouseX, rotation, 0, preview.get());
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        isDragging = true;
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        isDragging = false;
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        super.onDrag(mouseX, mouseY, deltaX, deltaY);
        if (isDragging) {
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