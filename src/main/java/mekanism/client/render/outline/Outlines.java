package mekanism.client.render.outline;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.common.Mekanism;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeCustomSelectionBox;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.joml.Math;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT)
public class Outlines {

    private static final Map<BlockState, List<Line>> cachedWireFrames = new Reference2ObjectOpenHashMap<>();

    @SubscribeEvent
    public static void bakingCompleted(ModelEvent.BakingCompleted event) {
        cachedWireFrames.clear();
    }

    private static List<Line> getOutlinesFromModel(ClientLevel level, BlockPos pos, BlockState state) {
        List<Line> lines = cachedWireFrames.get(state);
        if (lines == null) {
            BlockStateModel bakedModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
            lines = extract(level, pos, state, bakedModel);
            cachedWireFrames.put(state, lines);
        }
        return lines;
    }

    public static void onBlockHover(ExtractBlockOutlineRenderStateEvent event, ProfilerFiller profiler) {
        profiler.push(ProfilerConstants.MEKANISM_OUTLINE);
        ClientLevel level = event.getLevel();
        BlockPos pos = event.getBlockPos();
        BlockState blockState = event.getBlockState();
        if (!blockState.isAir() && level.getWorldBorder().isWithinBounds(pos)) {
            BlockPos actualPos = pos;
            BlockState actualState = blockState;
            if (blockState.is(MekanismBlocks.BOUNDING_BLOCK)) {
                BlockPos mainPos = BlockBounding.getMainBlockPos(level, pos);
                if (mainPos != null) {
                    actualPos = mainPos;
                    actualState = level.getBlockState(actualPos);
                }
            }
            AttributeCustomSelectionBox customSelectionBox = Attribute.get(actualState, AttributeCustomSelectionBox.class);
            if (customSelectionBox != null) {
                if (customSelectionBox.isJavaModel()) {
                    //If we use a TER to render the wire frame, grab the tile
                    BlockEntity tile = WorldUtils.getTileEntity(level, actualPos);
                    if (tile != null) {
                        BlockEntityRenderer<BlockEntity, ?> tileRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(tile);
                        if (tileRenderer instanceof IWireFrameRenderer wireFrameRenderer && wireFrameRenderer.hasSelectionBox(actualState)) {
                            List<Line> outlinesFromModel = wireFrameRenderer.isCombined() ? getOutlinesFromModel(level, actualPos, actualState) : Collections.emptyList();
                            event.addCustomRenderer(new IWireframeRendererHandler(actualPos, outlinesFromModel, wireFrameRenderer, tile));
                        }
                    }
                } else {
                    //Otherwise, skip getting the tile and just grab the model
                    List<Line> outlinesFromModel = getOutlinesFromModel(level, actualPos, actualState);
                    event.addCustomRenderer(new MekanismOutlineRenderer(actualPos, outlinesFromModel));
                }
            }
        }
        profiler.pop();
    }

    public static List<Line> extract(ClientLevel level, BlockPos pos, BlockState state, BlockStateModel model) {
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(level, pos, state, level.getRandom(), parts);
        return extract(parts);
    }

    public static List<Line> extract(List<BlockStateModelPart> parts) {
        Set<Line> lines = new HashSet<>();
        for (BlockStateModelPart part : parts) {
            for (Direction direction : EnumUtils.DIRECTIONS) {
                for (BakedQuad quad : part.getQuads(direction)) {
                    unpackLines(quad, lines);
                }
            }

            for (BakedQuad quad : part.getQuads(null)) {
                unpackLines(quad, lines);
            }
        }
        return new ArrayList<>(lines);
    }

    private static void unpackLines(BakedQuad quad, Set<Line> lines) {
        addQuad(lines, quad.position0(), quad.position1(), quad.position2(), quad.position3());
    }

    public static void addQuad(Set<Line> lines, Vector3fc v1, Vector3fc v2, Vector3fc v3, Vector3fc v4) {
        lines.add(Line.from(v1, v2));
        lines.add(Line.from(v2, v3));
        lines.add(Line.from(v3, v4));
        lines.add(Line.from(v4, v1));
    }

    public record Line(float x1, float y1, float z1, float x2, float y2, float z2, float nX, float nY, float nZ, int hash) {

        public static Line from(Vector3fc v1, Vector3fc v2) {
            // normalise by the distance between the points
            float nX = v2.x() - v1.x();
            float nY = v2.y() - v1.y();
            float nZ = v2.z() - v1.z();
            float scalar = Math.invsqrt(Math.fma(nX, nX, Math.fma(nY, nY, nZ * nZ)));
            nX = nX * scalar;
            nY = nY * scalar;
            nZ = nZ * scalar;
            return new Line(v1.x(), v1.y(), v1.z(), v2.x(), v2.y(), v2.z(), nX, nY, nZ, calculateHash(v1.x(), v1.y(), v1.z(), v2.x(), v2.y(), v2.z()));
        }

        private static int calculateHash(float x1, float y1, float z1, float x2, float y2, float z2) {
            //Supports up to a scale of 0.005 in the json (which the miner uses for LEDs)
            int result = Long.hashCode((long) Math.min(x1, x2) * 3_200);
            result = 31 * result + Long.hashCode((long) Math.min(y1, y2) * 3_200);
            result = 31 * result + Long.hashCode((long) Math.min(z1, z2) * 3_200);
            result = 31 * result + Long.hashCode((long) Math.max(x1, x2) * 3_200);
            result = 31 * result + Long.hashCode((long) Math.max(x1, x2) * 3_200);
            result = 31 * result + Long.hashCode((long) Math.max(x1, x2) * 3_200);
            return result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == this) {
                return true;
            } else if (obj == null || obj.getClass() != Line.class) {
                return false;
            }
            Line other = (Line) obj;
            return (Mth.equal(x1, other.x1) && Mth.equal(y1, other.y1) && Mth.equal(z1, other.z1) && Mth.equal(x2, other.x2) && Mth.equal(y2, other.y2) && Mth.equal(z2, other.z2)) ||
                   (Mth.equal(x1, other.x2) && Mth.equal(y1, other.y2) && Mth.equal(z1, other.z2) && Mth.equal(x2, other.x1) && Mth.equal(y2, other.y1) && Mth.equal(z2, other.z1));
        }
    }
}
