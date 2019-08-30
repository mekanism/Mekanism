package mekanism.generators.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.client.render.FluidRenderer;
import mekanism.client.render.FluidRenderer.RenderData;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.common.MekanismGases;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.Fluids;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

@OnlyIn(Dist.CLIENT)
public class RenderIndustrialTurbine extends TileEntityRenderer<TileEntityTurbineCasing> {

    @Nonnull
    private static final FluidStack STEAM = new FluidStack(MekanismGases.STEAM.getFluid(), 1);

    @Override
    public void render(TileEntityTurbineCasing tileEntity, double x, double y, double z, float partialTick, int destroyStage) {
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

            if (tileEntity.structure.fluidStored.getAmount() > 0 && tileEntity.structure.volLength > 0) {
                RenderData data = new RenderData();

                data.location = tileEntity.structure.renderLocation;
                data.height = tileEntity.structure.lowerVolume / (tileEntity.structure.volLength * tileEntity.structure.volWidth);
                data.length = tileEntity.structure.volLength;
                data.width = tileEntity.structure.volWidth;
                data.fluidType = STEAM;

                bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

                if (data.location != null && data.height >= 1 && tileEntity.structure.fluidStored.getFluid() != Fluids.EMPTY) {
                    GlStateManager.pushMatrix();
                    GlStateManager.enableCull();
                    GlStateManager.enableBlend();
                    GlStateManager.disableLighting();
                    GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
                    FluidRenderer.translateToOrigin(data.location);
                    GlowInfo glowInfo = MekanismRenderer.enableGlow(tileEntity.structure.fluidStored);
                    MekanismRenderer.color(tileEntity.structure.fluidStored, (float) tileEntity.structure.fluidStored.getAmount() / (float) tileEntity.structure.getFluidCapacity());
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