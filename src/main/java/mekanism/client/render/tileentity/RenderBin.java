package mekanism.client.render.tileentity;

import mekanism.api.Coord4D;
import mekanism.common.tile.bin.TileEntityBin;
import mekanism.common.util.LangUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderBin extends TileEntityRenderer<TileEntityBin> {

    private final ItemRenderer renderItem = Minecraft.getInstance().getRenderItem();

    @Override
    public void render(TileEntityBin tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        Coord4D obj = Coord4D.get(tileEntity).offset(tileEntity.getDirection());
        if (!obj.getBlockState(tileEntity.getWorld()).isSideSolid(tileEntity.getWorld(), obj.getPos(), tileEntity.getOppositeDirection())) {
            render(tileEntity.getDirection(), tileEntity.itemType, tileEntity.clientAmount, true, x, y, z);
        }
    }

    public void render(Direction facing, ItemStack itemType, int clientAmount, boolean text, double x, double y, double z) {
        String amount = "";
        if (!itemType.isEmpty()) {
            amount = Integer.toString(clientAmount);
            if (clientAmount == Integer.MAX_VALUE) {
                amount = LangUtils.localize("gui.infinite");
            }
            GlStateManager.pushMatrix();
            switch (facing) {
                case NORTH:
                    GlStateManager.translatef((float) x + 0.73F, (float) y + 0.83F, (float) z - 0.0001F);
                    break;
                case SOUTH:
                    GlStateManager.translatef((float) x + 0.27F, (float) y + 0.83F, (float) z + 1.0001F);
                    GlStateManager.rotatef(180, 0, 1, 0);
                    break;
                case WEST:
                    GlStateManager.translatef((float) x - 0.0001F, (float) y + 0.83F, (float) z + 0.27F);
                    GlStateManager.rotatef(90, 0, 1, 0);
                    break;
                case EAST:
                    GlStateManager.translatef((float) x + 1.0001F, (float) y + 0.83F, (float) z + 0.73F);
                    GlStateManager.rotatef(-90, 0, 1, 0);
                    break;
                default:
                    break;
            }

            float scale = 0.03125F;
            float scaler = 0.9F;
            GlStateManager.translatef(scale * scaler, scale * scaler, -0.0001F);
            GlStateManager.rotatef(180, 0, 0, 1);
            renderItem.renderItemAndEffectIntoGUI(itemType, 0, 0);
            GlStateManager.popMatrix();
        }
        if (text && !amount.equals("")) {
            renderText(amount, facing, 0.02F, x, y - 0.3725F, z);
        }
    }

    @SuppressWarnings("incomplete-switch")
    private void renderText(String text, Direction side, float maxScale, double x, double y, double z) {
        GlStateManager.pushMatrix();
        GlStateManager.doPolygonOffset(-10, -10);
        GlStateManager.enablePolygonOffset();
        float displayWidth = 1;
        float displayHeight = 1;
        GlStateManager.translatef((float) x, (float) y, (float) z);

        switch (side) {
            case SOUTH:
                GlStateManager.translatef(0, 1, 0);
                GlStateManager.rotatef(0, 0, 1, 0);
                GlStateManager.rotatef(90, 1, 0, 0);
                break;
            case NORTH:
                GlStateManager.translatef(1, 1, 1);
                GlStateManager.rotatef(180, 0, 1, 0);
                GlStateManager.rotatef(90, 1, 0, 0);
                break;
            case EAST:
                GlStateManager.translatef(0, 1, 1);
                GlStateManager.rotatef(90, 0, 1, 0);
                GlStateManager.rotatef(90, 1, 0, 0);
                break;
            case WEST:
                GlStateManager.translatef(1, 1, 0);
                GlStateManager.rotatef(-90, 0, 1, 0);
                GlStateManager.rotatef(90, 1, 0, 0);
                break;
        }

        GlStateManager.translatef(displayWidth / 2, 1F, displayHeight / 2);
        GlStateManager.rotatef(-90, 1, 0, 0);

        FontRenderer font = getFontRenderer();

        int requiredWidth = Math.max(font.getStringWidth(text), 1);
        int requiredHeight = font.FONT_HEIGHT + 2;
        float scaler = 0.4F;
        float scaleX = displayWidth / requiredWidth;
        float scale = scaleX * scaler;
        if (maxScale > 0) {
            scale = Math.min(scale, maxScale);
        }

        GlStateManager.translatef(scale, -scale, scale);
        GlStateManager.depthMask(false);
        int realHeight = (int) Math.floor(displayHeight / scale);
        int realWidth = (int) Math.floor(displayWidth / scale);
        int offsetX = (realWidth - requiredWidth) / 2;
        int offsetY = (realHeight - requiredHeight) / 2;
        GlStateManager.disableLighting();
        font.drawString("\u00a7f" + text, offsetX - (realWidth / 2), 1 + offsetY - (realHeight / 2), 1);
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.disablePolygonOffset();
        GlStateManager.popMatrix();
    }
}