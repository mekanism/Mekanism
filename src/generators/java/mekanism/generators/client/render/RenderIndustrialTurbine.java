package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.fluids.FluidStack;

public class RenderIndustrialTurbine extends TileEntityRenderer<TileEntityTurbineCasing> {

    @Nonnull
    private static FluidStack STEAM = FluidStack.EMPTY;

    public RenderIndustrialTurbine(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void func_225616_a_(@Nonnull TileEntityTurbineCasing tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight) {
        //TODO: 1.15
        //renderAModelAt(tile, x, y, z, partialTick, destroyStage);
    }

    public void renderAModelAt(TileEntityTurbineCasing tile, double x, double y, double z, float partialTick, int destroyStage) {
        //TODO: 1.15
        /*if (tile.clientHasStructure && tile.isRendering && tile.structure != null && tile.structure.complex != null) {
            RenderTurbineRotor.internalRender = true;
            BlockPos complexPos = tile.structure.complex.getPos();

            while (true) {
                complexPos = complexPos.down();
                TileEntityTurbineRotor rotor = MekanismUtils.getTileEntity(TileEntityTurbineRotor.class, tile.getWorld(), complexPos);
                if (rotor == null) {
                    break;
                }
                field_228858_b_.render(rotor, partialTick, destroyStage);
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

                field_228858_b_.textureManager.bindTexture(PlayerContainer.field_226615_c_);

                if (data.location != null && data.height >= 1 && tile.structure.fluidStored.getFluid() != Fluids.EMPTY) {
                    RenderSystem.pushMatrix();
                    RenderSystem.enableCull();
                    RenderSystem.enableBlend();
                    RenderSystem.disableLighting();
                    RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
                    FluidRenderer.translateToOrigin(data.location);
                    GlowInfo glowInfo = MekanismRenderer.enableGlow(tile.structure.fluidStored);
                    MekanismRenderer.color(tile.structure.fluidStored, (float) tile.structure.fluidStored.getAmount() / (float) tile.structure.getFluidCapacity());
                    FluidRenderer.getTankDisplay(data).render();
                    MekanismRenderer.resetColor();
                    MekanismRenderer.disableGlow(glowInfo);
                    RenderSystem.enableLighting();
                    RenderSystem.disableBlend();
                    RenderSystem.disableCull();
                    RenderSystem.popMatrix();
                }
            }
        }*/
    }

    @Override
    public boolean isGlobalRenderer(TileEntityTurbineCasing tile) {
        return tile.clientHasStructure && tile.isRendering && tile.structure != null && tile.structure.complex != null;
    }
}