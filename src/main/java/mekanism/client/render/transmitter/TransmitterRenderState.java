package mekanism.client.render.transmitter;

import java.util.Collections;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.RenderResizableCuboid.SideRender.SideRenderFlags;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.CommonColors;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

//TODO - 26.2: Do we want to override fillCrashReportCategory to add more details to it?
public class TransmitterRenderState extends BlockEntityRenderState {

    @Nullable
    public List<String> connectionContents;

    public static class BufferedTransmitterRenderState extends TransmitterRenderState {
        public float currentScale = 1;
    }

    public static class CableRenderState extends BufferedTransmitterRenderState {
    }

    public static class ConductorRenderState extends TransmitterRenderState {

        public int tempColor = CommonColors.WHITE;
    }

    public static class PipeRenderState extends BufferedTransmitterRenderState {

        public RenderResizableCuboid.@Nullable TexturePicker fluidTexture;
        public int fluidTint = CommonColors.WHITE;
        public int glow;
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

    public static class TubeRenderState extends BufferedTransmitterRenderState {

        @Nullable
        public TextureAtlasSprite chemicalTexture;
        public int chemicalTint = CommonColors.WHITE;
    }
}