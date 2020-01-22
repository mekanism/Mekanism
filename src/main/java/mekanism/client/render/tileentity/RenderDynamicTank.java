package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.FluidRenderer;
import mekanism.client.render.FluidRenderer.RenderData;
import mekanism.client.render.FluidRenderer.ValveRenderData;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.tile.TileEntityDynamicTank;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;

public class RenderDynamicTank extends TileEntityRenderer<TileEntityDynamicTank> {

    public RenderDynamicTank(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void render(@Nonnull TileEntityDynamicTank tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight) {
        if (tile.clientHasStructure && tile.isRendering && tile.structure != null && tile.structure.fluidStored.getAmount() > 0) {
            RenderData data = new RenderData();
            data.location = tile.structure.renderLocation;
            data.height = tile.structure.volHeight - 2;
            data.length = tile.structure.volLength;
            data.width = tile.structure.volWidth;
            data.fluidType = tile.structure.fluidStored;

            if (data.location != null && data.height >= 1) {
                matrix.push();
                BlockPos pos = tile.getPos();
                matrix.translate(data.location.x - pos.getX(), data.location.y - pos.getY(), data.location.z - pos.getZ());
                GlowInfo glowInfo = MekanismRenderer.enableGlow(data.fluidType);
                Model3D fluidModel = FluidRenderer.getFluidModel(data, tile.prevScale);
                MekanismRenderer.renderObject(fluidModel, matrix, renderer, MekanismRenderType.renderFluidState(AtlasTexture.LOCATION_BLOCKS_TEXTURE),
                      MekanismRenderer.getColorARGB(data.fluidType, (float) data.fluidType.getAmount() / (float) tile.clientCapacity));
                MekanismRenderer.disableGlow(glowInfo);
                matrix.pop();

                for (ValveData valveData : tile.valveViewing) {
                    matrix.push();
                    matrix.translate(valveData.location.x - pos.getX(), valveData.location.y - pos.getY(), valveData.location.z - pos.getZ());
                    GlowInfo valveGlowInfo = MekanismRenderer.enableGlow(data.fluidType);
                    Model3D valveModel = FluidRenderer.getValveModel(ValveRenderData.get(data, valveData));
                    MekanismRenderer.renderObject(valveModel, matrix, renderer, MekanismRenderType.renderFluidState(AtlasTexture.LOCATION_BLOCKS_TEXTURE),
                          MekanismRenderer.getColorARGB(data.fluidType));
                    MekanismRenderer.disableGlow(valveGlowInfo);
                    matrix.pop();
                }
            }
        }
    }

    @Override
    public boolean isGlobalRenderer(TileEntityDynamicTank tile) {
        return tile.clientHasStructure && tile.isRendering && tile.structure != null && tile.structure.fluidStored.getAmount() > 0;
    }
}