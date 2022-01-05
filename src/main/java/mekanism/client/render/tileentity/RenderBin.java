package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.tier.BinTier;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@ParametersAreNonnullByDefault
public class RenderBin extends MekanismTileEntityRenderer<TileEntityBin> {

    private static final Matrix3f FAKE_NORMALS;

    static {
        Vector3f NORMAL = new Vector3f(1, 1, 1);
        NORMAL.normalize();
        FAKE_NORMALS = new Matrix3f(new Quaternion(NORMAL, 0, true));
    }

    public RenderBin(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityBin tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        Level world = tile.getLevel();
        BinInventorySlot binSlot = tile.getBinSlot();
        if (world != null && !binSlot.isEmpty()) {
            Direction facing = tile.getDirection();
            //position of the block covering the front side
            BlockPos coverPos = tile.getBlockPos().relative(facing);
            //if the bin has an item stack and the face isn't covered by a solid side
            Optional<BlockState> blockState = WorldUtils.getBlockState(world, coverPos);
            if (blockState.isEmpty() || !blockState.get().canOcclude() || !blockState.get().isFaceSturdy(world, coverPos, facing.getOpposite())) {
                Component amount = tile.getTier() == BinTier.CREATIVE ? MekanismLang.INFINITE.translate() : TextComponentUtil.build(binSlot.getCount());
                matrix.pushPose();
                //TODO: Come up with a better way to do this hack? Basically we adjust the normals so that the lighting
                // isn't screwy when it tries to apply the diffuse lighting as we aren't able to disable diffuse lighting
                // ourselves so need to trick it
                matrix.last().normal().load(FAKE_NORMALS);
                switch (facing) {
                    case NORTH -> {
                        matrix.translate(0.73, 0.83, -0.0001);
                        matrix.mulPose(Vector3f.YP.rotationDegrees(180));
                    }
                    case SOUTH -> matrix.translate(0.27, 0.83, 1.0001);
                    case WEST -> {
                        matrix.translate(-0.0001, 0.83, 0.27);
                        matrix.mulPose(Vector3f.YP.rotationDegrees(-90));
                    }
                    case EAST -> {
                        matrix.translate(1.0001, 0.83, 0.73);
                        matrix.mulPose(Vector3f.YP.rotationDegrees(90));
                    }
                }

                float scale = 0.03125F;
                float scaler = 0.9F;
                matrix.scale(scale * scaler, scale * scaler, 0.0001F);
                matrix.translate(8, -8, 8);
                matrix.scale(16, 16, 16);
                //Calculate lighting based on the light at the block the bin is facing
                light = LevelRenderer.getLightColor(world, tile.getBlockPos().relative(facing));
                Minecraft.getInstance().getItemRenderer().renderStatic(binSlot.getStack(), TransformType.GUI, light, overlayLight, matrix, renderer,
                      MathUtils.clampToInt(tile.getBlockPos().asLong()));
                matrix.popPose();
                renderText(matrix, renderer, light, overlayLight, amount, facing, 0.02F);
            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.BIN;
    }

    @SuppressWarnings("incomplete-switch")
    private void renderText(@Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer, int light, int overlayLight, Component text, Direction side,
          float maxScale) {
        matrix.pushPose();
        matrix.translate(0, -0.3725, 0);
        switch (side) {
            case SOUTH -> {
                matrix.translate(0, 1, 0.0001);
                matrix.mulPose(Vector3f.XP.rotationDegrees(90));
            }
            case NORTH -> {
                matrix.translate(1, 1, 0.9999);
                matrix.mulPose(Vector3f.YP.rotationDegrees(180));
                matrix.mulPose(Vector3f.XP.rotationDegrees(90));
            }
            case EAST -> {
                matrix.translate(0.0001, 1, 1);
                matrix.mulPose(Vector3f.YP.rotationDegrees(90));
                matrix.mulPose(Vector3f.XP.rotationDegrees(90));
            }
            case WEST -> {
                matrix.translate(0.9999, 1, 0);
                matrix.mulPose(Vector3f.YP.rotationDegrees(-90));
                matrix.mulPose(Vector3f.XP.rotationDegrees(90));
            }
        }

        float displayWidth = 1;
        float displayHeight = 1;
        matrix.translate(displayWidth / 2, 1, displayHeight / 2);
        matrix.mulPose(Vector3f.XP.rotationDegrees(-90));

        Font font = context.getFont();

        int requiredWidth = Math.max(font.width(text), 1);
        int requiredHeight = font.lineHeight + 2;
        float scaler = 0.4F;
        float scaleX = displayWidth / requiredWidth;
        float scale = scaleX * scaler;
        if (maxScale > 0) {
            scale = Math.min(scale, maxScale);
        }

        matrix.scale(scale, -scale, scale);
        int realHeight = (int) Math.floor(displayHeight / scale);
        int realWidth = (int) Math.floor(displayWidth / scale);
        int offsetX = (realWidth - requiredWidth) / 2;
        int offsetY = (realHeight - requiredHeight) / 2;
        font.drawInBatch(TextComponentUtil.build(EnumColor.WHITE, text), offsetX - realWidth / 2, 1 + offsetY - realHeight / 2, overlayLight,
              false, matrix.last().pose(), renderer, false, 0, light);
        matrix.popPose();
    }
}