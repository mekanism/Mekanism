package mekanism.client.render;

import java.util.function.Function;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class MekanismRenderType {

    //TODO - 26.2 render types
    /*
    private static final RenderStateShard.TransparencyStateShard PARTICLE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("mek_particle_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }, RenderSystem::disableBlend);
    private static final RenderStateShard.ShaderStateShard PARTICLE_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getParticleShader);*/
    //TODO - 26.2: Can this just be replaced with LIGHTNING? Only difference is the output target
    public static final RenderType MEK_LIGHTNING = RenderType.create("mekanism_lightning", RenderSetup.builder(RenderPipelines.LIGHTNING)
          .sortOnUpload()
          .createRenderSetup()
    );

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

    public static final RenderType MEKASUIT = RenderType.create("mekanism_mekasuit", RenderSetup.builder(RenderPipelines.ENTITY_CUTOUT)
          .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)//TODO - 26.2: Is this the correct atlas?
          .useLightmap()
          .useOverlay()//TODO - 26.2: I don't think we want the overlay?
          .affectsCrumbling()
          .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)//TODO - 26.2?: affectsOutline ? RenderSetup.OutlineProperty.AFFECTS_OUTLINE : RenderSetup.OutlineProperty.NONE)
          .createRenderSetup()
    );

    public static final Function<Identifier, RenderType> SPS = Util.memoize(resourceLocation -> RenderType.create("mekanism_sps", RenderSetup.builder(MekanismRenderPipelines.SPS)
          .withTexture("Sampler0", resourceLocation)
          .sortOnUpload()
          .createRenderSetup()
    ));
}