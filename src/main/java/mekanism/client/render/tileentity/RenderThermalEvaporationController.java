package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class RenderThermalEvaporationController extends TileEntityRenderer<TileEntityThermalEvaporationController> {

    public RenderThermalEvaporationController(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void func_225616_a_(@Nonnull TileEntityThermalEvaporationController tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        //TODO: 1.15
        /*if (tile.structured && tile.height - 2 >= 1 && tile.inputTank.getFluidAmount() > 0) {
            RenderData data = new RenderData();
            data.location = tile.getRenderLocation();
            data.height = tile.height - 2;
            //TODO: If we ever allow different width for the evap controller then update this length and width
            data.length = 2;
            data.width = 2;
            data.fluidType = tile.inputTank.getFluid();
            field_228858_b_.textureManager.bindTexture(PlayerContainer.field_226615_c_);
            RenderSystem.pushMatrix();
            RenderSystem.enableCull();
            RenderSystem.enableBlend();
            RenderSystem.disableLighting();
            RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            setLightmapDisabled(true);
            FluidRenderer.translateToOrigin(data.location);
            float fluidScale = (float) tile.inputTank.getFluidAmount() / (float) tile.getMaxFluid();
            GlowInfo glowInfo = MekanismRenderer.enableGlow(data.fluidType);
            MekanismRenderer.color(data.fluidType, fluidScale);
            if (data.fluidType.getFluid().getAttributes().isGaseous(data.fluidType)) {
                FluidRenderer.getTankDisplay(data).render();
            } else {
                //Render the proper height
                FluidRenderer.getTankDisplay(data, Math.min(1, fluidScale)).render();
            }
            MekanismRenderer.resetColor();
            MekanismRenderer.disableGlow(glowInfo);
            setLightmapDisabled(false);
            RenderSystem.enableLighting();
            RenderSystem.disableBlend();
            RenderSystem.disableCull();
            RenderSystem.popMatrix();
        }*/
    }

    @Override
    public boolean isGlobalRenderer(TileEntityThermalEvaporationController tile) {
        return tile.structured && tile.height - 2 >= 1 && tile.inputTank.getFluidAmount() > 0;
    }
}