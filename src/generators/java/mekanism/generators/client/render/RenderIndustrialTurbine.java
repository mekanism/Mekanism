package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.FluidRenderer;
import mekanism.client.render.FluidRenderer.RenderData;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;

public class RenderIndustrialTurbine extends TileEntityRenderer<TileEntityTurbineCasing> {

    @Nonnull
    private static FluidStack STEAM = FluidStack.EMPTY;

    public RenderIndustrialTurbine(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void render(@Nonnull TileEntityTurbineCasing tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light,
          int overlayLight) {
        if (tile.clientHasStructure && tile.isRendering && tile.structure != null && tile.structure.complex != null) {
            RenderTurbineRotor.internalRender = true;
            BlockPos pos = tile.getPos();
            BlockPos complexPos = tile.structure.complex.getPos();

            while (true) {
                complexPos = complexPos.down();
                TileEntityTurbineRotor rotor = MekanismUtils.getTileEntity(TileEntityTurbineRotor.class, tile.getWorld(), complexPos);
                if (rotor == null) {
                    break;
                }
                matrix.push();
                matrix.translate(complexPos.getX() - pos.getX(), complexPos.getY() - pos.getY(), complexPos.getZ() - pos.getZ());
                renderDispatcher.renderNullable(rotor, matrix, renderer, MekanismRenderer.FULL_LIGHT, overlayLight);
                matrix.pop();
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

                if (data.location != null && data.height >= 1 && !tile.structure.fluidStored.isEmpty()) {
                    matrix.push();
                    matrix.translate(data.location.x - pos.getX(), data.location.y - pos.getY(), data.location.z - pos.getZ());
                    GlowInfo glowInfo = MekanismRenderer.enableGlow(tile.structure.fluidStored);
                    Model3D fluidModel = FluidRenderer.getFluidModel(data, 1);
                    MekanismRenderer.renderObject(fluidModel, matrix, renderer, MekanismRenderType.renderFluidState(AtlasTexture.LOCATION_BLOCKS_TEXTURE),
                          MekanismRenderer.getColorARGB(tile.structure.fluidStored, (float) tile.structure.fluidStored.getAmount() / (float) tile.structure.getFluidCapacity()));
                    MekanismRenderer.disableGlow(glowInfo);
                    matrix.pop();
                }
            }
        }
    }

    @Override
    public boolean isGlobalRenderer(TileEntityTurbineCasing tile) {
        return tile.clientHasStructure && tile.isRendering && tile.structure != null && tile.structure.complex != null;
    }
}