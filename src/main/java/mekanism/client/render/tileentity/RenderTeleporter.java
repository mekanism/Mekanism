package mekanism.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import java.util.HashMap;
import java.util.Map;
import mekanism.api.text.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.MekanismGases;
import mekanism.common.block.machine.BlockTeleporter;
import mekanism.common.tile.TileEntityTeleporter;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class RenderTeleporter extends TileEntityRenderer<TileEntityTeleporter> {

    private Map<Integer, DisplayInteger> cachedOverlays = new HashMap<>();

    @Override
    public void render(TileEntityTeleporter tileEntity, double x, double y, double z, float partialTick, int destroyStage) {
        //TODO: Figure out why it always renders in one direction even if the teleporter is assembled along the other axis
        if (tileEntity.shouldRender) {
            GlStateManager.pushMatrix();
            GlStateManager.enableCull();
            GlStateManager.disableLighting();
            GlowInfo glowInfo = MekanismRenderer.enableGlow();
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            GlStateManager.disableAlphaTest();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            MekanismRenderer.color(EnumColor.PURPLE, 0.75F);

            bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.translatef((float) x, (float) y, (float) z);
            BlockPos pos = tileEntity.getPos().west();
            int type = 0;
            BlockState s = tileEntity.getWorld().getBlockState(pos);
            if (s.getBlock() instanceof BlockTeleporter) {
                type = 1;
            }

            int display = getOverlayDisplay(type).display;
            GlStateManager.callList(display);

            MekanismRenderer.resetColor();
            GlStateManager.disableBlend();
            GlStateManager.enableAlphaTest();
            MekanismRenderer.disableGlow(glowInfo);
            GlStateManager.enableLighting();
            GlStateManager.disableCull();
            GlStateManager.popMatrix();
        }
    }

    private DisplayInteger getOverlayDisplay(Integer type) {
        if (cachedOverlays.containsKey(type)) {
            return cachedOverlays.get(type);
        }

        Model3D toReturn = new Model3D();
        toReturn.baseBlock = Blocks.STONE;
        toReturn.setTexture(MekanismRenderer.getChemicalTexture(MekanismGases.OXYGEN.getGas()));

        DisplayInteger display = DisplayInteger.createAndStart();
        //We already know it does not contain type, so add it
        cachedOverlays.put(type, display);

        switch (type) {
            case 0:
                toReturn.minY = 1;
                toReturn.maxY = 3;

                toReturn.minX = 0.46;
                toReturn.minZ = 0;
                toReturn.maxX = 0.54;
                toReturn.maxZ = 1;
                break;
            case 1:
                toReturn.minY = 1;
                toReturn.maxY = 3;

                toReturn.minX = 0;
                toReturn.minZ = 0.46;
                toReturn.maxX = 1;
                toReturn.maxZ = 0.54;
                break;
        }

        MekanismRenderer.renderObject(toReturn);
        GlStateManager.endList();

        return display;
    }

    @Override
    public boolean isGlobalRenderer(TileEntityTeleporter tile) {
        return true;
    }
}