package mekanism.generators.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import javax.annotation.Nonnull;
import mekanism.client.render.FluidRenderer;
import mekanism.client.render.FluidRenderer.RenderData;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.common.MekanismFluids;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;

public class RenderIndustrialTurbine extends TileEntityRenderer<TileEntityTurbineCasing> {

    @Nonnull
    private static FluidStack STEAM = FluidStack.EMPTY;

    @Override
    public void render(TileEntityTurbineCasing tile, double x, double y, double z, float partialTick, int destroyStage) {
        renderAModelAt(tile, x, y, z, partialTick, destroyStage);
    }

    public void renderAModelAt(TileEntityTurbineCasing tile, double x, double y, double z, float partialTick, int destroyStage) {
        if (tile.clientHasStructure && tile.isRendering && tile.structure != null && tile.structure.complex != null) {
            RenderTurbineRotor.internalRender = true;
            BlockPos complexPos = tile.structure.complex.getPos();

            while (true) {
                complexPos = complexPos.down();
                TileEntityTurbineRotor rotor = MekanismUtils.getTileEntity(TileEntityTurbineRotor.class, tile.getWorld(), complexPos);
                if (rotor == null) {
                    break;
                }
                TileEntityRendererDispatcher.instance.render(rotor, partialTick, destroyStage);
            }

            RenderTurbineRotor.internalRender = false;

            if (tile.structure.fluidStored.getAmount() > 0 && tile.structure.volLength > 0) {
                if (STEAM.isEmpty()) {
                    STEAM = MekanismFluids.STEAM.getFluidStack(1);
                }
                RenderData data = new RenderData();

                data.location = tile.structure.renderLocation;
                data.height = tile.structure.lowerVolume / (tile.structure.volLength * tile.structure.volWidth);
                data.length = tile.structure.volLength;
                data.width = tile.structure.volWidth;
                data.fluidType = STEAM;

                bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

                if (data.location != null && data.height >= 1 && tile.structure.fluidStored.getFluid() != Fluids.EMPTY) {
                    GlStateManager.pushMatrix();
                    GlStateManager.enableCull();
                    GlStateManager.enableBlend();
                    GlStateManager.disableLighting();
                    GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
                    FluidRenderer.translateToOrigin(data.location);
                    GlowInfo glowInfo = MekanismRenderer.enableGlow(tile.structure.fluidStored);
                    MekanismRenderer.color(tile.structure.fluidStored, (float) tile.structure.fluidStored.getAmount() / (float) tile.structure.getFluidCapacity());
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

    @Override
    public boolean isGlobalRenderer(TileEntityTurbineCasing tile) {
        return tile.clientHasStructure && tile.isRendering && tile.structure != null && tile.structure.complex != null;
    }
}