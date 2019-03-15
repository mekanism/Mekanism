package mekanism.client.render.item.basicblock;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBinItem {

    public static void renderStack() {
        //TODO: The idea of this is to render the bin's contents onto the bin
        //TODO: Implement it
        /*GlStateManager.pushMatrix();
        ItemBlockBasic itemBasic = (ItemBlockBasic)stack.getItem();
        InventoryBin inv = new InventoryBin(stack);
        binRenderer.render(EnumFacing.NORTH, inv.getItemType(), inv.getItemCount(), false, -0.5, -0.5, -0.5);
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();*/
    }
}