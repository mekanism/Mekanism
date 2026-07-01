package mekanism.client.render.transmitter;

import java.util.Collections;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.RenderResizableCuboid.SideRender.SideRenderFlags;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.CommonColors;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

//TODO - 26.2: Do we want to override fillCrashReportCategory to add more details to it?
public class TransmitterRenderState extends BlockEntityRenderState {

    public List<BlockStateModelPart> contentsModel = Collections.emptyList();
    public int[] modelTint = BlockModelRenderState.EMPTY_TINTS;

    public static class PipeRenderState extends TransmitterRenderState {

        public RenderResizableCuboid.@Nullable TexturePicker fluidTexture;
        public int fluidTint = CommonColors.WHITE;
        public int stage;
        public boolean[] renderSideModel = new boolean[EnumUtils.DIRECTIONS.length];
        @SideRenderFlags
        public byte coreSideRender = 0;
        public boolean renderBase;
    }

    public static class TransporterRenderState extends TransmitterRenderState {

        public List<TransporterStackRenderState> stacks = Collections.emptyList();

        public record TransporterStackRenderState(Vector3f stackPos, ItemStackRenderState item, @Nullable EnumColor color) {

            public TransporterStackRenderState(Vector3f stackPos, @Nullable EnumColor color) {
                this(stackPos, new ItemStackRenderState(), color);
            }
        }
    }
}