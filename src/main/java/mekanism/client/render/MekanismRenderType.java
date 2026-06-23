package mekanism.client.render;

import java.util.function.Function;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

public class MekanismRenderType {
//TODO - 26.2 render types
    /*
    private static final RenderStateShard.TransparencyStateShard PARTICLE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("mek_particle_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }, RenderSystem::disableBlend);
    private static final RenderStateShard.ShaderStateShard PARTICLE_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getParticleShader);
*/
public static final RenderType MEK_LIGHTNING = RenderTypes.lightning();/*RenderType.create("mek_lightning", DefaultVertexFormat.POSITION_COLOR, Mode.QUADS, 256,
          false, true, RenderType.CompositeState.builder()
                .setShaderState(RenderType.RENDERTYPE_LIGHTNING_SHADER)
                .setTransparencyState(RenderType.LIGHTNING_TRANSPARENCY)
                .createCompositeState(false)
    );*/

    //TODO - 26.2: Re-evaluate this
    public static final RenderType GUI_SPRITES = RenderType.create("mekanism_gui_sprite", RenderSetup.builder(RenderPipelines.GUI_TEXTURED)
          .withTexture("Sampler0", AtlasIds.GUI.withPrefix("textures/atlas/").withSuffix(".png"))
          .sortOnUpload()
          .createRenderSetup()
    );

    public static final Function<Identifier, RenderType> STANDARD = RenderTypes::entityCutout;/*Util.memoize(resourceLocation ->
          createStandard("mek_standard", resourceLocation, UnaryOperator.identity(), false));*/
    public static final Function<Identifier, RenderType> STANDARD_TRANSLUCENT_TARGET = RenderTypes::entityTranslucent;/*Util.memoize(resourceLocation ->
          createStandard("mek_standard_translucent_target", resourceLocation, state -> state.setOutputState(RenderType.TRANSLUCENT_TARGET), true));*/
    public static final Function<Identifier, RenderType> ALARM = RenderTypes::entityTranslucent;/*Util.memoize(resourceLocation ->
          createStandard("mek_alarm", resourceLocation, state -> state.setCullState(RenderType.NO_CULL).setOutputState(RenderType.TRANSLUCENT_TARGET), true));*/
    //Similar to mekStandard but blurs the texture
    public static final Function<Identifier, RenderType> JETPACK_GLASS = RenderTypes::entityTranslucent;/*Util.memoize(resourceLocation -> createStandard("mek_jetpack_glass", resourceLocation,
          state -> state.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, true, false)), false));*/

    /*private static RenderType createStandard(String name, Identifier resourceLocation, UnaryOperator<RenderType.CompositeState.CompositeStateBuilder> stateModifier,
          boolean sortOnUpload) {
        RenderType.CompositeState state = stateModifier.apply(RenderType.CompositeState.builder()
              //Note: We use the eyes shader as it is effectively equivalent to NEW_ENTITY except takes fog into account for purposes of
              // things like blindness and darkness
              .setShaderState(RenderType.RENDERTYPE_EYES_SHADER)
              .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
              .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
        ).createCompositeState(true);
        return RenderType.create(name, DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, sortOnUpload, state);
    }*/

    public static final Function<Identifier, RenderType> FLAME = RenderTypes::entityTranslucent;/*Util.memoize(resourceLocation -> {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
              .setShaderState(MekanismShaders.FLAME.shard)
              .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
              .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
              .createCompositeState(true);
        return RenderType.create("mek_flame", DefaultVertexFormat.POSITION_TEX_COLOR, Mode.QUADS, 256, true, false, state);
    });*/

    public static final RenderType NUTRITIONAL_PARTICLE = null;/*RenderType.create("mek_nutritional_particle", DefaultVertexFormat.PARTICLE, Mode.QUADS,
          256, false, false, RenderType.CompositeState.builder()
                .setShaderState(PARTICLE_SHADER)
                .setTextureState(RenderType.BLOCK_SHEET)
                .setTransparencyState(PARTICLE_TRANSPARENCY)
                .setLightmapState(RenderType.LIGHTMAP)
                .createCompositeState(false)
    );*/

    public static final RenderType MEKASUIT = null;/*RenderType.create("mekasuit", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 131_072, true, false,
          RenderType.CompositeState.builder()
                .setShaderState(MekanismShaders.MEKASUIT.shard)
                .setTextureState(RenderType.BLOCK_SHEET)
                .setLightmapState(RenderType.LIGHTMAP)
                .setOverlayState(RenderType.OVERLAY)
                .createCompositeState(true)
    );*/

    public static final Function<Identifier, RenderType> SPS = null;/*Util.memoize(resourceLocation -> {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
              .setShaderState(MekanismShaders.SPS.shard)
              .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
              .setTransparencyState(RenderType.LIGHTNING_TRANSPARENCY)
              .setOutputState(RenderType.TRANSLUCENT_TARGET)
              .createCompositeState(false);
        return RenderType.create("mek_sps", DefaultVertexFormat.POSITION_TEX_COLOR, Mode.QUADS, 1_536, false, true, state);
    });*/
}