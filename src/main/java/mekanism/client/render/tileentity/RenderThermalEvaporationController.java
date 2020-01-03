package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.FluidRenderer;
import mekanism.client.render.FluidRenderer.RenderData;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.math.BlockPos;

public class RenderThermalEvaporationController extends TileEntityRenderer<TileEntityThermalEvaporationController> {

    public RenderThermalEvaporationController(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void func_225616_a_(@Nonnull TileEntityThermalEvaporationController tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer,
          int light, int overlayLight) {
        if (tile.structured && tile.height - 2 >= 1 && tile.inputTank.getFluidAmount() > 0) {
            RenderData data = new RenderData();
            data.location = tile.getRenderLocation();
            data.height = tile.height - 2;
            //TODO: If we ever allow different width for the evap controller then update this length and width
            data.length = 2;
            data.width = 2;
            data.fluidType = tile.inputTank.getFluid();
            field_228858_b_.textureManager.bindTexture(PlayerContainer.field_226615_c_);
            matrix.func_227860_a_();
            BlockPos pos = tile.getPos();
            matrix.func_227861_a_(data.location.x - pos.getX(), data.location.y - pos.getY(), data.location.z - pos.getZ());
            float fluidScale = (float) tile.inputTank.getFluidAmount() / (float) tile.getMaxFluid();
            GlowInfo glowInfo = MekanismRenderer.enableGlow(data.fluidType);
            //Render the proper height
            Model3D fluidModel = FluidRenderer.getFluidModel(data, Math.min(1, fluidScale));
            MekanismRenderer.renderObject(fluidModel, matrix, renderer, MekanismRenderType.renderFluidState(PlayerContainer.field_226615_c_),
                  MekanismRenderer.getColorARGB(data.fluidType, fluidScale));
            MekanismRenderer.disableGlow(glowInfo);
            matrix.func_227865_b_();
        }
    }

    @Override
    public boolean isGlobalRenderer(TileEntityThermalEvaporationController tile) {
        return tile.structured && tile.height - 2 >= 1 && tile.inputTank.getFluidAmount() > 0;
    }
}