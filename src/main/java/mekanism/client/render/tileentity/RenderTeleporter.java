package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
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
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class RenderTeleporter extends MekanismTileEntityRenderer<TileEntityTeleporter> {

    private Map<Integer, DisplayInteger> cachedOverlays = new HashMap<>();

    @Override
    public void func_225616_a_(@Nonnull TileEntityTeleporter tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        //TODO: Figure out why it always renders in one direction even if the teleporter is assembled along the other axis
        if (tile.shouldRender) {
            RenderSystem.pushMatrix();
            RenderSystem.enableCull();
            RenderSystem.disableLighting();
            GlowInfo glowInfo = MekanismRenderer.enableGlow();
            RenderSystem.shadeModel(GL11.GL_SMOOTH);
            RenderSystem.disableAlphaTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            MekanismRenderer.color(EnumColor.PURPLE, 0.75F);

            field_228858_b_.textureManager.bindTexture(PlayerContainer.field_226615_c_);
            RenderSystem.translatef((float) x, (float) y, (float) z);
            BlockPos pos = tile.getPos().west();
            int type = 0;
            BlockState s = tile.getWorld().getBlockState(pos);
            if (s.getBlock() instanceof BlockTeleporter) {
                type = 1;
            }

            int display = getOverlayDisplay(type).display;
            GlStateManager.callList(display);

            MekanismRenderer.resetColor();
            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
            MekanismRenderer.disableGlow(glowInfo);
            RenderSystem.enableLighting();
            RenderSystem.disableCull();
            RenderSystem.popMatrix();
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
        return tile.shouldRender;
    }
}