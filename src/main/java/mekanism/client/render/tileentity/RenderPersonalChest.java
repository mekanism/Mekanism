package mekanism.client.render.tileentity;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

@NothingNullByDefault
public class RenderPersonalChest extends ChestRenderer<TileEntityPersonalChest> {

    private static final Identifier TEXTURE = MekanismUtils.getResource(ResourceType.TEXTURE_BLOCKS, "models/personal_chest.png");
    //TODO - 26.1: Validate this is properly grabbing the texture
    private static final SpriteId MATERIAL = Sheets.BLOCKS_MAPPER.apply(TEXTURE);

    public RenderPersonalChest(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    //TODO - 26.1: Evaluate if we have to do anything or if it works fine even though it isn't a ChestBlockEntity
    /*@Override
    public void extractRenderState(TileEntityPersonalChest chest, ChestRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(chest, state, partialTicks, cameraPosition, breakProgress);
    }

    @Override
    protected void render(TileEntityPersonalChest tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        matrix.pushPose();
        if (!tile.isRemoved()) {
            matrix.translate(0.5D, 0.5D, 0.5D);
            matrix.mulPose(Axis.YP.rotationDegrees(-tile.getDirection().toYRot()));
            matrix.translate(-0.5D, -0.5D, -0.5D);
        }
        float lidAngle = 1.0F - tile.getOpenNess(partialTick);
        lidAngle = 1.0F - lidAngle * lidAngle * lidAngle;
        VertexConsumer builder = renderer.getBuffer(RenderType.entityCutout(texture));
        lid.xRot = -(lidAngle * Mth.HALF_PI);
        lock.xRot = lid.xRot;
        lid.render(matrix, builder, light, overlayLight);
        lock.render(matrix, builder, light, overlayLight);
        bottom.render(matrix, builder, light, overlayLight);
        matrix.popPose();
    }*/

    @Nullable
    @Override
    protected SpriteId getCustomSprite(TileEntityPersonalChest chest, ChestRenderState state) {
        return MATERIAL;
    }

    //@Override//TODO - 26.1: Figure out if we need to setup profiling for this again?
    protected String getProfilerSection() {
        return ProfilerConstants.PERSONAL_CHEST;
    }
}