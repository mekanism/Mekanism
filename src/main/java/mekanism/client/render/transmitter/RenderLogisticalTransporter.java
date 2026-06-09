package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.render.transmitter.TransmitterRenderState.TransporterRenderState;
import mekanism.client.render.transmitter.TransmitterRenderState.TransporterRenderState.TransporterStackRenderState;
import mekanism.common.Mekanism;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TransporterUtils;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;
import org.joml.Vector3f;

public class RenderLogisticalTransporter<TILE extends TileEntityLogisticalTransporterBase, STATE extends TransporterRenderState> extends RenderTransmitterBase<TILE, STATE> {

    public static final ModelLayerLocation BOX_LAYER = new ModelLayerLocation(Mekanism.rl("transporter_box"), "main");
    private static final Identifier BOX_TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "transporter_box.png");

    public static LayerDefinition createBoxLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("box", CubeListBuilder.create().addBox(0F, 0F, 0F, 7, 7, 7),
              //TODO - 26.1: Do we need the offset, or can we just move the origin?
              PartPose.offset(-3.5F, 0, -3.5F)
        );
        return LayerDefinition.create(mesh, 64, 64);
    }

    private final ItemModelResolver itemModelResolver;
    private final Supplier<STATE> stateCreator;
    private final ModelPart modelBox;

    public RenderLogisticalTransporter(BlockEntityRendererProvider.Context context, Supplier<STATE> stateCreator) {
        super(context);
        this.stateCreator = stateCreator;
        this.modelBox = context.bakeLayer(BOX_LAYER);
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public STATE createRenderState() {
        return stateCreator.get();
    }

    @Override
    public void extractRenderState(TILE transporter, STATE state, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(transporter, state, partialTick, cameraPosition, breakProgress);
        LogisticalTransporterBase transmitter = transporter.getTransmitter();
        Collection<TransporterStack> inTransit = transmitter.getTransit();
        if (!inTransit.isEmpty()) {
            Level level = transmitter.getLevel();
            float partial = partialTick * transmitter.tier.getSpeed();
            state.stacks = new ArrayList<>();
            Set<TransportInformation> information = new ObjectOpenHashSet<>(inTransit.size());
            for (TransporterStack stack : inTransit) {
                //Shrink the in transit list as much as possible. Don't try to render things of the same type that are in the same spot with the same color, ignoring stack size
                if (!stack.isEmpty() && information.add(new TransportInformation(stack))) {
                    //Ensure the stack is valid AND we did not already have information matching the stack
                    //We use add to check if it already contained the value, so that we only have to query the set once
                    Vector3f stackPos = TransporterUtils.getStackPosition(transmitter, stack, partial);
                    TransporterStackRenderState stackRenderState = new TransporterStackRenderState(stackPos, stack.color);
                    //TODO - 26.1: Do we need to do any sort of seed?
                    this.itemModelResolver.updateForTopItem(stackRenderState.item(), stack.asItemStack(), ItemDisplayContext.GROUND, level, null, 0);
                    state.stacks.add(stackRenderState);
                }
            }
        }
    }

    @Override
    public void submit(STATE state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (!state.stacks.isEmpty()) {
            poseStack.pushPose();
            for (TransporterStackRenderState stackRenderState : state.stacks) {
                poseStack.pushPose();
                poseStack.translate(stackRenderState.stackPos().x(), stackRenderState.stackPos().y(), stackRenderState.stackPos().z());
                if (stackRenderState.color() != null) {
                    nodeCollector.submitModelPart(
                          this.modelBox,
                          poseStack,
                          //TODO - 26.1: Is this the correct render type to be using? I believe it is what we used to use, so it probably is fine
                          RenderTypes.entityCutout(BOX_TEXTURE),
                          //TODO - 26.1: I believe in the past we used LightTexture.FULL_BRIGHT for the model box, check which looks better state.lightCoords
                          LightCoordsUtil.FULL_BRIGHT,
                          OverlayTexture.NO_OVERLAY,
                          //TODO - 26.1: Do we need to pass the texture here as well, or not?
                          null,
                          stackRenderState.color().getPackedColor(),
                          null//TODO - 26.1: Should we render the crumbling progress onto the box around the item inside the transporter? Probably not
                    );
                }
                //TODO - 26.1: We used to render the item before the box, but doing it after lets us skip an extra push/pop.
                // Does this still render as we expect it to?
                //Render the item at the center of the block, this translation used to be handled by the item entity's position
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.scale(0.75F, 0.75F, 0.75F);
                stackRenderState.item().submit(poseStack, nodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
            poseStack.popPose();
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.LOGISTICAL_TRANSPORTER;
    }

    private static class TransportInformation {

        @Nullable
        private final EnumColor color;
        private final ItemResource item;
        private final int progress;

        private TransportInformation(TransporterStack transporterStack) {
            this.progress = transporterStack.progress;
            this.color = transporterStack.color;
            this.item = transporterStack.getItemType();
        }

        @Override
        public int hashCode() {
            int code = 1;
            code = 31 * code + progress;
            code = 31 * code + item.hashCode();
            if (color != null) {
                code = 31 * code + color.hashCode();
            }
            return code;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == this) {
                return true;
            }
            return obj instanceof TransportInformation other && progress == other.progress && color == other.color && item.equals(other.item);
        }
    }
}