package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.tier.BinTier;
import mekanism.common.tile.TileEntityBin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class RenderBin extends TileEntityRenderer<TileEntityBin> {

    public RenderBin(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void render(@Nonnull TileEntityBin tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight) {
        Direction facing = tile.getDirection();
        //position of the block covering the front side
        BlockPos coverPos = tile.getPos().offset(facing);
        //if the bin has an item stack and the face isn't covered by a solid side
        if (!tile.clientStack.isEmpty() && !tile.getWorld().getBlockState(coverPos).func_224755_d(tile.getWorld(), coverPos, facing.getOpposite())) {
            String amount = tile.getTier() == BinTier.CREATIVE ? MekanismLang.INFINITE.translate().getFormattedText()
                                                               : Integer.toString(tile.clientStack.getCount());
            matrix.push();
            switch (facing) {
                case NORTH:
                    matrix.translate(0.73, 0.83, -0.0001);
                    break;
                case SOUTH:
                    matrix.translate(0.27, 0.83, 1.0001);
                    matrix.rotate(Vector3f.field_229181_d_.func_229187_a_(180));
                    break;
                case WEST:
                    matrix.translate(-0.0001, 0.83, 0.27);
                    matrix.rotate(Vector3f.field_229181_d_.func_229187_a_(90));
                    break;
                case EAST:
                    matrix.translate(1.0001, 0.83, 0.73);
                    matrix.rotate(Vector3f.field_229181_d_.func_229187_a_(-90));
                    break;
                default:
                    break;
            }

            float scale = 0.03125F;
            float scaler = 0.9F;
            matrix.scale(scale * scaler, scale * scaler, -0.0001F);
            matrix.rotate(Vector3f.field_229183_f_.func_229187_a_(180));
            matrix.translate(8, 8, 3);
            matrix.scale(16, -16, 16);
            //TODO: The lighting seems a bit off but it is close enough for now
            Minecraft.getInstance().getItemRenderer().renderItem(tile.clientStack, TransformType.GUI, MekanismRenderer.FULL_LIGHT, overlayLight, matrix, renderer);
            matrix.pop();
            renderText(matrix, renderer, overlayLight, amount, facing, 0.02F);
        }
    }

    @SuppressWarnings("incomplete-switch")
    private void renderText(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int overlayLight, String text, Direction side, float maxScale) {
        matrix.push();
        matrix.translate(0, -0.3725, 0);
        switch (side) {
            case SOUTH:
                matrix.translate(0, 1, 0);
                matrix.rotate(Vector3f.field_229179_b_.func_229187_a_(90));
                break;
            case NORTH:
                matrix.translate(1, 1, 1);
                matrix.rotate(Vector3f.field_229181_d_.func_229187_a_(180));
                matrix.rotate(Vector3f.field_229179_b_.func_229187_a_(90));
                break;
            case EAST:
                matrix.translate(0, 1, 1);
                matrix.rotate(Vector3f.field_229181_d_.func_229187_a_(90));
                matrix.rotate(Vector3f.field_229179_b_.func_229187_a_(90));
                break;
            case WEST:
                matrix.translate(1, 1, 0);
                matrix.rotate(Vector3f.field_229181_d_.func_229187_a_(-90));
                matrix.rotate(Vector3f.field_229179_b_.func_229187_a_(90));
                break;
        }

        float displayWidth = 1;
        float displayHeight = 1;
        matrix.translate(displayWidth / 2, 1, displayHeight / 2);
        matrix.rotate(Vector3f.field_229179_b_.func_229187_a_(-90));

        FontRenderer font = renderDispatcher.getFontRenderer();

        int requiredWidth = Math.max(font.getStringWidth(text), 1);
        int requiredHeight = font.FONT_HEIGHT + 2;
        float scaler = 0.4F;
        float scaleX = displayWidth / requiredWidth;
        float scale = scaleX * scaler;
        if (maxScale > 0) {
            scale = Math.min(scale, maxScale);
        }

        matrix.scale(scale, -scale, scale);
        int realHeight = (int) Math.floor(displayHeight / scale);
        int realWidth = (int) Math.floor(displayWidth / scale);
        int offsetX = (realWidth - requiredWidth) / 2;
        int offsetY = (realHeight - requiredHeight) / 2;
        //font.drawString("\u00a7f" + text, offsetX - (realWidth / 2), 1 + offsetY - (realHeight / 2), 1);
        font.renderString("\u00a7f" + text, offsetX - realWidth / 2, 1 + offsetY - realHeight / 2, overlayLight,
              false, matrix.getLast().getPositionMatrix(), renderer, false, 0, MekanismRenderer.FULL_LIGHT);
        matrix.pop();
    }
}