package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.data.ChemicalRenderData.GasRenderData;
import mekanism.client.render.data.ChemicalRenderData.InfusionRenderData;
import mekanism.client.render.data.ChemicalRenderData.PigmentRenderData;
import mekanism.client.render.data.ChemicalRenderData.SlurryRenderData;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.RenderData;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;

@ParametersAreNonnullByDefault
public class RenderDynamicTank extends MekanismTileEntityRenderer<TileEntityDynamicTank> {

    public RenderDynamicTank(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityDynamicTank tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        if (tile.isMaster()) {
            TankMultiblockData multiblock = tile.getMultiblock();
            if (multiblock.isFormed() && multiblock.renderLocation != null) {
                RenderData data = getRenderData(multiblock);
                if (data != null) {
                    data.location = multiblock.renderLocation;
                    data.height = multiblock.height() - 2;
                    data.length = multiblock.length();
                    data.width = multiblock.width();
                    matrix.pushPose();

                    VertexConsumer buffer = renderer.getBuffer(Sheets.translucentCullBlockSheet());
                    BlockPos pos = tile.getBlockPos();
                    matrix.translate(data.location.getX() - pos.getX(), data.location.getY() - pos.getY(), data.location.getZ() - pos.getZ());
                    int glow = data.calculateGlowLight(MekanismRenderer.FULL_SKY_LIGHT);
                    Model3D model = ModelRenderer.getModel(data, multiblock.prevScale);
                    MekanismRenderer.renderObject(model, matrix, buffer, data.getColorARGB(multiblock.prevScale), glow, overlayLight, getFaceDisplay(data, model));
                    matrix.popPose();
                    if (data instanceof FluidRenderData fluidRenderData) {
                        MekanismRenderer.renderValves(matrix, buffer, multiblock.valves, fluidRenderData, pos, glow, overlayLight, isInsideMultiblock(data));
                    }
                }
            }
        }
    }

    @Nullable
    private RenderData getRenderData(TankMultiblockData multiblock) {
        return switch (multiblock.mergedTank.getCurrentType()) {
            case FLUID -> new FluidRenderData(multiblock.getFluidTank().getFluid());
            case GAS -> new GasRenderData(multiblock.getGasTank().getStack());
            case INFUSION -> new InfusionRenderData(multiblock.getInfusionTank().getStack());
            case PIGMENT -> new PigmentRenderData(multiblock.getPigmentTank().getStack());
            case SLURRY -> new SlurryRenderData(multiblock.getSlurryTank().getStack());
            default -> null;
        };
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.DYNAMIC_TANK;
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityDynamicTank tile) {
        if (tile.isMaster()) {
            TankMultiblockData multiblock = tile.getMultiblock();
            return multiblock.isFormed() && !multiblock.isEmpty() && multiblock.renderLocation != null;
        }
        return false;
    }
}