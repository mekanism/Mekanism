package mekanism.client.gui.element.custom;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.function.Supplier;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
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
        GuiUtils.renderWithPose(matrix, () -> renderEntityInInventory(relativeX + width / 2, relativeY + height - 2 - border - (height - size) / 2, scale, rotation, 0, preview.get()));
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

    //Vanilla copy of InventoryScreen#renderEntityInInventory except without the extra clamping for the y rotation
    @SuppressWarnings({"deprecation", "UnnecessaryLocalVariable"})
    private static void renderEntityInInventory(int posX, int posY, int scale, float mouseX, float mouseY, LivingEntity livingEntity) {
        //Don't clamp the directions into the bounds of atan (mouseX is the important one, mouseY is always zero)
        // The only thing changed are these first two lines
        float f = mouseX;//(float)Math.atan((double)(mouseX / 40.0F));
        float f1 = mouseY;//(float)Math.atan((double)(mouseY / 40.0F));
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate(posX, posY, 1050.0D);
        posestack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack posestack1 = new PoseStack();
        posestack1.translate(0.0D, 0.0D, 1000.0D);
        posestack1.scale((float) scale, (float) scale, (float) scale);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(f1 * 20.0F);
        quaternion.mul(quaternion1);
        posestack1.mulPose(quaternion);
        float f2 = livingEntity.yBodyRot;
        float f3 = livingEntity.getYRot();
        float f4 = livingEntity.getXRot();
        float f5 = livingEntity.yHeadRotO;
        float f6 = livingEntity.yHeadRot;
        livingEntity.yBodyRot = 180.0F + f * 20.0F;
        livingEntity.setYRot(180.0F + f * 40.0F);
        livingEntity.setXRot(-f1 * 20.0F);
        livingEntity.yHeadRot = livingEntity.getYRot();
        livingEntity.yHeadRotO = livingEntity.getYRot();
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion1.conj();
        entityrenderdispatcher.overrideCameraOrientation(quaternion1);
        entityrenderdispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> entityrenderdispatcher.render(livingEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, posestack1, multibuffersource$buffersource, 15728880));
        multibuffersource$buffersource.endBatch();
        entityrenderdispatcher.setRenderShadow(true);
        livingEntity.yBodyRot = f2;
        livingEntity.setYRot(f3);
        livingEntity.setXRot(f4);
        livingEntity.yHeadRotO = f5;
        livingEntity.yHeadRot = f6;
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }
}