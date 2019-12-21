package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tier.BinTier;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class RenderBin extends MekanismTileEntityRenderer<TileEntityBin> {

    @Override
    public void func_225616_a_(@Nonnull TileEntityBin tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        Direction facing = tile.getDirection();
        //position of the block covering the front side
        BlockPos coverPos = tile.getPos().offset(facing);
        //if the bin has an item stack and the face isn't covered by a solid side
        if (!tile.clientStack.isEmpty() && !tile.getWorld().getBlockState(coverPos).func_224755_d(tile.getWorld(), coverPos, facing.getOpposite())) {
            String amount = tile.getTier() == BinTier.CREATIVE ? TextComponentUtil.translate("gui.mekanism.infinite").getFormattedText()
                                                               : Integer.toString(tile.clientStack.getCount());
            matrix.func_227860_a_();
            switch (facing) {
                case NORTH:
                    matrix.func_227861_a_(0.73F, 0.83F, -0.0001F);
                    break;
                case SOUTH:
                    matrix.func_227861_a_(0.27F, 0.83F, 1.0001F);
                    matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(180));
                    break;
                case WEST:
                    matrix.func_227861_a_(-0.0001F, 0.83F, 0.27F);
                    matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(90));
                    break;
                case EAST:
                    matrix.func_227861_a_(1.0001F, 0.83F, 0.73F);
                    matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(-90));
                    break;
                default:
                    break;
            }

            float scale = 0.03125F;
            float scaler = 0.9F;
            matrix.func_227862_a_(scale * scaler, scale * scaler, -0.0001F);
            matrix.func_227863_a_(Vector3f.field_229183_f_.func_229187_a_(180));
            matrix.func_227861_a_(8, 8, 3);
            matrix.func_227862_a_(16, -16, 16);
            //TODO: The lighting seems a bit off but it is close enough for now
            Minecraft.getInstance().getItemRenderer().func_229110_a_(tile.clientStack, TransformType.GUI, MekanismRenderer.FULL_LIGHT, otherLight, matrix, renderer);
            matrix.func_227865_b_();
            renderText(matrix, renderer, otherLight, amount, facing, 0.02F);
        }
    }

    @SuppressWarnings("incomplete-switch")
    private void renderText(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int otherLight, String text, Direction side, float maxScale) {
        matrix.func_227860_a_();
        matrix.func_227861_a_(0, -0.3725F, 0);
        switch (side) {
            case SOUTH:
                matrix.func_227861_a_(0, 1, 0);
                matrix.func_227863_a_(Vector3f.field_229179_b_.func_229187_a_(90));
                break;
            case NORTH:
                matrix.func_227861_a_(1, 1, 1);
                matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(180));
                matrix.func_227863_a_(Vector3f.field_229179_b_.func_229187_a_(90));
                break;
            case EAST:
                matrix.func_227861_a_(0, 1, 1);
                matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(90));
                matrix.func_227863_a_(Vector3f.field_229179_b_.func_229187_a_(90));
                break;
            case WEST:
                matrix.func_227861_a_(1, 1, 0);
                matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(-90));
                matrix.func_227863_a_(Vector3f.field_229179_b_.func_229187_a_(90));
                break;
        }

        float displayWidth = 1;
        float displayHeight = 1;
        matrix.func_227861_a_(displayWidth / 2, 1F, displayHeight / 2);
        matrix.func_227863_a_(Vector3f.field_229179_b_.func_229187_a_(-90));

        FontRenderer font = field_228858_b_.getFontRenderer();

        int requiredWidth = Math.max(font.getStringWidth(text), 1);
        int requiredHeight = font.FONT_HEIGHT + 2;
        float scaler = 0.4F;
        float scaleX = displayWidth / requiredWidth;
        float scale = scaleX * scaler;
        if (maxScale > 0) {
            scale = Math.min(scale, maxScale);
        }

        matrix.func_227862_a_(scale, -scale, scale);
        int realHeight = (int) Math.floor(displayHeight / scale);
        int realWidth = (int) Math.floor(displayWidth / scale);
        int offsetX = (realWidth - requiredWidth) / 2;
        int offsetY = (realHeight - requiredHeight) / 2;
        //font.drawString("\u00a7f" + text, offsetX - (realWidth / 2), 1 + offsetY - (realHeight / 2), 1);
        font.func_228079_a_("\u00a7f" + text, offsetX - realWidth / 2, 1 + offsetY - realHeight / 2, otherLight,
              false, matrix.func_227866_c_().func_227870_a_(), renderer, false, 0, MekanismRenderer.FULL_LIGHT);
        matrix.func_227865_b_();
    }
}