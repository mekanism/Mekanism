package mekanism.generators.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BlockItem;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

/**
 * 26.1 SpecialModelRenderer for Advanced Solar Generator item.
 * <p>
 * Applies a {@code translate(0, 1, 0)} offset to the block model before submission,
 * matching the legacy {@code TransformedBakedModel} offset that was removed with
 * {@code ModifyBakingResult}.
 */
@NullMarked
public class RenderAdvancedSolarItem implements SpecialModelRenderer<RenderAdvancedSolarItem.SolarState> {

    public RenderAdvancedSolarItem() {
    }

    @Override
    public void submit(@Nullable SolarState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (state == null) {
            return;
        }
        // Apply the legacy +1 Y offset that used to come from TransformedBakedModel
        poseStack.pushPose();
        poseStack.translate(0, 1, 0);
        state.blockModelRenderState.submit(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor);
        poseStack.popPose();
    }

    @Override
    public void getExtents(java.util.function.Consumer<org.joml.Vector3fc> output) {
        // Let the block model extents drive this
    }

    @Nullable
    @Override
    public SolarState extractArgument(ItemStack stack) {
        BlockState blockState = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
        BlockModelRenderState blockModel = new BlockModelRenderState();
        Minecraft.getInstance().getBlockModelResolver().update(blockModel, blockState, mekanism.client.ModelUtil.BLOCK_DISPLAY_NO_CONTEXT);
        return new SolarState(blockModel);
    }

    public record SolarState(BlockModelRenderState blockModelRenderState) {
    }

    public static class Unbaked implements SpecialModelRenderer.Unbaked<SolarState> {

        public static final Unbaked INSTANCE = new Unbaked();
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        @Nullable
        public SpecialModelRenderer<SolarState> bake(net.minecraft.client.renderer.special.SpecialModelRenderer.BakingContext context) {
            return new RenderAdvancedSolarItem();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<SolarState>> type() {
            return MAP_CODEC;
        }
    }
}
