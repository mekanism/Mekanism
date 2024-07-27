package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Optional;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.tier.BinTier;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.util.WorldUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@NothingNullByDefault
public class RenderBin extends MekanismTileEntityRenderer<TileEntityBin> {

    private static final Matrix3f FAKE_NORMALS = Util.make(() -> {
        Vector3f NORMAL = new Vector3f(1, 1, 1);
        NORMAL.normalize();
        return new Matrix3f().set(new Quaternionf().setAngleAxis(0, NORMAL.x, NORMAL.y, NORMAL.z));
    });

    public RenderBin(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityBin tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        Level world = tile.getLevel();
        BinInventorySlot binSlot = tile.getBinSlot();
        if (world != null && (!binSlot.isEmpty() || binSlot.isLocked())) {
            Direction facing = tile.getDirection();
            //position of the block covering the front side
            BlockPos coverPos = tile.getBlockPos().relative(facing);
            //if the bin has an item stack and the face isn't covered by a solid side
            Optional<BlockState> blockState = WorldUtils.getBlockState(world, coverPos);
            if (blockState.isEmpty() || !blockState.get().canOcclude() || !blockState.get().isFaceSturdy(world, coverPos, facing.getOpposite())) {
                matrix.pushPose();
                switch (facing) {
                    case NORTH -> {
                        matrix.translate(0.71, 0.8, -0.0001);
                        matrix.mulPose(Axis.YP.rotationDegrees(180));
                    }
                    case SOUTH -> matrix.translate(0.29, 0.8, 1.0001);
                    case WEST -> {
                        matrix.translate(-0.0001, 0.8, 0.29);
                        matrix.mulPose(Axis.YP.rotationDegrees(-90));
                    }
                    case EAST -> {
                        matrix.translate(1.0001, 0.8, 0.71);
                        matrix.mulPose(Axis.YP.rotationDegrees(90));
                    }
                }

                float scale = 0.025F;
                matrix.scale(scale, scale, 0.0001F);
                matrix.translate(8, -8, 8);
                matrix.scale(16, 16, 16);
                //TODO: Come up with a better way to do this hack? Basically we adjust the normals so that the lighting
                // isn't screwy when it tries to apply the diffuse lighting as we aren't able to disable diffuse lighting
                // ourselves so need to trick it
                matrix.last().normal().set(FAKE_NORMALS);
                //Calculate lighting based on the light at the block the bin is facing
                light = LevelRenderer.getLightColor(world, tile.getBlockPos().relative(facing));
                Minecraft.getInstance().getItemRenderer().renderStatic(binSlot.getRenderStack(), ItemDisplayContext.GUI, light, overlayLight, matrix, renderer, world,
                      MathUtils.clampToInt(tile.getBlockPos().asLong()));
                matrix.popPose();
                renderText(matrix, renderer, light, overlayLight, getCount(tile), facing, 0.02F);
            }
        }
    }

    protected Component getCount(TileEntityBin bin) {
        if (bin.getTier() == BinTier.CREATIVE) {
            return MekanismLang.INFINITE.translateColored(EnumColor.WHITE);
        }
        final BinInventorySlot slot = bin.getBinSlot();
        return TextComponentUtil.build(slot.isLocked() ? EnumColor.AQUA : EnumColor.WHITE, slot.getCount());
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.BIN;
    }

    @SuppressWarnings("incomplete-switch")
    private void renderText(@NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight, Component text, Direction side,
          float maxScale) {
        matrix.pushPose();
        matrix.translate(0, -0.25, 0);
        switch (side) {
            case SOUTH -> {
                matrix.translate(0, 1, 0);
                matrix.mulPose(Axis.XP.rotationDegrees(90));
            }
            case NORTH -> {
                matrix.translate(1, 1, 1);
                matrix.mulPose(Axis.YP.rotationDegrees(180));
                matrix.mulPose(Axis.XP.rotationDegrees(90));
            }
            case EAST -> {
                matrix.translate(0, 1, 1);
                matrix.mulPose(Axis.YP.rotationDegrees(90));
                matrix.mulPose(Axis.XP.rotationDegrees(90));
            }
            case WEST -> {
                matrix.translate(1, 1, 0);
                matrix.mulPose(Axis.YP.rotationDegrees(-90));
                matrix.mulPose(Axis.XP.rotationDegrees(90));
            }
        }

        float displayWidth = 1;
        float displayHeight = 1;
        matrix.translate(displayWidth / 2, 1, displayHeight / 2);
        matrix.mulPose(Axis.XP.rotationDegrees(-90));

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
        int realHeight = Mth.floor(displayHeight / scale);
        int realWidth = Mth.floor(displayWidth / scale);
        int offsetX = (realWidth - requiredWidth) / 2;
        int offsetY = (realHeight - requiredHeight) / 2;
        font.drawInBatch(text, offsetX - realWidth / 2, 1 + offsetY - realHeight / 2, overlayLight,
              false, matrix.last().pose(), renderer, DisplayMode.POLYGON_OFFSET, 0, light);
        matrix.popPose();
    }
}