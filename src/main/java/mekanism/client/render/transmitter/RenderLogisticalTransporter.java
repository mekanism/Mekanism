package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import mekanism.api.text.EnumColor;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.transmitter.TransmitterRenderState.TransporterRenderState;
import mekanism.client.render.transmitter.TransmitterRenderState.TransporterRenderState.TransporterStackRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.TransporterUtils;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public class RenderLogisticalTransporter extends RenderTransmitterBase<TileEntityLogisticalTransporterBase, TransporterRenderState> {

    private final ItemModelResolver itemModelResolver;

    public RenderLogisticalTransporter(BlockEntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public TransporterRenderState createRenderState() {
        return new TransporterRenderState();
    }

    @Override
    public void extractRenderState(TileEntityLogisticalTransporterBase transporter, TransporterRenderState state, float partialTick, Vec3 cameraPosition,
          ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(transporter, state, partialTick, cameraPosition, breakProgress);
        LogisticalTransporterBase transmitter = transporter.getTransmitter();
        Collection<TransporterStack> inTransit = transmitter.getTransit();
        if (!inTransit.isEmpty()) {
            Level level = transporter.getLevel();
            float partial = partialTick * transmitter.tier.getSpeed();
            state.stacks = new ArrayList<>();
            record TransportInformation(int progress, ItemResource item, @Nullable EnumColor color) {
            }
            Set<TransportInformation> information = new ObjectOpenHashSet<>(inTransit.size());
            for (TransporterStack stack : inTransit) {
                //Shrink the in transit list as much as possible. Don't try to render things of the same type that are in the same spot with the same color, ignoring stack size
                if (!stack.isEmpty() && information.add(new TransportInformation(stack.progress, stack.getItemType(), stack.color))) {
                    //Ensure the stack is valid AND we did not already have information matching the stack
                    //We use add to check if it already contained the value, so that we only have to query the set once
                    Vector3f stackPos = TransporterUtils.getStackPosition(transmitter, stack, partial);
                    TransporterStackRenderState stackRenderState = new TransporterStackRenderState(stackPos, stack.color);
                    //Similar to campfire renderer's seed, except we bind it to the original location so that when the stack goes from one transporter to the next
                    // it doesn't change types
                    this.itemModelResolver.updateForTopItem(stackRenderState.item(), stack.asItemStack(), ItemDisplayContext.NONE, level, null, (int) stack.originalLocation);
                    state.stacks.add(stackRenderState);
                }
            }
        }
    }

    @Override
    public void submit(TransporterRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        //Note: We expect this to not actually render anything, but call it just in case for consistency
        super.submit(state, poseStack, nodeCollector, camera);
        if (!state.stacks.isEmpty()) {
            for (TransporterStackRenderState stackRenderState : state.stacks) {
                poseStack.pushPose();
                poseStack.translate(stackRenderState.stackPos().x(), stackRenderState.stackPos().y(), stackRenderState.stackPos().z());
                if (stackRenderState.color() != null) {
                    poseStack.pushPose();
                    poseStack.translate(-0.5F, -0.25F, -0.5F);
                    nodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockItemSheet(), MekanismModelCache.INSTANCE.TRANSPORTER_BOX.getBakedModel(),
                          new int[]{stackRenderState.color().getPackedColor()}, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
                    poseStack.popPose();
                }
                AABB bb = stackRenderState.item().getModelBoundingBox();
                double maxDimension = Math.max(bb.getXsize(), Math.max(bb.getYsize(), bb.getZsize()));
                //TODO: Do we want to scale up tiny models?
                if (maxDimension > 1) {
                    //Scale any overly large models down to a single unit
                    float scale = (float) (1 / maxDimension);
                    poseStack.scale(scale, scale, scale);
                    poseStack.translate(0, 0.25F, 0);
                }
                poseStack.translate(0, 0.25F, 0);
                poseStack.scale(0.25F, 0.25F, 0.25F);
                stackRenderState.item().submit(poseStack, nodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.LOGISTICAL_TRANSPORTER;
    }
}