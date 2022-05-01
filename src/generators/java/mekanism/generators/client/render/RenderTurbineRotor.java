package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.UUID;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.generators.client.model.ModelTurbine;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;

@ParametersAreNonnullByDefault
public class RenderTurbineRotor extends MekanismTileEntityRenderer<TileEntityTurbineRotor> {

    public static RenderTurbineRotor INSTANCE;
    private static final float BASE_SPEED = 512F;
    public final ModelTurbine model;

    public RenderTurbineRotor(BlockEntityRendererProvider.Context context) {
        super(context);
        INSTANCE = this;
        model = new ModelTurbine(context.getModelSet());
    }

    @Override
    protected void render(TileEntityTurbineRotor tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        if (tile.getMultiblockUUID() == null) {
            render(tile, matrix, model.getBuffer(renderer), light, overlayLight);
        }
    }

    public void render(TileEntityTurbineRotor tile, PoseStack matrix, VertexConsumer buffer, int light, int overlayLight) {
        int housedBlades = tile.getHousedBlades();
        if (housedBlades == 0) {
            return;
        }
        int baseIndex = tile.getPosition() * 2;
        if (!Minecraft.getInstance().isPaused()) {
            UUID multiblockUUID = tile.getMultiblockUUID();
            if (multiblockUUID != null && TurbineMultiblockData.clientRotationMap.containsKey(multiblockUUID)) {
                float rotateSpeed = TurbineMultiblockData.clientRotationMap.getFloat(multiblockUUID) * BASE_SPEED;
                tile.rotationLower = (tile.rotationLower + rotateSpeed * (1F / (baseIndex + 1))) % 360;
                tile.rotationUpper = (tile.rotationUpper + rotateSpeed * (1F / (baseIndex + 2))) % 360;
            } else {
                tile.rotationLower = tile.rotationLower % 360;
                tile.rotationUpper = tile.rotationUpper % 360;
            }
        }
        //Bottom blade
        matrix.pushPose();
        matrix.translate(0.5, -1, 0.5);
        matrix.mulPose(Vector3f.YP.rotationDegrees(tile.rotationLower));
        model.render(matrix, buffer, light, overlayLight, baseIndex);
        matrix.popPose();
        //Top blade
        if (housedBlades == 2) {
            matrix.pushPose();
            matrix.translate(0.5, -0.5, 0.5);
            matrix.mulPose(Vector3f.YP.rotationDegrees(tile.rotationUpper));
            model.render(matrix, buffer, light, overlayLight, baseIndex + 1);
            matrix.popPose();
        }
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.TURBINE_ROTOR;
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityTurbineRotor tile) {
        return tile.getMultiblockUUID() == null && tile.getHousedBlades() > 0;
    }
}