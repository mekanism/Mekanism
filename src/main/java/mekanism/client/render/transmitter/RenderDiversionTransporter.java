package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.render.transmitter.TransmitterRenderState.TransporterRenderState.DiversionTransporterRenderState;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.network.transmitter.DiversionTransporter;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderDiversionTransporter extends RenderLogisticalTransporter<TileEntityDiversionTransporter, DiversionTransporterRenderState> {

    public static final ModelLayerLocation OVERLAY_LAYER = new ModelLayerLocation(Mekanism.rl("diversion_overlay"), "main");
    private static final SpriteId TORCH_OFF_TEXTURE = Sheets.BLOCKS_MAPPER.defaultNamespaceApply("redstone_torch_off");
    private static final SpriteId TORCH_TEXTURE = Sheets.BLOCKS_MAPPER.defaultNamespaceApply("redstone_torch");
    private static final SpriteId GUNPOWDER_TEXTURE = Sheets.ITEMS_MAPPER.defaultNamespaceApply("gunpowder");
    private static final int DIVERSION_OVERLAY_ARGB = ARGB.white(0.8F);

    public static LayerDefinition createOverlayLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        //TODO - 26.2: Figure out the overlay model size and parameters. And if we need a separate one for each case of only rendering the model on one side?
        root.addOrReplaceChild("overlay",
              CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.01F)),
              PartPose.ZERO
        );
        return LayerDefinition.create(mesh, 16, 16);
    }

    protected final SpriteGetter materials;
    private final ModelPart overlayModel;

    public RenderDiversionTransporter(BlockEntityRendererProvider.Context context) {
        super(context, DiversionTransporterRenderState::new);
        this.materials = context.sprites();
        this.overlayModel = context.bakeLayer(OVERLAY_LAYER);
    }

    @Override
    public void extractRenderState(TileEntityDiversionTransporter transporter, DiversionTransporterRenderState state, float partialTick, Vec3 cameraPosition,
          ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(transporter, state, partialTick, cameraPosition, breakProgress);
        Player player = Minecraft.getInstance().player;
        //Player shouldn't be null here, but validate it
        if (player != null) {
            ItemStack itemStack = player.getMainHandItem();
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemConfigurator) {
                BlockHitResult rayTraceResult = MekanismUtils.rayTrace(player);
                if (rayTraceResult.getType() != Type.MISS && rayTraceResult.getBlockPos().equals(state.blockPos)) {
                    DiversionTransporter transmitter = transporter.getTransmitter();
                    Direction side = transporter.getSideLookingAt(player, rayTraceResult.getDirection());
                    state.overlay = switch (transmitter.modes[side.ordinal()]) {
                        case DISABLED -> GUNPOWDER_TEXTURE;
                        case HIGH -> TORCH_TEXTURE;
                        case LOW -> TORCH_OFF_TEXTURE;
                    };
                }
            }
        }
    }

    @Override
    public void submit(DiversionTransporterRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (!MekanismConfig.client.opaqueTransmitters.get()) {//TODO - 26.2: Re-evaluate this check
            super.submit(state, poseStack, nodeCollector, camera);
        }
        if (state.overlay != null) {
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate(0.5, 0.5, 0.5);
            nodeCollector.submitModelPart(
                  this.overlayModel,
                  poseStack,
                  //TODO - 26.2: ConduitRenderer uses RenderTypes::entityCutoutNoCull, do we care about cull,
                  // and is MC better at handling ordering of a variety of render types. Such as we used to use translucentCullBlockSheet
                  state.overlay.renderType(RenderTypes::entityCutout),
                  state.lightCoords,
                  OverlayTexture.NO_OVERLAY,
                  this.materials.get(state.overlay),
                  DIVERSION_OVERLAY_ARGB,
                  state.breakProgress//TODO - 26.2: Do we want to be passing the crumbling state?
            );
            poseStack.popPose();
        }
    }

    @Override
    protected boolean shouldRenderTransmitter(TileEntityDiversionTransporter tile, Vec3 camera) {
        //Render the transmitter as we will need to render the overlay regardless of if the rest of the transmitter is opaque
        return true;
    }
}