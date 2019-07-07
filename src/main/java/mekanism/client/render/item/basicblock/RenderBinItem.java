package mekanism.client.render.item.basicblock;

import javax.annotation.Nonnull;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBinItem {

    public static void renderStack(@Nonnull ItemStack stack, TransformType transformType) {
        //TODO: The idea of this is to render the bin's contents onto the bin
        //TODO: Implement it
        /*GlStateManager.pushMatrix();
        ItemBlockBasic itemBasic = (ItemBlockBasic) stack.getItem();
        InventoryBin inv = new InventoryBin(stack);
        binRenderer.render(EnumFacing.NORTH, inv.getItemType(), inv.getItemCount(), false, -0.5, -0.5, -0.5);
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();*/
    }
}