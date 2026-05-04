package mekanism.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import mekanism.client.model.robit.RobitModel;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismDataComponents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;

public class RenderRobit extends MobRenderer<EntityRobit, RenderRobit.RobitRenderState, RobitModel> {

    public static final Identifier ROBIT_MODEL = Mekanism.rl("item/robit");

    public RenderRobit(EntityRendererProvider.Context context) {
        super(context, new RobitModel(context.bakeLayer(RobitModel.ROBIT_LAYER)), 0.5F);
    }

    @Override
    public RobitRenderState createRenderState() {
        return new RobitRenderState();
    }

    @Override
    public void extractRenderState(@NonNull EntityRobit robit, @NonNull RobitRenderState state, float partialTick) {
        super.extractRenderState(robit, state, partialTick);
        ItemStack stack = new ItemStack(MekanismItems.ROBIT.get());
        stack.set(MekanismDataComponents.ROBIT_SKIN, robit.getSkin());
        state.walkAnimationPos = robit.walkAnimation.position(partialTick);
        state.walkAnimationSpeed = robit.walkAnimation.speed(partialTick);
        state.yBodyRot = robit.yBodyRot;
        state.yHeadRot = robit.yHeadRot;

        BlockState blockAtFeet = robit.level().getBlockState(robit.blockPosition());
        state.isOnChargepad = WorldUtils.getTileEntity(TileEntityChargepad.class, robit.level(), robit.blockPosition()) != null;
        
        if (state.isOnChargepad && blockAtFeet.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            state.padRotation = blockAtFeet.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot();
        } else {
            state.padRotation = 0;
        }

        if (blockAtFeet.getBlock() instanceof SnowLayerBlock) {
            int layers = blockAtFeet.getValue(SnowLayerBlock.LAYERS);
            state.snowOffset = layers / 8.0F;
        } else {
            state.snowOffset = 0;
        }

        Minecraft.getInstance().getItemModelResolver().updateForTopItem(state.itemRenderState, stack,
                ItemDisplayContext.NONE, robit.level(), robit, robit.getId());
    }

    @Override
    public void submit(@NonNull RobitRenderState state, @NonNull PoseStack poseStack,
            @NonNull SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        float rotation;
        
        if (state.isOnChargepad) {
            rotation = 180.0F - state.yHeadRot;
        } else {
            rotation = 180.0F - state.yBodyRot;
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        float yOffset = 0.5F + state.snowOffset;
        
        if (state.isOnChargepad) {
            yOffset += 0.0625F;
        }

        poseStack.translate(0, yOffset, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        
        float wobble = Mth.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed;
        poseStack.mulPose(Axis.ZP.rotationDegrees(wobble * 5.0F));
        state.itemRenderState.submit(poseStack, nodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY,
                state.outlineColor);
        poseStack.popPose();

        super.submit(state, poseStack, nodeCollector, camera);
    }

    @Override
    public @NonNull Identifier getTextureLocation(@NotNull RobitRenderState state) {
        return AtlasIds.BLOCKS;
    }

    public static class RobitRenderState extends LivingEntityRenderState {
        public final ItemStackRenderState itemRenderState = new ItemStackRenderState();
        public float walkAnimationPos;
        public float walkAnimationSpeed;
        public float yBodyRot;
        public float yHeadRot;
        public float snowOffset;
        public float padRotation;
        public boolean isOnChargepad;
    }

    public static class Unbaked implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = Identifier.CODEC.fieldOf("model").xmap(Unbaked::new,
                u -> u.model);

        private final Identifier model;

        public Unbaked(Identifier model) {
            this.model = model;
        }

        @Override
        public @Nullable SpecialModelRenderer<Void> bake(SpecialModelRenderer.@NonNull BakingContext context) {
            if (!(context instanceof ItemModel.BakingContext itemBaking)) {
                return null;
            }
            ModelBaker modelBaker = itemBaking.blockModelBaker();
            ResolvedModel resolvedModel = modelBaker.getModel(model);
            BlockStateModelPart bakedModel = SimpleModelWrapper.bake(modelBaker, resolvedModel,
                    BlockModelRotation.get(OctahedralGroup.IDENTITY));
            return new RenderRobitItem(bakedModel);
        }

        @Override
        public @NonNull MapCodec<? extends NoDataSpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }

    private static class RenderRobitItem implements NoDataSpecialModelRenderer {
        private final BlockStateModelPart model;

        public RenderRobitItem(BlockStateModelPart model) {
            this.model = model;
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords, int overlayCoords,
                boolean hasFoil, int outlineColor) {
            BlockModelRenderState blockModel = new BlockModelRenderState();
            blockModel.setupModel(new Matrix4f(), true).add(model);
            blockModel.submit(poseStack, nodeCollector, lightCoords, overlayCoords, outlineColor);
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
            if (model instanceof SimpleModelWrapper simpleModelWrapper) {
                CuboidItemModelWrapper.computeExtents(simpleModelWrapper.getQuads(null));
            }
        }
    }
}