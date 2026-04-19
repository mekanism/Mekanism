package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Optional;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.render.tileentity.RenderBin.BinRenderState;
import mekanism.common.MekanismLang;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.tier.BinTier;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@NothingNullByDefault
public class RenderBin extends MekanismTileEntityRenderer<TileEntityBin, BinRenderState> {

    private static final Matrix3f FAKE_NORMALS = Util.make(() -> {
        Vector3f NORMAL = new Vector3f(1, 1, 1);
        NORMAL.normalize();
        return new Matrix3f().set(new Quaternionf().setAngleAxis(0, NORMAL.x, NORMAL.y, NORMAL.z));
    });

    private final ItemModelResolver itemModelResolver;
    private final Font font;

    public RenderBin(BlockEntityRendererProvider.Context context) {
        super(context);
        this.font = context.font();
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public BinRenderState createRenderState() {
        return new BinRenderState();
    }

    @Override
    public void extractRenderState(TileEntityBin bin, BinRenderState state, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(bin, state, partialTick, cameraPosition, breakProgress);
        Level level = bin.getLevel();
        BinInventorySlot binSlot = bin.getBinSlot();
        if (level != null && (!binSlot.isEmpty() || binSlot.isLocked())) {
            state.facing = bin.getDirection();
            //position of the block covering the front side
            BlockPos coverPos = state.blockPos.relative(state.facing);
            //if the bin has an item stack and the face isn't covered by a solid side
            Optional<BlockState> blockState = WorldUtils.getBlockState(level, coverPos);
            if (blockState.isEmpty() || !blockState.get().canOcclude() || !blockState.get().isFaceSturdy(level, coverPos, state.facing.getOpposite())) {
                //Calculate lighting based on the light at the block the bin is facing
                state.lightCoords = LevelRenderer.getLightCoords(level, coverPos);
                //TODO - 26.1: Evaluate the seed we are passing, and if we want to use this as the seed for transporters or if maybe we should be using zero here as well?
                int seed = MathUtils.clampToInt(state.blockPos.asLong());
                //TODO - 26.1: Is this going to try and display a stack of items, or will it display a single one? If a stack we need to return a single sized item
                this.itemModelResolver.updateForTopItem(state.item, binSlot.getRenderStack(), ItemDisplayContext.GUI, level, null, seed);
                if (bin.getTier() == BinTier.CREATIVE) {
                    state.displayCount = MekanismLang.INFINITE.translateColored(EnumColor.WHITE);
                } else {
                    state.displayCount = TextComponentUtil.build(binSlot.isLocked() ? EnumColor.AQUA : EnumColor.WHITE, binSlot.getCount());
                }
            } else {
                //TODO - 26.1: Re-evaluate how we want to do this. This just makes it so that we don't actually submit any rendering,
                // but we should see if we can just put some of this stuff in the should render?
                state.facing = null;
            }
        }
    }

    @Override
    public void submit(BinRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.facing != null) {
            poseStack.pushPose();
            switch (state.facing) {
                case NORTH -> {
                    poseStack.translate(0.71, 0.8, -0.0001);
                    poseStack.mulPose(Axis.YP.rotationDegrees(180));
                }
                case SOUTH -> poseStack.translate(0.29, 0.8, 1.0001);
                case WEST -> {
                    poseStack.translate(-0.0001, 0.8, 0.29);
                    poseStack.mulPose(Axis.YP.rotationDegrees(-90));
                }
                case EAST -> {
                    poseStack.translate(1.0001, 0.8, 0.71);
                    poseStack.mulPose(Axis.YP.rotationDegrees(90));
                }
            }

            poseStack.scale(0.025F, 0.025F, 0.0001F);
            poseStack.translate(8, -8, 8);
            poseStack.scale(16, 16, 16);
            //TODO: Come up with a better way to do this hack? Basically we adjust the normals so that the lighting
            // isn't screwy when it tries to apply the diffuse lighting as we aren't able to disable diffuse lighting
            // ourselves so need to trick it
            poseStack.last().normal().set(FAKE_NORMALS);
            state.item.submit(poseStack, nodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
            if (state.displayCount != null) {
                poseStack.pushPose();
                poseStack.translate(0, -0.25, 0);
                switch (state.facing) {
                    case SOUTH -> {
                        poseStack.translate(0, 1, 0);
                        poseStack.mulPose(Axis.XP.rotationDegrees(90));
                    }
                    case NORTH -> {
                        poseStack.translate(1, 1, 1);
                        poseStack.mulPose(Axis.YP.rotationDegrees(180));
                        poseStack.mulPose(Axis.XP.rotationDegrees(90));
                    }
                    case EAST -> {
                        poseStack.translate(0, 1, 1);
                        poseStack.mulPose(Axis.YP.rotationDegrees(90));
                        poseStack.mulPose(Axis.XP.rotationDegrees(90));
                    }
                    case WEST -> {
                        poseStack.translate(1, 1, 0);
                        poseStack.mulPose(Axis.YP.rotationDegrees(-90));
                        poseStack.mulPose(Axis.XP.rotationDegrees(90));
                    }
                }

                float displayWidth = 1;
                float displayHeight = 1;
                poseStack.translate(displayWidth / 2, 1, displayHeight / 2);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));

                int requiredWidth = Math.max(font.width(state.displayCount), 1);
                int requiredHeight = font.lineHeight + 2;
                float textScale = 0.4F * (displayWidth / requiredWidth);
                //clamp how much we are willing to scale to ensure it remains readable
                textScale = Math.min(textScale, 0.02F);

                poseStack.scale(textScale, -textScale, textScale);
                int realHeight = Mth.floor(displayHeight / textScale);
                int realWidth = Mth.floor(displayWidth / textScale);
                int offsetX = (realWidth - requiredWidth) / 2;
                int offsetY = (realHeight - requiredHeight) / 2;
                nodeCollector.submitText(
                      poseStack,
                      offsetX - realWidth / 2,
                      1 + offsetY - realHeight / 2,
                      state.displayCount.getVisualOrderText(),
                      false,
                      DisplayMode.POLYGON_OFFSET,
                      state.lightCoords,
                      0xFFFFFFFF,//TODO - 26.1: What color do we want to be using?
                      0,
                      0

                );
                poseStack.popPose();

            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.BIN;
    }

    public static class BinRenderState extends BlockEntityRenderState {

        public final ItemStackRenderState item = new ItemStackRenderState();
        @Nullable
        public Component displayCount;
        @Nullable
        public Direction facing;
    }
}