package mekanism.client.render;

import java.util.function.Function;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class MekanismRenderType {

    //TODO - 26.3: Re-evaluate this (and if we need this to support translucency)
    public static final RenderType GUI_SPRITES = RenderType.create("mekanism_gui_sprite", RenderSetup.builder(RenderPipelines.GUI_TEXTURED)
          .withTexture("Sampler0", AtlasIds.GUI.withPrefix("textures/atlas/").withSuffix(".png"))
          .sortOnUpload()
          .createRenderSetup()
    );

    public static final Function<Identifier, RenderType> FLAME = RenderTypes::entityTranslucent;

    /// Similar to [RenderTypes#armorCutoutNoCull(Identifier)] but uses our own render pipeline
    public static final RenderType MEKASUIT = RenderType.create("mekanism_mekasuit", RenderSetup.builder(MekanismRenderPipelines.MEKASUIT)
          .withTexture("Sampler0", TextureAtlas.LOCATION_ITEMS)
          .useLightmap()
          .useOverlay()
          //Note: We don't need to do z offset layering as we disable rendering of the body
          //.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
          .affectsCrumbling()
          .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
          .createRenderSetup()
    );

    /// Similar to [RenderTypes#armorCutoutNoCullGlint(Identifier)] but uses our own render pipeline so that it applies correctly when the armor is tinted
    public static final RenderType MEKASUIT_GLINT = RenderType.create("mekanism_mekasuit_glint", RenderSetup.builder(MekanismRenderPipelines.MEKASUIT_GLINT)
          .withTexture("Sampler0", TextureAtlas.LOCATION_ITEMS)
          .withTexture("GlintSampler", ItemFeatureRenderer.ENCHANTED_GLINT_ARMOR)
          .setTextureTransform(TextureTransform.ARMOR_ENTITY_GLINT_TEXTURING)
          .useLightmap()
          .useOverlay()
          .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
          .affectsCrumbling()
          .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
          .createRenderSetup()
    );

    /// Similar to [RenderTypes#armorCutoutNoCullGlint(Identifier)] but sorts it, and has translucent handling pipelines.
    public static final RenderType ARMOR_TRANSLUCENT_GLINT = RenderType.create("mekanism_armor_translucent_glint", RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT)
          .setOitPipelines(MekanismRenderPipelines.OIT_ARMOR_TRANSLUCENT_GLINT)
          .withTexture("Sampler0", TextureAtlas.LOCATION_ITEMS)
          .withTexture("GlintSampler", ItemFeatureRenderer.ENCHANTED_GLINT_ARMOR)
          .setTextureTransform(TextureTransform.ARMOR_ENTITY_GLINT_TEXTURING)
          .useLightmap()
          .useOverlay()
          .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
          .affectsCrumbling()
          .sortOnUpload()
          .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
          .createRenderSetup()
    );

    public static final Function<Identifier, RenderType> SPS = Util.memoize(resourceLocation -> RenderType.create("mekanism_sps", RenderSetup.builder(MekanismRenderPipelines.SPS)
          .withTexture("Sampler0", resourceLocation)
          .setOitPipelines(MekanismRenderPipelines.OIT_SPS)
          .sortOnUpload()
          .createRenderSetup()
    ));
}