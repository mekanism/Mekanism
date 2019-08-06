package mekanism.generators.client.render;

import mekanism.api.Coord4D;
import mekanism.client.render.FluidRenderer;
import mekanism.client.render.FluidRenderer.RenderData;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderIndustrialTurbine extends TileEntityRenderer<TileEntityTurbineCasing> {

    private FluidStack STEAM = new FluidStack(FluidRegistry.getFluid("steam"), 1);

    @Override
    public void render(TileEntityTurbineCasing tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        renderAModelAt(tileEntity, x, y, z, partialTick, destroyStage);
    }

    public void renderAModelAt(TileEntityTurbineCasing tileEntity, double x, double y, double z, float partialTick, int destroyStage) {
        if (tileEntity.clientHasStructure && tileEntity.isRendering && tileEntity.structure != null && tileEntity.structure.complex != null) {
            RenderTurbineRotor.internalRender = true;
            Coord4D coord = tileEntity.structure.complex;

            while (true) {
                coord = coord.offset(Direction.DOWN);
                TileEntity tile = coord.getTileEntity(tileEntity.getWorld());
                if (!(tile instanceof TileEntityTurbineRotor)) {
                    break;
                }
                TileEntityRendererDispatcher.instance.render(tile, partialTick, destroyStage);
            }

            RenderTurbineRotor.internalRender = false;

            if (tileEntity.structure.fluidStored != null && tileEntity.structure.fluidStored.amount != 0 && tileEntity.structure.volLength > 0) {
                RenderData data = new RenderData();

                data.location = tileEntity.structure.renderLocation;
                data.height = tileEntity.structure.lowerVolume / (tileEntity.structure.volLength * tileEntity.structure.volWidth);
                data.length = tileEntity.structure.volLength;
                data.width = tileEntity.structure.volWidth;
                data.fluidType = STEAM;

                bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

                if (data.location != null && data.height >= 1 && tileEntity.structure.fluidStored.getFluid() != null) {
                    GlStateManager.pushMatrix();
                    GlStateManager.enableCull();
                    GlStateManager.enableBlend();
                    GlStateManager.disableLighting();
                    GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
                    FluidRenderer.translateToOrigin(data.location);
                    GlowInfo glowInfo = MekanismRenderer.enableGlow(tileEntity.structure.fluidStored);
                    MekanismRenderer.color(tileEntity.structure.fluidStored, (float) tileEntity.structure.fluidStored.amount / (float) tileEntity.structure.getFluidCapacity());
                    FluidRenderer.getTankDisplay(data).render();
                    MekanismRenderer.resetColor();
                    MekanismRenderer.disableGlow(glowInfo);
                    GlStateManager.enableLighting();
                    GlStateManager.disableBlend();
                    GlStateManager.disableCull();
                    GlStateManager.popMatrix();
                }
            }
        }
    }
}