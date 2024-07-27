package mekanism.client.render;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class MekanismRenderType {

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

    public static final RenderType MEK_LIGHTNING = RenderType.create("mek_lightning", DefaultVertexFormat.POSITION_COLOR, Mode.QUADS, 256,
          false, true, RenderType.CompositeState.builder()
                .setShaderState(RenderType.RENDERTYPE_LIGHTNING_SHADER)
                .setTransparencyState(RenderType.LIGHTNING_TRANSPARENCY)
                .createCompositeState(false)
    );

    public static final Function<ResourceLocation, RenderType> STANDARD = Util.memoize(resourceLocation ->
          createStandard("mek_standard", resourceLocation, UnaryOperator.identity(), false));
    public static final Function<ResourceLocation, RenderType> STANDARD_TRANSLUCENT_TARGET = Util.memoize(resourceLocation ->
          createStandard("mek_standard_translucent_target", resourceLocation, state -> state.setOutputState(RenderType.TRANSLUCENT_TARGET), true));
    public static final Function<ResourceLocation, RenderType> ALARM = Util.memoize(resourceLocation ->
          createStandard("mek_alarm", resourceLocation, state -> state.setCullState(RenderType.NO_CULL).setOutputState(RenderType.TRANSLUCENT_TARGET), true));
    //Similar to mekStandard but blurs the texture
    public static final Function<ResourceLocation, RenderType> JETPACK_GLASS = Util.memoize(resourceLocation -> createStandard("mek_jetpack_glass", resourceLocation,
          state -> state.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, true, false)), false));

    private static RenderType createStandard(String name, ResourceLocation resourceLocation, UnaryOperator<RenderType.CompositeState.CompositeStateBuilder> stateModifier,
          boolean sortOnUpload) {
        RenderType.CompositeState state = stateModifier.apply(RenderType.CompositeState.builder()
              //Note: We use the eyes shader as it is effectively equivalent to NEW_ENTITY except takes fog into account for purposes of
              // things like blindness and darkness
              .setShaderState(RenderType.RENDERTYPE_EYES_SHADER)
              .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
              .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
        ).createCompositeState(true);
        return RenderType.create(name, DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, sortOnUpload, state);
    }

    public static final Function<ResourceLocation, RenderType> BLADE = Util.memoize(resourceLocation -> {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
              //Note: We use the eyes shader as it is effectively equivalent to NEW_ENTITY except takes fog into account for purposes of
              // things like blindness and darkness
              .setShaderState(RenderType.RENDERTYPE_EYES_SHADER)
              .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
              .setTransparencyState(BLADE_TRANSPARENCY)
              .createCompositeState(true);
        return RenderType.create("mek_blade", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, false, state);
    });

    public static final Function<ResourceLocation, RenderType> FLAME = Util.memoize(resourceLocation -> {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
              .setShaderState(MekanismShaders.FLAME.shard)
              .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
              .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
              .createCompositeState(true);
        return RenderType.create("mek_flame", DefaultVertexFormat.POSITION_TEX_COLOR, Mode.QUADS, 256, true, false, state);
    });

    public static final RenderType NUTRITIONAL_PARTICLE = RenderType.create("mek_nutritional_particle", DefaultVertexFormat.PARTICLE, Mode.QUADS,
          256, false, false, RenderType.CompositeState.builder()
                .setShaderState(PARTICLE_SHADER)
                .setTextureState(RenderType.BLOCK_SHEET)
                .setTransparencyState(PARTICLE_TRANSPARENCY)
                .setLightmapState(RenderType.LIGHTMAP)
                .createCompositeState(false)
    );

    public static final RenderType MEKASUIT = RenderType.create("mekasuit", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 131_072, true, false,
          RenderType.CompositeState.builder()
                .setShaderState(MekanismShaders.MEKASUIT.shard)
                .setTextureState(RenderType.BLOCK_SHEET)
                .setLightmapState(RenderType.LIGHTMAP)
                .setOverlayState(RenderType.OVERLAY)
                .createCompositeState(true)
    );

    public static final Function<ResourceLocation, RenderType> SPS = Util.memoize(resourceLocation -> {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
              .setShaderState(MekanismShaders.SPS.shard)
              .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
              .setTransparencyState(RenderType.LIGHTNING_TRANSPARENCY)
              .setOutputState(RenderType.TRANSLUCENT_TARGET)
              .createCompositeState(false);
        return RenderType.create("mek_sps", DefaultVertexFormat.POSITION_TEX_COLOR, Mode.QUADS, 1_536, false, true, state);
    });
}