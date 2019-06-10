package mekanism.client.render.tileentity;

import mekanism.api.Coord4D;
import mekanism.client.render.GLSMHelper;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.util.LangUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBin extends TileEntitySpecialRenderer<TileEntityBin> {

    private final RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

    @Override
    public void render(TileEntityBin tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        Coord4D obj = Coord4D.get(tileEntity).offset(tileEntity.facing);
        if (!obj.getBlockState(tileEntity.getWorld()).isSideSolid(tileEntity.getWorld(), obj.getPos(), tileEntity.facing.getOpposite())) {
            render(tileEntity.facing, tileEntity.itemType, tileEntity.clientAmount, true, x, y, z);
        }
    }

    public void render(EnumFacing facing, ItemStack itemType, int clientAmount, boolean text, double x, double y, double z) {
        String amount = "";
        if (!itemType.isEmpty()) {
            amount = Integer.toString(clientAmount);
            if (clientAmount == Integer.MAX_VALUE) {
                amount = LangUtils.localize("gui.infinite");
            }
            GlStateManager.pushMatrix();
            switch (facing) {
                case NORTH:
                    GLSMHelper.INSTANCE.translate(x + 0.73, y + 0.83, z - 0.0001);
                    break;
                case SOUTH:
                    GLSMHelper.INSTANCE.translate(x + 0.27, y + 0.83, z + 1.0001).rotateY(180, 1);
                    break;
                case WEST:
                    GLSMHelper.INSTANCE.translate(x - 0.0001, y + 0.83, z + 0.27).rotateY(90, 1);
                    break;
                case EAST:
                    GLSMHelper.INSTANCE.translate(x + 1.0001, y + 0.83, z + 0.73).rotateY(-90, 1);
                    break;
                default:
                    break;
            }

            float scale = 0.03125F;
            float scaler = 0.9F;
            GLSMHelper.INSTANCE.scale(scale * scaler, scale * scaler, -0.0001F).rotateZ(180, 1);
            renderItem.renderItemAndEffectIntoGUI(itemType, 0, 0);
            GlStateManager.popMatrix();
        }
        if (text && !amount.equals("")) {
            renderText(amount, facing, 0.02F, x, y - 0.3725F, z);
        }
    }

    @SuppressWarnings("incomplete-switch")
    private void renderText(String text, EnumFacing side, float maxScale, double x, double y, double z) {
        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true);
        GlStateManager.doPolygonOffset(-10, -10);
        renderHelper.enablePolygonOffset();
        float displayWidth = 1;
        float displayHeight = 1;
        renderHelper.translate(x, y, z);

        switch (side) {
            case SOUTH:
                renderHelper.translateY(1).rotateY(0, 1).rotateX(90, 1);
                break;
            case NORTH:
                renderHelper.translateAll(1).rotateY(180, 1).rotateX(90, 1);
                break;
            case EAST:
                renderHelper.translateYZ(1, 1).rotateY(90, 1).rotateX(90, 1);
                break;
            case WEST:
                renderHelper.translateXY(1, 1).rotateY(-90, 1).rotateX(90, 1);
                break;
        }

        renderHelper.translate(displayWidth / 2, 1F, displayHeight / 2).rotateX(-90, 1);

        FontRenderer fontRenderer = getFontRenderer();

        int requiredWidth = Math.max(fontRenderer.getStringWidth(text), 1);
        int requiredHeight = fontRenderer.FONT_HEIGHT + 2;
        float scaler = 0.4F;
        float scaleX = displayWidth / requiredWidth;
        float scale = scaleX * scaler;
        if (maxScale > 0) {
            scale = Math.min(scale, maxScale);
        }

        renderHelper.scale(scale, -scale, scale).disableDepthMask();
        int realHeight = (int) Math.floor(displayHeight / scale);
        int realWidth = (int) Math.floor(displayWidth / scale);
        int offsetX = (realWidth - requiredWidth) / 2;
        int offsetY = (realHeight - requiredHeight) / 2;
        renderHelper.disableLighting();
        fontRenderer.drawString("\u00a7f" + text, offsetX - (realWidth / 2), 1 + offsetY - (realHeight / 2), 1);
        renderHelper.cleanup();
    }
}