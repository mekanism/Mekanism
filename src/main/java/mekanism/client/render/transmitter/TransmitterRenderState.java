package mekanism.client.render.transmitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderResizableCuboid;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

//TODO - 26.1: Do we want to override fillCrashReportCategory to add more details to it?
@NothingNullByDefault
public class TransmitterRenderState extends BlockEntityRenderState {

    @Nullable
    public List<String> connectionContents;

    public static class BufferedTransmitterRenderState extends TransmitterRenderState {
        public float currentScale = 1;
    }

    public static class CableRenderState extends BufferedTransmitterRenderState {
    }

    public static class ConductorRenderState extends TransmitterRenderState {

        public int tempColor = 0xFFFFFFFF;
    }

    public static class PipeRenderState extends BufferedTransmitterRenderState {

        @Nullable
        public RenderResizableCuboid.TexturePicker fluidTexture;
        public int fluidTint = 0XFFFFFFFF;
        public int glow;
        public int stage;
        public List<MekanismRenderer.Model3D> sideModels = new ArrayList<>();
        @Nullable
        public MekanismRenderer.Model3D model;
    }

    public static class TransporterRenderState extends TransmitterRenderState {

        public List<TransporterStackRenderState> stacks = Collections.emptyList();


        public static class DiversionTransporterRenderState extends TransporterRenderState {

            @Nullable
            public SpriteId overlay;
        }

        public record TransporterStackRenderState(Vector3f stackPos, ItemStackRenderState item, @Nullable EnumColor color) {

            public TransporterStackRenderState(Vector3f stackPos, @Nullable EnumColor color) {
                this(stackPos, new ItemStackRenderState(), color);
            }
        }
    }

    public static class TubeRenderState extends BufferedTransmitterRenderState {

        @Nullable
        public TextureAtlasSprite chemicalTexture;
        public int chemicalTint = 0XFFFFFF;
    }
}