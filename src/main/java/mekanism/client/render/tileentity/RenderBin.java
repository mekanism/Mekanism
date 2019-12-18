package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

public class RenderBin extends MekanismTileEntityRenderer<TileEntityBin> {

    @Override
    public void func_225616_a_(@Nonnull TileEntityBin tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        Coord4D obj = Coord4D.get(tile).offset(tile.getDirection());
        if (!Block.hasSolidSide(tile.getWorld().getBlockState(obj.getPos()), tile.getWorld(), obj.getPos(), tile.getOppositeDirection())) {
            render(tile.getDirection(), tile.clientStack, true, x, y, z);
        }
    }

    public void render(Direction facing, ItemStack clientStack, boolean text, double x, double y, double z) {
        if (!clientStack.isEmpty()) {
            int clientAmount = clientStack.getCount();
            String amount = Integer.toString(clientAmount);
            if (clientAmount == Integer.MAX_VALUE) {
                amount = TextComponentUtil.translate("gui.mekanism.infinite").getFormattedText();
            }
            setLightmapDisabled(true);
            RenderSystem.pushMatrix();
            switch (facing) {
                case NORTH:
                    RenderSystem.translatef((float) x + 0.73F, (float) y + 0.83F, (float) z - 0.0001F);
                    break;
                case SOUTH:
                    RenderSystem.translatef((float) x + 0.27F, (float) y + 0.83F, (float) z + 1.0001F);
                    RenderSystem.rotatef(180, 0, 1, 0);
                    break;
                case WEST:
                    RenderSystem.translatef((float) x - 0.0001F, (float) y + 0.83F, (float) z + 0.27F);
                    RenderSystem.rotatef(90, 0, 1, 0);
                    break;
                case EAST:
                    RenderSystem.translatef((float) x + 1.0001F, (float) y + 0.83F, (float) z + 0.73F);
                    RenderSystem.rotatef(-90, 0, 1, 0);
                    break;
                default:
                    break;
            }

            float scale = 0.03125F;
            float scaler = 0.9F;
            RenderSystem.scalef(scale * scaler, scale * scaler, -0.0001F);
            RenderSystem.rotatef(180, 0, 0, 1);
            Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(clientStack, 0, 0);
            RenderSystem.popMatrix();
            if (text) {
                renderText(amount, facing, 0.02F, x, y - 0.3725F, z);
            }
            setLightmapDisabled(false);
        }
    }

    @SuppressWarnings("incomplete-switch")
    private void renderText(String text, Direction side, float maxScale, double x, double y, double z) {
        RenderSystem.pushMatrix();
        RenderSystem.polygonOffset(-10, -10);
        RenderSystem.enablePolygonOffset();
        float displayWidth = 1;
        float displayHeight = 1;
        RenderSystem.translatef((float) x, (float) y, (float) z);

        switch (side) {
            case SOUTH:
                RenderSystem.translatef(0, 1, 0);
                RenderSystem.rotatef(0, 0, 1, 0);
                RenderSystem.rotatef(90, 1, 0, 0);
                break;
            case NORTH:
                RenderSystem.translatef(1, 1, 1);
                RenderSystem.rotatef(180, 0, 1, 0);
                RenderSystem.rotatef(90, 1, 0, 0);
                break;
            case EAST:
                RenderSystem.translatef(0, 1, 1);
                RenderSystem.rotatef(90, 0, 1, 0);
                RenderSystem.rotatef(90, 1, 0, 0);
                break;
            case WEST:
                RenderSystem.translatef(1, 1, 0);
                RenderSystem.rotatef(-90, 0, 1, 0);
                RenderSystem.rotatef(90, 1, 0, 0);
                break;
        }

        RenderSystem.translatef(displayWidth / 2, 1F, displayHeight / 2);
        RenderSystem.rotatef(-90, 1, 0, 0);

        FontRenderer font = field_228858_b_.getFontRenderer();

        int requiredWidth = Math.max(font.getStringWidth(text), 1);
        int requiredHeight = font.FONT_HEIGHT + 2;
        float scaler = 0.4F;
        float scaleX = displayWidth / requiredWidth;
        float scale = scaleX * scaler;
        if (maxScale > 0) {
            scale = Math.min(scale, maxScale);
        }

        RenderSystem.scalef(scale, -scale, scale);
        RenderSystem.depthMask(false);
        int realHeight = (int) Math.floor(displayHeight / scale);
        int realWidth = (int) Math.floor(displayWidth / scale);
        int offsetX = (realWidth - requiredWidth) / 2;
        int offsetY = (realHeight - requiredHeight) / 2;
        RenderSystem.disableLighting();
        font.drawString("\u00a7f" + text, offsetX - (realWidth / 2), 1 + offsetY - (realHeight / 2), 1);
        RenderSystem.enableLighting();
        RenderSystem.depthMask(true);
        RenderSystem.disablePolygonOffset();
        RenderSystem.popMatrix();
    }
}