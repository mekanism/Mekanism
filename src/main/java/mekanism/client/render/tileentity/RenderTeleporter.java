package mekanism.client.render.tileentity;

import java.util.HashMap;
import java.util.Map;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismFluids;
import mekanism.common.tile.TileEntityTeleporter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;

public class RenderTeleporter extends TileEntitySpecialRenderer<TileEntityTeleporter> {

    private Map<Integer, DisplayInteger> cachedOverlays = new HashMap<>();

    @Override
    public void render(TileEntityTeleporter tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        if (tileEntity.shouldRender) {
            MekanismRenderHelper renderHelper = initHelper().color(EnumColor.PURPLE, 0.75F);

            bindTexture(MekanismRenderer.getBlocksTexture());
            GlStateManager.translate((float) x, (float) y, (float) z);
            Coord4D obj = Coord4D.get(tileEntity).offset(EnumFacing.WEST);
            int type = 0;
            IBlockState s = obj.getBlockState(tileEntity.getWorld());
            if (s.getBlock() == MekanismBlocks.BasicBlock && s.getBlock().getMetaFromState(s) == 7) {
                type = 1;
            }

            int display = getOverlayDisplay(type).display;
            GlStateManager.callList(display);
            renderHelper.cleanup();
        }
    }

    private MekanismRenderHelper initHelper() {
        return new MekanismRenderHelper(true).enableCull().disableLighting().enableGlow().enableBlendPreset();
    }

    private DisplayInteger getOverlayDisplay(Integer type) {
        if (cachedOverlays.containsKey(type)) {
            return cachedOverlays.get(type);
        }

        Model3D toReturn = new Model3D();
        toReturn.baseBlock = Blocks.STONE;
        toReturn.setTexture(MekanismFluids.Oxygen.getSprite());

        DisplayInteger display = DisplayInteger.createAndStart();

        if (cachedOverlays.containsKey(type)) {
            cachedOverlays.get(type);
        } else {
            cachedOverlays.put(type, display);
        }

        switch (type) {
            case 0: {
                toReturn.minY = 1;
                toReturn.maxY = 3;

                toReturn.minX = 0.46;
                toReturn.minZ = 0;
                toReturn.maxX = 0.54;
                toReturn.maxZ = 1;
                break;
            }
            case 1: {
                toReturn.minY = 1;
                toReturn.maxY = 3;

                toReturn.minX = 0;
                toReturn.minZ = 0.46;
                toReturn.maxX = 1;
                toReturn.maxZ = 0.54;
                break;
            }
        }

        MekanismRenderer.renderObject(toReturn);
        DisplayInteger.endList();

        return display;
    }
}