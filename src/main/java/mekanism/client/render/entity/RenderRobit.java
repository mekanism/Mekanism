package mekanism.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Collections;
import mekanism.client.RobitSpriteUploader;
import mekanism.common.entity.EntityRobit;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismRobitSkins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RenderRobit extends MobRenderer<EntityRobit, RenderRobit.RobitRenderState, RenderRobit.RobitModelWrapper> {

    public RenderRobit(EntityRendererProvider.Context context) {
        super(context, new RobitModelWrapper(new ModelPart(Collections.emptyList(), Collections.emptyMap())), 0.5F);
    }

    @Override
    public RobitRenderState createRenderState() {
        return new RobitRenderState();
    }

    @Override
    public void extractRenderState(EntityRobit robit, RobitRenderState state, float partialTick) {
        super.extractRenderState(robit, state, partialTick);
        state.skinLookup = MekanismRobitSkins.lookup(robit.level().registryAccess(), robit.getSkin());
        state.modelData = robit.getModelData();
        
        ItemStack stack = MekanismItems.ROBIT.asStack();
        ItemModelResolver resolver = Minecraft.getInstance().getItemModelResolver();
        resolver.updateForTopItem(state.itemStackRenderState, stack, ItemDisplayContext.GROUND, robit.level(), null, 0);
    }

    @Override
    public void submit(@NotNull RobitRenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector nodeCollector, @NotNull net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!state.itemStackRenderState.isEmpty()) {
            poseStack.pushPose();
            
            poseStack.scale(2.0F, 2.0F, 2.0F);
            
            poseStack.translate(0, 0.1, 0);
            
            poseStack.mulPose(Axis.YP.rotationDegrees(270));
            
            state.itemStackRenderState.submit(
                poseStack,
                nodeCollector,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0
            );

            poseStack.popPose();
        }
        
        super.submit(state, poseStack, nodeCollector, camera);
    }

    @NotNull
    @Override
    public Identifier getTextureLocation(@NotNull RobitRenderState state) {
        return RobitSpriteUploader.ATLAS_LOCATION;
    }

    public static class RobitRenderState extends LivingEntityRenderState {
        @Nullable
        public MekanismRobitSkins.SkinLookup skinLookup;
        public ModelData modelData = ModelData.EMPTY;
        public final ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
    }

    public static class RobitModelWrapper extends EntityModel<RobitRenderState> {
        RobitModelWrapper(ModelPart root) {
            super(root);
        }
    }
}