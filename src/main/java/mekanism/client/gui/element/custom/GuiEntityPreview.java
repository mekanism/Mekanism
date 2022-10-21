package mekanism.client.gui.element.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Supplier;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
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
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        renderBackgroundTexture(matrix, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE, GuiInnerScreen.SCREEN_SIZE);
    }

    @Override
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        GuiUtils.renderWithPose(matrix, () -> InventoryScreen.renderEntityInInventoryRaw(relativeX + width / 2, relativeY + height - 2 - border - (height - size) / 2,
              scale, rotation, 0, preview.get()));
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (clicked(mouseX, mouseY)) {
            isDragging = true;
        }
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
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isMouseOver(mouseX, mouseY)) {
            rotation = Mth.wrapDegrees(rotation + (float) delta);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}