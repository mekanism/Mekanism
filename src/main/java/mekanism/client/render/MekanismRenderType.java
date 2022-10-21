package mekanism.client.render;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class MekanismRenderType extends RenderType {

    //Ignored
    private MekanismRenderType(String name, VertexFormat format, Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState,
          Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    private static final RenderStateShard.TransparencyStateShard BLADE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("mek_blade_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    private static final RenderStateShard.TransparencyStateShard PARTICLE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("mek_particle_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }, RenderSystem::disableBlend);
    private static final RenderStateShard.ShaderStateShard PARTICLE_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getParticleShader);

    public static final RenderType MEK_LIGHTNING = create("mek_lightning", DefaultVertexFormat.POSITION_COLOR, Mode.QUADS, 256,
          false, true, RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
                .setTransparencyState(LIGHTNING_TRANSPARENCY)
                .createCompositeState(false)
    );

    public static final Function<ResourceLocation, RenderType> STANDARD = Util.memoize(resourceLocation ->
          createStandard("mek_standard", resourceLocation, UnaryOperator.identity(), false));
    public static final Function<ResourceLocation, RenderType> STANDARD_TRANSLUCENT_TARGET = Util.memoize(resourceLocation ->
          createStandard("mek_standard_translucent_target", resourceLocation, state -> state.setOutputState(RenderType.TRANSLUCENT_TARGET), true));
    public static final Function<ResourceLocation, RenderType> ALARM = Util.memoize(resourceLocation ->
          createStandard("mek_alarm", resourceLocation, state -> state.setCullState(NO_CULL).setOutputState(RenderType.TRANSLUCENT_TARGET), true));
    //Similar to mekStandard but blurs the texture
    public static final Function<ResourceLocation, RenderType> JETPACK_GLASS = Util.memoize(resourceLocation -> createStandard("mek_jetpack_glass", resourceLocation,
          state -> state.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, true, false)), false));

    private static RenderType createStandard(String name, ResourceLocation resourceLocation, UnaryOperator<RenderType.CompositeState.CompositeStateBuilder> stateModifier,
          boolean sortOnUpload) {
        RenderType.CompositeState state = stateModifier.apply(RenderType.CompositeState.builder()
              .setShaderState(NEW_ENTITY_SHADER)
              .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
              .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
        ).createCompositeState(true);
        return create(name, DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, sortOnUpload, state);
    }

    public static final Function<ResourceLocation, RenderType> BLADE = Util.memoize(resourceLocation -> {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
              .setShaderState(RenderStateShard.NEW_ENTITY_SHADER)
              .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
              .setTransparencyState(BLADE_TRANSPARENCY)
              .createCompositeState(true);
        return create("mek_blade", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, false, state);
    });

    public static final Function<ResourceLocation, RenderType> FLAME = Util.memoize(resourceLocation -> {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
              .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
              .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
              .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
              .createCompositeState(true);
        return create("mek_flame", DefaultVertexFormat.POSITION_COLOR_TEX, Mode.QUADS, 256, true, false, state);
    });

    public static final RenderType NUTRITIONAL_PARTICLE = create("mek_nutritional_particle", DefaultVertexFormat.PARTICLE, Mode.QUADS,
          256, false, false, RenderType.CompositeState.builder()
                .setShaderState(PARTICLE_SHADER)
                .setTextureState(BLOCK_SHEET)
                .setTransparencyState(PARTICLE_TRANSPARENCY)
                .setLightmapState(LIGHTMAP)
                .createCompositeState(false)
    );

    public static final RenderType MEKASUIT = create("mekasuit", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 131_072, true, false,
          RenderType.CompositeState.builder()
                .setShaderState(MekanismShaders.MEKASUIT.shard)
                .setTextureState(BLOCK_SHEET)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(true)
    );

    public static final Function<ResourceLocation, RenderType> SPS = Util.memoize(resourceLocation -> {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
              .setShaderState(MekanismShaders.SPS.shard)
              .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
              .setTransparencyState(LIGHTNING_TRANSPARENCY)
              .setOutputState(RenderType.TRANSLUCENT_TARGET)
              .createCompositeState(true);
        return create("mek_sps", DefaultVertexFormat.POSITION_COLOR_TEX, Mode.QUADS, 256, true, true, state);
    });
}