package mekanism.client.render;

import java.util.function.Function;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class MekanismRenderType {

    //TODO - 26.3 render types
    /*
    private static final RenderStateShard.TransparencyStateShard PARTICLE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("mek_particle_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }, RenderSystem::disableBlend);
    private static final RenderStateShard.ShaderStateShard PARTICLE_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getParticleShader);*/

    //TODO - 26.3: Re-evaluate this
    public static final RenderType GUI_SPRITES = RenderType.create("mekanism_gui_sprite", RenderSetup.builder(RenderPipelines.GUI_TEXTURED)
          .withTexture("Sampler0", AtlasIds.GUI.withPrefix("textures/atlas/").withSuffix(".png"))
          .sortOnUpload()
          .createRenderSetup()
    );

    public static final Function<Identifier, RenderType> STANDARD = RenderTypes::entityTranslucent;
    public static final Function<Identifier, RenderType> ALARM = RenderTypes::entityTranslucent;
    //Similar to mekStandard but blurs the texture
    public static final Function<Identifier, RenderType> JETPACK_GLASS = RenderTypes::entityTranslucent;

    public static final Function<Identifier, RenderType> FLAME = RenderTypes::entityTranslucent;

    public static final RenderType NUTRITIONAL_PARTICLE = null;

    public static final RenderType MEKASUIT = RenderType.create("mekanism_mekasuit", RenderSetup.builder(MekanismRenderPipelines.MEKASUIT)
          .withTexture("Sampler0", TextureAtlas.LOCATION_ITEMS)
          .useLightmap()
          .useOverlay()//TODO - 26.3: I don't think we want the overlay?
          .affectsCrumbling()
          .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)//TODO - 26.3?: affectsOutline ? RenderSetup.OutlineProperty.AFFECTS_OUTLINE : RenderSetup.OutlineProperty.NONE)
          .createRenderSetup()
    );

    ///Copy of [RenderTypes#TRIMMED_ARMOR_GLINT] but without the view offset layering
    public static final RenderType ARMOR_GLINT = RenderType.create("mekanism_armor_entity_glint", RenderSetup.builder(RenderPipelines.GLINT)
          .withTexture("Sampler0", ItemFeatureRenderer.ENCHANTED_GLINT_ARMOR)
          .setTextureTransform(TextureTransform.ARMOR_ENTITY_GLINT_TEXTURING)
          //.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
          .withForcedSolidModelPhase()
          .createRenderSetup()
    );

    public static final Function<Identifier, RenderType> SPS = Util.memoize(resourceLocation -> RenderType.create("mekanism_sps", RenderSetup.builder(MekanismRenderPipelines.SPS)
          .withTexture("Sampler0", resourceLocation)
          .setOitPipelines(MekanismRenderPipelines.OIT_SPS)
          .sortOnUpload()
          .createRenderSetup()
    ));
}