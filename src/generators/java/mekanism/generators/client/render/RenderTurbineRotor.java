package mekanism.generators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.tileentity.ModelTileEntityRenderer;
import mekanism.generators.client.model.ModelTurbine;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderTurbineRotor extends ModelTileEntityRenderer<TileEntityTurbineRotor, ModelTurbine> {

    @Nullable
    public static RenderTurbineRotor INSTANCE;
    private static final float BASE_SPEED = 512F;

    public RenderTurbineRotor(BlockEntityRendererProvider.Context context) {
        super(context, ModelTurbine::new);
        INSTANCE = this;
    }

    public VertexConsumer getBuffer(@NotNull MultiBufferSource renderer) {
        return model.getBuffer(renderer);
    }

    @Override
    protected void render(TileEntityTurbineRotor tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        render(tile, matrix, getBuffer(renderer), light, overlayLight);
    }

    public void render(TileEntityTurbineRotor tile, PoseStack matrix, VertexConsumer buffer, int light, int overlayLight) {
        int housedBlades = tile.getHousedBlades();
        if (housedBlades == 0) {
            return;
        }
        int baseIndex = tile.getPosition() * 2;
        if (isTickingNormally(tile)) {
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
        matrix.mulPose(Axis.YP.rotationDegrees(tile.rotationLower));
        model.render(matrix, buffer, light, overlayLight, baseIndex);
        matrix.popPose();
        //Top blade
        if (housedBlades == 2) {
            matrix.pushPose();
            matrix.translate(0.5, -0.5, 0.5);
            matrix.mulPose(Axis.YP.rotationDegrees(tile.rotationUpper));
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
        return true;
    }

    @Override
    public boolean shouldRender(TileEntityTurbineRotor tile, Vec3 camera) {
        return tile.getMultiblockUUID() == null && tile.getHousedBlades() > 0 && super.shouldRender(tile, camera);
    }

    @Override
    public AABB getRenderBoundingBox(TileEntityTurbineRotor tile) {
        int radius = tile.getRadius();
        if (tile.blades == 0 || radius == -1) {
            //If there are no blades default to the collision box of the rotor
            return super.getRenderBoundingBox(tile);
        }
        BlockPos pos = tile.getBlockPos();
        return AABB.encapsulatingFullBlocks(pos.offset(-radius, 0, -radius), pos.offset(radius, 0, radius));
    }
}